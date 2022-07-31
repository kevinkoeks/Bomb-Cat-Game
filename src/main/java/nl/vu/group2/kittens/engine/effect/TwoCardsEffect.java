package nl.vu.group2.kittens.engine.effect;

import lombok.Value;
import nl.vu.group2.kittens.engine.GameState;
import nl.vu.group2.kittens.engine.player.Player;
import nl.vu.group2.kittens.model.Card;

import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static nl.vu.group2.kittens.model.GameEvent.infoEvent;

@Value
class TwoCardsEffect implements ComboEffect {

    @Override
    public void apply(GameState state) {
        final var currentPlayer = state.getCurrentPlayer();
        final var opponents = state.getActiveOpponents();
        final var targetOpponent = currentPlayer.selectPlayer(opponents);
        final var maybeCard = currentPlayer.selectCardOf(targetOpponent);

        if (maybeCard.isEmpty()) {
            state.onEvent(infoEvent(String.format("The target player %s has no cards", targetOpponent.getId())));
            return;
        }

        final Card card = maybeCard.get();
        state.transferCard(targetOpponent, currentPlayer, card);

        // notify the two players involved
        final Set<Player> twoCardPlayers = Set.of(currentPlayer, targetOpponent);
        final var message = stealMessage(currentPlayer, targetOpponent);
        state.onEvent(infoEvent(message.apply(card.name())), twoCardPlayers);

        // notify the other players, without showing the card
        final Set<Player> otherPlayers = state.getPlayers()
                                              .stream()
                                              .filter(not(twoCardPlayers::contains))
                                              .collect(Collectors.toSet());
        state.onEvent(infoEvent(message.apply("a card")), otherPlayers);
    }

    /**
     * Currified method to avoid repetition in the {@link #apply(GameState)} method.
     */
    private static UnaryOperator<String> stealMessage(Player currentPlayer, Player targetOpponent) {
        return cardName -> String.format(
                "PLAYER %s stole %s from PLAYER %s",
                currentPlayer, cardName, targetOpponent
        );
    }
}
