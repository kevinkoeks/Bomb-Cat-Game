package nl.vu.group2.kittens.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests on @{@link DiscardPile}s.
 * If you're a reviewer, please ignore this class since it belongs to the test suite.
 */
class DiscardPileTest {

    @Test
    void emptyPile() {
        final DiscardPile pile = new DiscardPile();
        assertTrue(pile.getCards().isEmpty());
    }

    @Test
    void cardPresentInSetAfterPush() {
        final DiscardPile pile = new DiscardPile();
        pile.pushCard(Card.NOPE);
        final Set<Card> cards = pile.getCards();
        assertEquals(1, cards.size());
        assertTrue(cards.contains(Card.NOPE));
    }

    @Test
    void extractCardWhichIsPresent() {
        final DiscardPile pile = new DiscardPile();
        pile.pushCard(Card.NOPE);
        pile.extractCard(Card.NOPE);
        assertTrue(pile.getCards().isEmpty());
    }

    @Test
    void extractCardWithManyCardsInPile() {
        final DiscardPile pile = new DiscardPile();
        pile.pushCard(Card.NOPE);
        pile.pushCard(Card.ATTACK);
        pile.pushCard(Card.NOPE);
        pile.extractCard(Card.NOPE);

        final Set<Card> cards = pile.getCards();
        assertEquals(2, cards.size());
        assertTrue(cards.contains(Card.NOPE));
        assertTrue(cards.contains(Card.ATTACK));
    }

    @Test
    void extractCardWithCardNotPresent() {
        final DiscardPile pile = new DiscardPile();
        pile.pushCard(Card.NOPE);
        assertThrows(
                IllegalStateException.class,
                () -> pile.extractCard(Card.ATTACK)
        );
    }
}