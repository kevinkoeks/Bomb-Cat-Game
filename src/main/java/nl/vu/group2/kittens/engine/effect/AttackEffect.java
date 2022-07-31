package nl.vu.group2.kittens.engine.effect;

import lombok.Value;
import nl.vu.group2.kittens.engine.GameState;
import nl.vu.group2.kittens.engine.player.Player;

import java.util.Deque;

import static nl.vu.group2.kittens.model.GameEvent.infoEvent;

@Value
class AttackEffect implements ComboEffect {

    @Override
    public void apply(GameState state) {
        final Player currentPlayer = state.getCurrentPlayer();
        while (currentPlayer.equals(state.getCurrentPlayer())) {
            state.nextTurn();
        }

        final Deque<ComboEffect> effectHistory = state.getEffectHistory();
        long previousAttacksPlayed = effectHistory.stream()
                                                  .takeWhile(this::equals)
                                                  .count();
        final Player victim = state.getCurrentPlayer();
        long additionalTurns = previousAttacksPlayed * 2 + 1;
        state.addTurns(additionalTurns);
        state.onEvent(infoEvent(String.format("PLAYER %s has %d more turns", victim.getId(), additionalTurns)));
    }
}
