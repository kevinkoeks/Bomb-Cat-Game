package nl.vu.group2.kittens.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Value
@Slf4j
public class DiscardPile {

    @Getter(value = AccessLevel.NONE)
    List<Card> discardedCards;

    public DiscardPile() {
        this.discardedCards = new LinkedList<>();
    }

    public void pushCard(Card card) {
        discardedCards.add(card);
    }

    public void extractCard(Card card) {
        if (!discardedCards.contains(card)) {
            log.error("Trying to extract a card {} which is not present in the discard pile.", card);
            throw new IllegalStateException(String.format("Card %s not present in the discard pile.", card));
        }
        discardedCards.remove(card);
    }

    public Set<Card> getCards() {
        return new HashSet<>(discardedCards);
    }
}
