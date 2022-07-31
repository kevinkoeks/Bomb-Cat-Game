package nl.vu.group2.kittens.engine;

import lombok.extern.slf4j.Slf4j;
import nl.vu.group2.kittens.engine.effect.ComboEffect;
import nl.vu.group2.kittens.engine.player.Player;
import nl.vu.group2.kittens.model.Card;
import nl.vu.group2.kittens.model.Deck;
import nl.vu.group2.kittens.model.DiscardPile;
import nl.vu.group2.kittens.model.GameEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public final class GameState {

    private final List<Player> players;
    private final Deque<Player> turns;
    private final Deck deck;
    private final DiscardPile discardPile;
    private final Deque<ComboEffect> effectHistory;

    public GameState(List<Player> players, Deck deck) {
        this.players = List.copyOf(players);
        this.turns = new LinkedList<>(players);
        this.deck = deck;
        this.discardPile = new DiscardPile();
        this.effectHistory = new LinkedList<>();
    }

    //<editor-fold desc="accessors">
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public Collection<Player> getActivePlayers() {
        return new HashSet<>(turns);
    }

    public Player getCurrentPlayer() {
        return turns.getFirst();
    }

    public Collection<Player> getOpponents() {
        final Player currentPlayer = getCurrentPlayer();
        return getPlayers().stream()
                           .filter(Predicate.not(currentPlayer::equals))
                           .collect(Collectors.toList());
    }

    public Collection<Player> getActiveOpponents() {
        final Collection<Player> activePlayers = getActivePlayers();
        return getOpponents().stream()
                             .filter(activePlayers::contains)
                             .collect(Collectors.toList());
    }

    public Deck getDeck() {
        return deck.copy();
    }

    public Deque<ComboEffect> getEffectHistory() {
        return new LinkedList<>(effectHistory);
    }

    public void addEffectToHistory(ComboEffect effect) {
        effectHistory.addFirst(effect);
    }
    //</editor-fold>

    public Card drawCard() {
        final Card card = deck.drawCard();
        final Player currentPlayer = getCurrentPlayer();
        currentPlayer.addCard(card);
        return card;
    }

    public void reinsertCard(Player player, Card card, int offset) {
        player.removeCardFromHand(card);
        deck.insertCard(card, offset);
    }

    public void discard(Player player, Card card) {
        player.removeCardFromHand(card);
        discardPile.pushCard(card);
    }

    public void shuffleDeck() {
        deck.shuffle();
    }

    public void transferCard(Player from, Player to, Card card) {
        from.removeCardFromHand(card);
        to.addCard(card);
    }

    public void transferFromPile(Player player, Card card) {
        discardPile.extractCard(card);
        player.addCard(card);
    }

    public Set<Card> getDiscardedCards() {
        return discardPile.getCards();
    }

    public void nextTurn() {
        final Player currentPlayer = turns.removeFirst();
        final Player nextPlayer = turns.peekFirst();
        if (nextPlayer != null && currentPlayer != nextPlayer) {
            turns.addLast(currentPlayer);
        }
    }

    public void addTurns(long numberOfTurns) {
        for (long i = 0; i < numberOfTurns; i++) {
            turns.addFirst(turns.getFirst());
        }
    }

    public void removeFromGame(Player player) {
        log.info("BEFORE removing from game: turns '{}'", turns);
        while (turns.contains(player)) {
            turns.remove(player);
        }
        log.info("AFTER removing from game: turns '{}'", turns);
    }

    public void onEvent(GameEvent event) {
        this.onEvent(event, getPlayers());
    }

    public void onEvent(GameEvent event, Collection<Player> players) {
        players.forEach(player -> player.onEvent(event));
    }
}
