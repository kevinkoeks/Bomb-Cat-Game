package nl.vu.group2.kittens.engine.effect;

import nl.vu.group2.kittens.engine.GameState;

/**
 * Abstract type for effects applied to the state of a game as a result of a player using a combination of cards.
 */
public interface ComboEffect {

    void apply(GameState state);
}
