package nl.vu.group2.kittens.model;

import lombok.NonNull;
import lombok.Value;

/**
 * Type-safe wrapper for description-type pairs which embody the details of an event happened inside the game.
 * <p>
 * This class provided some convenience methods to create events with less verbosity than what you usually would have
 * in Java for better readability.
 * These methods don't strictly follow the convention of starting their names with a verb, but we chose understandability
 * over convention in this case.
 */
@Value
public class GameEvent {

    @NonNull String description;
    EventType type;

    public static GameEvent infoEvent(String description) {
        return new GameEvent(description, EventType.INFO);
    }

    public static GameEvent systemEvent(String description) {
        return new GameEvent(description, EventType.SYSTEM);
    }

    public static GameEvent errorEvent(String description) {
        return new GameEvent(description, EventType.ERROR);
    }

    @Override
    public String toString() {
        return type + ": " + description;
    }

    public static GameEvent of(String str) {
        final String[] tokens = str.split(": ", 2);
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Invalid string received for GameEvent: " + str);
        }
        return new GameEvent(tokens[1], EventType.valueOf(tokens[0]));
    }

    public enum EventType {
        INFO,
        SYSTEM,
        ERROR
    }
}
