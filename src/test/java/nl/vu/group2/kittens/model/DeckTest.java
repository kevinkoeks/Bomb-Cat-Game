package nl.vu.group2.kittens.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests on @{@link Deck}s.
 * If you're a reviewer, please ignore this class since it belongs to the test suite.
 */
class DeckTest {

    @Test
    void peekLessThanThree() {
        final List<Card> cards = List.of(Card.ATTACK, Card.EXPLODING_KITTEN);
        final Deck deck = Deck.of(cards);
        final List<Card> peekedCards = deck.peek(0, 3);
        cards.forEach(card ->
                assertTrue(peekedCards.contains(card))
        );
    }

    @Test
    void peekMoreThanThree() {
        final List<Card> cards = List.of(Card.ATTACK, Card.EXPLODING_KITTEN, Card.DEFUSE, Card.NOPE, Card.TACOCAT);
        final Deck deck = Deck.of(cards);
        final List<Card> peekedCards = deck.peek(0, 3);
        List.of(Card.ATTACK, Card.EXPLODING_KITTEN, Card.DEFUSE).forEach(card ->
                assertTrue(peekedCards.contains(card), "Peek doesn't contain " + card)
        );
        List.of(Card.NOPE, Card.TACOCAT).forEach(card ->
                assertFalse(peekedCards.contains(card), "Peek doesn't contain " + card)
        );
    }

    @Test
    void peekIsWhatYouShouldDraw() {
        final List<Card> cards = List.of(Card.ATTACK, Card.EXPLODING_KITTEN, Card.DEFUSE, Card.NOPE, Card.TACOCAT);
        final Deck deck = Deck.of(cards);
        final List<Card> peekedCards = deck.peek(0, 3);
        final Card deckTop = deck.drawCard();
        assertTrue(peekedCards.contains(deckTop), "Peeked cards don't contain the top of the deck");
        assertEquals(peekedCards.get(0), deckTop, "The first peeked card is the one you should draw");
    }
}
