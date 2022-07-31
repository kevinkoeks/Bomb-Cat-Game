package nl.vu.group2.kittens.engine.effect;

import lombok.Value;
import nl.vu.group2.kittens.engine.GameState;
import nl.vu.group2.kittens.engine.player.Player;
import nl.vu.group2.kittens.model.Card;
import nl.vu.group2.kittens.model.Deck;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.vu.group2.kittens.model.GameEvent.infoEvent;

@Value
class SeeTheFutureEffect implements ComboEffect {

    @Override
    public void apply(GameState state) {
        final Deck deck = state.getDeck();
        final List<Card> topCards = deck.peek(0, 3);

        final String resultForCurrentPlayer = getResultForCurrentPlayer(topCards);
        // show the future to the current player
        final Player currentPlayer = state.getCurrentPlayer();
        state.onEvent(infoEvent(resultForCurrentPlayer), Set.of(currentPlayer));
        // but hide it to the opponents
        state.onEvent(infoEvent(currentPlayer.getId() + "\tsaw the future..."), state.getOpponents());
    }

    private String getResultForCurrentPlayer(List<Card> topCards) {
        int cardsCount = topCards.size();
        if (cardsCount == 1) {
            return "The top card on the deck is " + topCards.get(0);
        }
        final String cardsList = topCards.stream().map(Card::name).collect(Collectors.joining(", "));
        return String.format("The top %d cards on the deck are %s", cardsCount, cardsList);
    }
}
