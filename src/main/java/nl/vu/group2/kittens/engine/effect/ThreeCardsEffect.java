package nl.vu.group2.kittens.engine.effect;

import lombok.Value;
import nl.vu.group2.kittens.engine.GameState;
import nl.vu.group2.kittens.engine.player.Player;
import nl.vu.group2.kittens.model.Card;

import static nl.vu.group2.kittens.model.GameEvent.infoEvent;

@Value
class ThreeCardsEffect implements ComboEffect {

    @Override
    public void apply(GameState state) {
        final var currentPlayer = state.getCurrentPlayer();
        final var opponents = state.getActiveOpponents();
        final var requestedCard = currentPlayer.selectCardType();
        final var targetOpponent = currentPlayer.selectPlayer(opponents);
        final var opponentHand = targetOpponent.getHand();

        if (opponentHand.contains(requestedCard)) {
            stealCard(state, currentPlayer, targetOpponent, requestedCard);
        } else {
            state.onEvent(infoEvent(String.format("%s does not have the card %s", targetOpponent.getId(), requestedCard)));
        }
    }

    private void stealCard(GameState state, Player currentPlayer, Player targetOpponent, Card card) {
        state.transferCard(targetOpponent, currentPlayer, card);
        state.onEvent(infoEvent(String.format("%s transferred from %s", card, targetOpponent.getId())));
    }
}
