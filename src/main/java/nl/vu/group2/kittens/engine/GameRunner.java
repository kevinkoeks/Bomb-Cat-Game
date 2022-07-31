package nl.vu.group2.kittens.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import nl.vu.group2.kittens.engine.player.AiPlayer;
import nl.vu.group2.kittens.engine.player.HumanPlayer;
import nl.vu.group2.kittens.engine.player.Player;
import nl.vu.group2.kittens.model.Card;
import nl.vu.group2.kittens.model.Deck;
import nl.vu.group2.kittens.network.Network;
import nl.vu.group2.kittens.ui.UserInterface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static nl.vu.group2.kittens.model.GameEvent.errorEvent;
import static nl.vu.group2.kittens.model.GameEvent.infoEvent;
import static nl.vu.group2.kittens.model.GameEvent.systemEvent;

/**
 * This class has the responsibility of bootstrapping an Exploding Kittens game.
 * Note that Bootstrapping a game also includes:
 * - Creating the list of {@link Player}s; and
 * - Creating the {@link Deck}.
 */
@Slf4j
@Value
public class GameRunner {

    private static final int INITIAL_HAND_SIZE = 8;
    private static final String AI_PLAYER_NAME_PATTERN = "^AI \\d+$";
    private static final String YES_NO_CHOICE = "^[YyNn]?$";
    private static final Duration JOIN_TIMEOUT = Duration.ofDays(7L);
    public static final int MAX_PLAYER_COUNT = 5;

    UserInterface ui;

    public void start() {
        final GameType gameType = getGameType();
        if (gameType == GameType.JOIN_NETWORKED) {
            new RemoteGame(ui).run();
            return;
        }
        final List<Player> players = createPlayers(gameType);
        final List<Card> deckCards = createDeckCards(players);
        distributeCards(players, deckCards);
        final GameState state = new GameState(players, Deck.of(deckCards));
        new HostedGame(state).run();
    }

    private GameType getGameType() {
        String isLocal;
        do {
            isLocal = ui.query("Is this game local? (Y/n)");
        } while (!isLocal.matches(YES_NO_CHOICE));
        if (isLocal.equalsIgnoreCase("n")) {
            String isHost;
            do {
                isHost = ui.query("Are you hosting the game? (Y/n)");
            } while (!isHost.matches(YES_NO_CHOICE));
            return isHost.equalsIgnoreCase("n") ? GameType.JOIN_NETWORKED : GameType.HOST_NETWORKED;
        }
        return GameType.LOCAL;
    }

    // <editor-fold desc="Create players methods">
    private List<Player> createPlayers(GameType gameType) {
        final List<Player> players = new ArrayList<>();
        final Player humanPlayer = createHumanPlayer();
        players.add(humanPlayer);
        int opponentsCount = selectNumOpponents();
        switch (gameType) {
            case LOCAL:
                players.addAll(createAiPlayers(opponentsCount));
                break;
            case HOST_NETWORKED:
                players.addAll(connectPlayers(opponentsCount));
                break;
            default:
                throw new UnsupportedOperationException("Game type " + gameType + " not supported");
        }
        log.info("Initialized: {}", players);
        return players;
    }

    private List<Player> createAiPlayers(int opponentsCount) {
        final List<Player> players = new ArrayList<>(opponentsCount);
        IntStream.range(1, opponentsCount + 1)
                 .mapToObj(Integer::toString)
                 .map(i -> new AiPlayer("AI " + i))
                 .forEach(players::add);
        return players;
    }

    private int selectNumOpponents() {
        return ui.queryNumber("Select the number of opponents", 1, MAX_PLAYER_COUNT - 1);
    }

    private List<Player> connectPlayers(int playersCount) {
        final String waitingMessage = String.format("Waiting for %d players to connect to port %d", playersCount, Network.PORT);
        ui.notify(systemEvent(waitingMessage));
        return Network.getInstance().registerPlayers(playersCount);
    }

    private Player createHumanPlayer() {
        final String humanPlayerName = selectName();
        return new HumanPlayer(humanPlayerName, ui);
    }

    private String selectName() {
        final String name = ui.query("Insert your name");
        if (name.isBlank() || name.matches(AI_PLAYER_NAME_PATTERN)) {
            ui.notify(errorEvent("You chose an invalid name, please enter again your choice."));
            return selectName();
        }
        return name;
    }
    // </editor-fold>

    // <editor-fold desc="Create deck methods">
    private List<Card> createDeckCards(List<Player> players) {
        final String deckChoiceYn = ui.query("Do you want to use the base deck? (Y/n)");
        if (!deckChoiceYn.matches(YES_NO_CHOICE)) {
            ui.notify(errorEvent("Please write Y, N, or leave blank."));
            return createDeckCards(players);
        }
        if (deckChoiceYn.equalsIgnoreCase("n")) {
            return getCardsForCustomDeck(players.size());
        } else {
            return getCardsForBaseDeck(players.size());
        }
    }

    private static List<Card> getCardsForBaseDeck(int playersCount) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            cards.addAll(List.of(Card.ATTACK, Card.SKIP, Card.FAVOR, Card.SHUFFLE, Card.TACOCAT, Card.BEARD_CAT, Card.CATTERMELON, Card.HAIRY_POTATO_CAT, Card.RAINBOW_RALPHING_CAT));
        }
        for (int i = 0; i < 5; i++) {
            cards.addAll(List.of(Card.NOPE, Card.SEE_THE_FUTURE));
        }
        for (int i = 0; i < 6 - playersCount; i++) { // NOTE: now it's 6 DEFUSE, later it should be according to the rules
            cards.addAll(List.of(Card.DEFUSE));
        }
        for (int i = 0; i < playersCount - 1; i++) {
            cards.addAll(List.of(Card.EXPLODING_KITTEN));
        }
        return cards;
    }

    private List<Card> getCardsForCustomDeck(int playersCount) {
        final String pathInput = ui.query("Enter the path to the deck");
        final Path deckPath;
        try {
            deckPath = Paths.get(pathInput);
            if (Files.notExists(deckPath) || Files.isDirectory(deckPath)) {
                throw new InvalidPathException(pathInput, "The path either does not exist or is a directory");
            }
        } catch (InvalidPathException throwable) {
            ui.notify(errorEvent("Please provide a valid path"));
            return getCardsForCustomDeck(playersCount);
        }
        final String deckJson;
        try {
            deckJson = Files.readString(deckPath);
        } catch (IOException e) {
            ui.notify(errorEvent("Error while reading, please provide a valid path"));
            return getCardsForCustomDeck(playersCount);
        }
        try {
            final Map<Card, Integer> parsedMap = JSON.parseObject(deckJson, new TypeReference<>() {
            });
            final List<Card> deckCards = validateCustomDeck(parsedMap, playersCount);
            ui.notify(infoEvent("Custom deck successfully loaded."));
            return deckCards;
        } catch (JSONException | InvalidDeckException exception) {
            log.error("Error while parsing the JSON file containing the deck", exception);
            ui.notify(errorEvent("Error while parsing the JSON file, please provide a valid file. For more information, check the logs."));
            return getCardsForCustomDeck(playersCount);
        }
    }

    private static List<Card> validateCustomDeck(Map<Card, Integer> cardsAndCounts, int playersCount) throws InvalidDeckException {
        if (cardsAndCounts.containsKey(null)) {
            throw new InvalidDeckException("At least one of the cards in the JSON is invalid.");
        }
        final Integer totalCardsCount = cardsAndCounts.values().stream().reduce(Integer::sum).orElse(0);
        // total must be >= than cards in all player's (initial) hands + `playersCount` - 1 exploding kittens
        if (totalCardsCount < (playersCount + 1) * INITIAL_HAND_SIZE - 1) {
            throw new InvalidDeckException(String.format("Not enough cards in the deck (%d).", totalCardsCount));
        }
        if (cardsAndCounts.get(Card.DEFUSE) < playersCount) {
            throw new InvalidDeckException("Not enough defuse cards.");
        }
        if (cardsAndCounts.get(Card.EXPLODING_KITTEN) < playersCount - 1) {
            throw new InvalidDeckException("Not enough exploding kittens.");
        }
        cardsAndCounts.put(Card.EXPLODING_KITTEN, playersCount - 1);

        final List<Card> cardsFromJsonDeck = cardsAndCounts.entrySet()
                                                           .stream()
                                                           .map(entry -> Collections.nCopies(entry.getValue(), entry.getKey()))
                                                           .flatMap(Collection::stream)
                                                           .collect(Collectors.toList());
        log.info("Parsed deck: {}", cardsAndCounts);
        return cardsFromJsonDeck;
    }
    // </editor-fold>

    private static void distributeCards(List<Player> players, List<Card> cards) {
        Collections.shuffle(cards);
        for (Player player : players) {
            player.addCard(Card.DEFUSE);
            final List<Card> otherHandCards = cards.stream()
                                                   .filter(GameRunner::isAnInitialHandCard)
                                                   .limit(7L)
                                                   .collect(Collectors.toList());
            otherHandCards.forEach(player::addCard);
            otherHandCards.forEach(cards::remove);
        }
        Collections.shuffle(cards);
    }

    private static boolean isAnInitialHandCard(Card card) {
        return card != Card.EXPLODING_KITTEN && card != Card.DEFUSE;
    }

    /**
     * NOTE: Since this exception is used only inside this class, we thought it was better not to create a dedicated
     * file for it.
     */
    private static class InvalidDeckException extends Exception {
        public InvalidDeckException(String message) {
            super(message);
        }
    }

    private enum GameType {
        LOCAL,
        HOST_NETWORKED,
        JOIN_NETWORKED
    }
}
