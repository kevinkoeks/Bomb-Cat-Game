package nl.vu.group2.kittens.engine.effect;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SkipEffect}.
 * If you're a reviewer, please ignore this class.
 */
class SkipEffectTest extends ComboEffectTestBase {

    @Override
    protected ComboEffect createEffectUnderTest() {
        return new SkipEffect();
    }

    @Test
    void simpleSkipScenario() {
        playEffectUnderTest(playerA);
        playTurn(playerB);
        playTurn(playerA);
        playTurn(playerB);
        playTurn(playerA);
    }

    @Test
    void doubleSkipScenario() {
        playEffectUnderTest(playerA);
        playEffectUnderTest(playerB);

        playTurn(playerA);
        playTurn(playerB);
        playTurn(playerA);
    }
}
