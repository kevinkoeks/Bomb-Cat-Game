package nl.vu.group2.kittens.engine.effect;

import lombok.Value;
import nl.vu.group2.kittens.engine.GameState;

import static nl.vu.group2.kittens.model.GameEvent.infoEvent;

@Value
class ShuffleEffect implements ComboEffect {

    @Override
    public void apply(GameState state) {
        state.shuffleDeck();
        state.onEvent(infoEvent("The deck has been shuffled"));
    }
}
