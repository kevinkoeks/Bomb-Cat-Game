package nl.vu.group2.kittens.engine;

import lombok.extern.slf4j.Slf4j;
import nl.vu.group2.kittens.engine.effect.CardCombo;
import nl.vu.group2.kittens.engine.effect.ComboEffect;
import nl.vu.group2.kittens.engine.player.Player;
import nl.vu.group2.kittens.model.Card;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static nl.vu.group2.kittens.model.GameEvent.errorEvent;
import static nl.vu.group2.kittens.model.GameEvent.infoEvent;
import static nl.vu.group2.kittens.model.GameEvent.systemEvent;

@Slf4j
class HostedGame implements Game {

    private final GameState state;
    private final Scoreboard scoreboard;

    HostedGame(GameState state) {
        this.state = state;
        this.scoreboard = new Scoreboard();
        log.info("Game initialized! {}", state);
    }

    @Override
    public void run() {
        showInitialCards();
        // If we want to quit the game before the "logic" end of the game, we might need to change the condition here
        while (state.getActivePlayers().size() > 1) {
            doTurn();
        }
        // at the end of the game there is only 1 active player, who is the winner
        final Player winner = state.getActivePlayers().iterator().next();
        state.onEvent(infoEvent("Congratulations, you won!"), Set.of(winner));
        scoreboard.add(winner.getId(), state.getActivePlayers().size());
        // while all the other players are the losers
        final Set<Player> losers = state.getPlayers().stream().filter(not(winner::equals)).collect(Collectors.toSet());
        state.onEvent(infoEvent(String.format("You lost!\t%s won", winner.getId())), losers);
        scoreboard.update();
        state.onEvent(systemEvent(scoreboard.toString()));
        state.onEvent(systemEvent(GAME_ENDED_MARKER));
    }

    private void showInitialCards() {
        state.getPlayers()
             .forEach(player -> {
                 final String hand = player.serializeHand();
                 state.onEvent(infoEvent("Your initial hand is \n" + hand), Set.of(player));
             });
    }

    private void doTurn() {
        final Player currentPlayer = state.getCurrentPlayer();
        state.onEvent(systemEvent(String.format("%s\tis currently playing", currentPlayer.getId())));
        while (currentPlayer.equals(state.getCurrentPlayer())) { // NOTE: A turn might end because of card effects
            final List<Card> handCopy = currentPlayer.getHand();
            state.onEvent(infoEvent("Select a card to play"), Set.of(currentPlayer));
            final List<Card> cardSelection = currentPlayer.selectCardFrom(handCopy);
            if (cardSelection.isEmpty()) {
                endTurn();
            } else {
                onCardsPlayed(cardSelection);
            }
        }
    }

    private void endTurn() {
        final Player currentPlayer = state.getCurrentPlayer();
        final Card card = state.drawCard();
        state.onEvent(systemEvent(String.format("%s\tdrawn", card)), Set.of(currentPlayer));
        state.onEvent(systemEvent(String.format("%s\tdrew a card", currentPlayer.getId())), state.getOpponents());
        if (explodesBecauseOfKitten(currentPlayer, card)) {
            log.info("Player {} exploded!", currentPlayer);
            state.onEvent(errorEvent(String.format("%s\tExploded", currentPlayer.getId())), state.getActivePlayers());
            scoreboard.add(currentPlayer.getId(), state.getActivePlayers().size());
            currentPlayer.getHand().forEach(c -> state.discard(currentPlayer, c));
            state.removeFromGame(currentPlayer);
            return;
        }
        state.onEvent(systemEvent(String.format("%n%d Cards left in the deck! %n%.2f%% chance of drawing an exploding kitten%n", state.getDeck().size(), state.getDeck().explodingOdds() * 100)));
        state.addEffectToHistory(NoOperationEffect.INSTANCE);
        state.nextTurn();
    }

    private boolean explodesBecauseOfKitten(Player player, Card card) {
        if (card != Card.EXPLODING_KITTEN) {
            return false;
        }
        state.onEvent(errorEvent(String.format("%s\tPicked an exploding kitten!", player.getId())), state.getActivePlayers());
        boolean cannotDefuse = !player.getHand().contains(Card.DEFUSE);
        if (cannotDefuse) {
            return true;
        }

        log.info("Explosion defused for {}", player);
        state.onEvent(systemEvent(String.format("%s\tDefused the explosion!", player.getId())), state.getActivePlayers());
        state.discard(player, Card.DEFUSE);
        int newIndex = player.reinsertExplodingKitten(state.getDeck().size());
        state.reinsertCard(player, Card.EXPLODING_KITTEN, newIndex);
        return false;
    }

    private void onCardsPlayed(List<Card> cards) {
        final var currentPlayer = state.getCurrentPlayer();
        final Optional<ComboEffect> maybeComboEffect = new CardCombo(cards).getEffect();
        if (maybeComboEffect.isEmpty()) {
            state.onEvent(errorEvent("Invalid card selection"), Set.of(currentPlayer));
            return;
        }
        final var comboEffect = maybeComboEffect.get();
        state.onEvent(systemEvent(String.format("%s\tis about to be played by %s", cards.toString(), currentPlayer.getId())));
        cards.forEach(card -> state.discard(currentPlayer, card));
        final boolean actionNoped = isNoped(currentPlayer, comboEffect.toString());
        if (actionNoped) {
            state.onEvent(systemEvent(String.format("%s\thas been NOPE'd!", comboEffect.toString())));
        } else {
            comboEffect.apply(state);
            state.addEffectToHistory(comboEffect);
        }
    }

    private boolean isNoped(Player player, String action) {
        Stream<Player> otherPlayers = state.getActivePlayers()
                                           .stream()
                                           .filter(not(player::equals));
        final CompletableFuture<Optional<Player>> nopePlayed = new CompletableFuture<>();
        nopePlayed.completeOnTimeout(Optional.empty(), 5L, TimeUnit.SECONDS);
        otherPlayers.forEach(p -> p.askForNope(action, nopePlayed));
        final Optional<Player> nopingPlayer;
        try {
            nopingPlayer = nopePlayed.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return false;
        }
        if (nopingPlayer.isPresent()) {
            state.discard(nopingPlayer.get(), Card.NOPE);
            state.onEvent(infoEvent(String.format("%s\tHas been noped by %s.", action, nopingPlayer.get().getId())));
            // NOTE: Please look closely the following tail-recursive return: there is a '!' in front of that for noping NOPEs
            return !isNoped(nopingPlayer.get(), "NOPE on " + action);
        } else {
            log.info("Action {} not NOPE'd", action);
            return false;
        }
    }

    /**
     * Used as a null object in the effect history.
     * See also: https://en.wikipedia.org/wiki/Null_object_pattern
     */
    private enum NoOperationEffect implements ComboEffect {

        INSTANCE;

        @Override
        public void apply(GameState state) {
            // NO-OP
        }
    }
}
