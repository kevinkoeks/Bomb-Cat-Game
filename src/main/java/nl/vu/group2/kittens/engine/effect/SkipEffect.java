package nl.vu.group2.kittens.engine.effect;

import lombok.Value;
import nl.vu.group2.kittens.engine.GameState;
import nl.vu.group2.kittens.engine.player.Player;

import static nl.vu.group2.kittens.model.GameEvent.infoEvent;

@Value
class SkipEffect implements ComboEffect {

    @Override
    public void apply(GameState state) {
        state.nextTurn();
        final Player currentPlayer = state.getCurrentPlayer();
        state.onEvent(infoEvent(currentPlayer.getId() + " skipped a turn"));
    }
}
