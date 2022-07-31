package nl.vu.group2.kittens.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.min;

@Value
public class Deck {

    @Getter(value = AccessLevel.NONE)
    List<Card> cards;

    private Deck(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
    }

    public Card drawCard() {
        return cards.remove(0);
    }

    public void insertCard(Card card, int offset) {
        cards.add(offset, card);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<Card> peek(int from, int to) {
        final List<Card> peekedCards = new ArrayList<>();
        for (int i = from; i < min(to, size()); i++) {
            peekedCards.add(cards.get(i));
        }
        return peekedCards;
    }

    public int totalExplodingCards() {
        Card explodingCard = Card.EXPLODING_KITTEN;
        int nrOfCards = 0;
        for (int i = 0; i < size(); i++) {
            if (cards.get(i) == explodingCard) {
                nrOfCards++;
            }
        }
        return nrOfCards;
    }

    public double explodingOdds() {
        return ((double) totalExplodingCards()) / size();
    }

    public Deck copy() {
        return new Deck(this.cards);
    }

    public int size() {
        return cards.size();
    }

    public static Deck of(List<Card> cards) {
        return new Deck(cards);
    }
}
