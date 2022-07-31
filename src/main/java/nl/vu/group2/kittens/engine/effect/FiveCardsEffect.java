package nl.vu.group2.kittens.engine.effect;

import lombok.Value;
import nl.vu.group2.kittens.engine.GameState;
import nl.vu.group2.kittens.engine.player.Player;
import nl.vu.group2.kittens.model.Card;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static nl.vu.group2.kittens.model.GameEvent.infoEvent;

@Value
class FiveCardsEffect implements ComboEffect {

    @Override
    public void apply(GameState state) {
        final var currentPlayer = state.getCurrentPlayer();
        final Set<Card> discardedCards = state.getDiscardedCards();

        if (discardedCards.isEmpty()) {
            state.onEvent(infoEvent("The five-card effect was played, but the discard pile has no cards"));
            return;
        }

        // if the discard pile was empty, we would have returned in the above if statement
        // now, we want to force a player to choose one card from the discard pile
        List<Card> requestedCard;
        do {
            state.onEvent(infoEvent("Select a card. The first option (0) will be considered invalid"), Set.of(currentPlayer));
            requestedCard = currentPlayer.selectCardFrom(discardedCards);
        } while (requestedCard.isEmpty());

        final Card card = requestedCard.get(0);
        state.transferFromPile(currentPlayer, card);

        final String baseMessage = String.format("Card transferred from the discard pile to PLAYER %s", currentPlayer.getId());
        state.onEvent(infoEvent(baseMessage + ": " + card), Set.of(currentPlayer));

        final Set<Player> otherPlayers = state.getPlayers()
                                              .stream()
                                              .filter(not(currentPlayer::equals))
                                              .collect(Collectors.toSet());
        state.onEvent(infoEvent(baseMessage), otherPlayers);
    }
}
