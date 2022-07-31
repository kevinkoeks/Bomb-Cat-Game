package nl.vu.group2.kittens.engine.player;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.vu.group2.kittens.model.Card;
import nl.vu.group2.kittens.model.GameEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Parent class for players.
 * If you want to extend this class, you have to implement the region with the abstract methods inside.
 */
@Slf4j
public abstract class Player {

    @Getter
    private final String id;
    private final List<Card> hand;
    private static final int HAND_SIZE = 8;

    protected Player(String id) {
        this.id = id;
        this.hand = new ArrayList<>(HAND_SIZE);
    }

    public List<Card> getHand() {
        return new ArrayList<>(hand);
    }

    public void removeCardFromHand(Card card) {
        hand.remove(card);
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public String serializeHand() {
        return serializeAsNumberedList(this.hand);
    }

    //<editor-fold desc="abstract methods">
    public abstract void onEvent(GameEvent event);

    public abstract Card selectCardType();

    public abstract Player selectPlayer(Collection<Player> players);

    public abstract Optional<Card> selectCardOf(Player player);

    public abstract List<Card> selectCardFrom(Collection<Card> cards);

    public abstract int reinsertExplodingKitten(int deckSize);

    public abstract void askForNope(String action, CompletableFuture<Optional<Player>> nopePlayedBy);
    //</editor-fold>


    @Override
    public String toString() {
        return "Player " + id;
    }

    protected static <T> String serializeAsNumberedList(List<T> list) {
        final StringBuilder text = new StringBuilder();
        int i = 1;
        for (T elem : list) {
            text.append(i)
                .append(": ")
                .append(elem.toString())
                .append("\n");
            i++;
        }
        return text.toString();
    }
}
