package nl.vu.group2.kittens.engine;

import lombok.Value;

/**
 * Note about Result: Lombok already creates the @AllArgsConstructor thanks to the @Value annotation.
 * There is no need to define a constructor here in the code, which might be even harmful for FastJSON.
 * <p>
 * This is because FastJSON looks at the name of the parameters of the constructor and expects to find the same names
 * in the JSON string.
 * For example, `player` as an argument instead of `playerName` would cause FastJSON to look for the string "player"
 * instead of "playerName", therefore breaking consecutive serializations and de-serializations of the same object.
 * <p>
 * Hence, since the constructor would also anyway just fill in the values in the two fields and nothing more, let's
 * avoid redefining the 2-argument constructor.
 */
@Value
public class Result implements Comparable<Result> {

    String playerName;
    int score;

    @Override
    public int compareTo(Result r) {
        return this.score - r.getScore();
    }
}
