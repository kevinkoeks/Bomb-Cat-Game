package nl.vu.group2.kittens.engine.player;

import lombok.extern.slf4j.Slf4j;
import nl.vu.group2.kittens.model.Card;
import nl.vu.group2.kittens.model.GameEvent;
import org.apache.commons.lang3.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * This kind of player automatically plays with no human controlling its moves.
 *
 * There is a bit of repetition in the code due to the pauses always happening before making a decision, but we
 * preferred to implement it in this way to have it more explicit and visible.
 */
@Slf4j
public class AiPlayer extends Player {

    private static final int WAIT_TIME = 750;

    public AiPlayer(String id) {
        super(id);
    }

    @Override
    public void onEvent(GameEvent event) {
        // NO-OP
        log.info("{} logging {}", this, event);
    }

    @Override
    public Card selectCardType() {
        pauseAiPlayer();
        final List<Card> allCardVariants = EnumUtils.getEnumList(Card.class);
        return returnRandom(allCardVariants);
    }

    @Override
    public Player selectPlayer(Collection<Player> players) {
        pauseAiPlayer();
        return returnRandom(players);
    }

    @Override
    public Optional<Card> selectCardOf(Player player) {
        pauseAiPlayer();
        final List<Card> opponentHand = player.getHand();
        return Optional.ofNullable(returnRandom(opponentHand));
    }

    @Override
    public List<Card> selectCardFrom(Collection<Card> cards) {
        pauseAiPlayer();
        if (cards.isEmpty() || new Random().nextBoolean()) { // sometimes just return nothing
            return List.of();
        }
        return playCard(cards, returnRandom(cards));
    }

    private List<Card> playCard(Collection<Card> cards, Card choice) {
        pauseAiPlayer();
        int numberOfSameCards = Collections.frequency(cards, choice);
        if (numberOfSameCards > 1) {
            return createCombo(choice, numberOfSameCards);
        }
        return Collections.singletonList(choice);
    }

    private List<Card> createCombo(Card choice, int comboSize) {
        ArrayList<Card> combo = new ArrayList<>();
        int size = Math.min(comboSize, 3);
        for (int i = 0; i < size; i++) {
            combo.add(choice);
        }
        return combo;
    }

    @Override
    public int reinsertExplodingKitten(int deckSize) {
        // Check if EK is last card in the deck, if so reinsert in only position possible
        if (deckSize <= 0) {
            return 0;
        }
        return new Random().nextInt(deckSize - 1);
    }

    @Override
    public void askForNope(String action, CompletableFuture<Optional<Player>> nopePlayedBy) {
        // NOTE: an AI player does _not_ always play a NOPE card
        if (this.getHand().contains(Card.NOPE) && new Random().nextBoolean()) {
            log.info("{} is noping", getId());
            // supplyAsync is used to have some (minimal) variance in playing the NOPE
            // due to the JVM assigning a thread to this runnable
            CompletableFuture.supplyAsync(() -> nopePlayedBy.complete(Optional.of(this)));
        }
    }

    /**
     * Returns a random element from the collection.
     * If the collection is empty, this method will return null.
     */
    private <T> T returnRandom(Collection<T> coll) {
        int rand = new Random().nextInt(coll.size());
        for (T elem : coll) if (--rand < 0) return elem;
        return null;
    }

    private void pauseAiPlayer() {
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            log.error("AI player interrupted while sleeping", e);
            Thread.currentThread().interrupt();
        }
    }
}
