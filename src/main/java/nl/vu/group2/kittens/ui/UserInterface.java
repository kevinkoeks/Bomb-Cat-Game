package nl.vu.group2.kittens.ui;

import nl.vu.group2.kittens.model.GameEvent;

import java.time.Duration;
import java.util.List;

public interface UserInterface {

    void notify(GameEvent event);

    /**
     * Reads an input from the user.
     *
     * @param prompt text prompting the user for input
     */
    String query(String prompt);

    /**
     * Reads a number from the user.
     */
    default int queryNumber(String prompt, int min, int max) {
        return queryNumber(prompt, min, max, null, 0);
    }

    /**
     * Reads a number from the user, within a timeout.
     * After the timeout, the defaultValue will be returned.
     * If timeout is null, then no timeout will be applied.
     */
    int queryNumber(String prompt, int min, int max, Duration timeout, int defaultValue);

    /**
     * Returns a list of numbers selected by the user.
     * All numbers are checked to be less or equal than max.
     */
    List<Integer> queryNumbers(String prompt, int max);

    void close();
}
