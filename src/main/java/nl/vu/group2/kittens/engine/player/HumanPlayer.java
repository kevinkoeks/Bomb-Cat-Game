package nl.vu.group2.kittens.engine.player;

import lombok.extern.slf4j.Slf4j;
import nl.vu.group2.kittens.model.Card;
import nl.vu.group2.kittens.model.GameEvent;
import nl.vu.group2.kittens.ui.UserInterface;
import org.apache.commons.lang3.EnumUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static nl.vu.group2.kittens.model.GameEvent.infoEvent;

/**
 * Player (local or remote) controlled by a human.
 *
 * This class directly interacts with a user interface (i.e., CLI or network).
 */
@Slf4j
public class HumanPlayer extends Player {

    private static final Duration NOPE_TIMEOUT = Duration.ofSeconds(5L).minus(Duration.ofMillis(500L));
    private final UserInterface ui;

    public HumanPlayer(String id, UserInterface ui) {
        super(id);
        this.ui = ui;
    }

    @Override
    public void onEvent(GameEvent event) {
        ui.notify(event);
    }

    @Override
    public Card selectCardType() {
        final List<Card> allCardVariants = EnumUtils.getEnumList(Card.class);
        final int cardChoice = ui.queryNumber(String.format("Choose a card:%n%s", serializeAsNumberedList(allCardVariants)), 1, Card.values().length);
        return allCardVariants.get(cardChoice - 1);
    }

    @Override
    public Player selectPlayer(Collection<Player> players) {
        final List<Player> playersList = new ArrayList<>(players);
        playersList.sort(Comparator.comparing(Player::getId));
        final StringBuilder playerString = new StringBuilder();
        for (int i = 0; i < playersList.size(); i++) {
            playerString.append(i + 1)
                        .append(": ")
                        .append(playersList.get(i).getId())
                        .append("\n");
        }
        final int playerIndex = ui.queryNumber(String.format("Select an opponent: %n%s", playerString), 1, players.size()) - 1;
        return playersList.get(playerIndex);
    }

    @Override
    public Optional<Card> selectCardOf(Player player) {
        final int handSize = player.getHand().size();
        final int choice = ui.queryNumber(String.format("Player has %d cards, choose 1-%d:%n", handSize, handSize), 1, handSize);
        return Optional.ofNullable(player.getHand().get(choice - 1));
    }

    @Override
    public List<Card> selectCardFrom(Collection<Card> cards) {
        List<Card> cardsList = List.copyOf(cards);
        final String selectCardMessage = "0. PICK CARD" +
                "\n" + serializeAsNumberedList(cardsList) + "\n>";
        final List<Integer> rawCardInput = ui.queryNumbers(selectCardMessage, cards.size());
        if (rawCardInput.isEmpty() || rawCardInput.get(0) == 0) {
            return List.of();
        }
        return getCardsFromHand(cardsList, rawCardInput);
    }

    @Override
    public int reinsertExplodingKitten(int deckSize) {
        return ui.queryNumber(String.format("Where do you want to reinsert it? (Range 0-%d)", deckSize), 0, deckSize);
    }

    @Override
    public void askForNope(String action, CompletableFuture<Optional<Player>> nopePlayedBy) {
        if (!getHand().contains(Card.NOPE)) {
            ui.notify(infoEvent("You cannot NOPE :("));
            return;
        }
        final String nopePrompt = action + " is about to be happen. Do you want to nope? (0 - NO / 1 - YES)";
        boolean isNoping = ui.queryNumber(nopePrompt, 0, 1, NOPE_TIMEOUT, 0) == 1;
        if (isNoping) {
            log.info("{} is noping", getId());
            nopePlayedBy.complete(Optional.of(this));
        }
    }

    private List<Card> getCardsFromHand(List<Card> cardsList, List<Integer> cardIndexes) {
        final List<Card> cards = new ArrayList<>();
        // card indexes is transformed to a set in the loop, otherwise the same card could be played n times (possible bug)
        for (Integer cardIndex : new HashSet<>(cardIndexes)) {
            cards.add(cardsList.get(cardIndex - 1));
        }
        return cards;
    }
}
