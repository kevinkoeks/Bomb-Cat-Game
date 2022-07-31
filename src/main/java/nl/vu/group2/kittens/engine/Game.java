package nl.vu.group2.kittens.engine;

/**
 * Common type for Exploding Kittens matches.
 */
public interface Game {

    String GAME_ENDED_MARKER = "Game ended.";

    void run();
}
