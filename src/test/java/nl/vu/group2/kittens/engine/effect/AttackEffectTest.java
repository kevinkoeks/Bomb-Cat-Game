package nl.vu.group2.kittens.engine.effect;

import nl.vu.group2.kittens.engine.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link AttackEffect}.
 * If you're a reviewer, please ignore this class.
 */
class AttackEffectTest extends ComboEffectTestBase {

    @Override
    protected ComboEffect createEffectUnderTest() {
        return new AttackEffect();
    }

    @Test
    void simpleAttackScenario() {
        playEffectUnderTest(playerA);
        for (int i = 0; i < 2; i++) {
            playTurn(playerB);
        }
        playTurn(playerA);
        playTurn(playerB);
        playTurn(playerA);
    }

    @Test
    void doubleAttackScenario() {
        playEffectUnderTest(playerA);
        playEffectUnderTest(playerB);
        for (int i = 0; i < 4; i++) {
            playTurn(playerA);
        }
        playTurn(playerB);
        playTurn(playerA);
        playTurn(playerB);
    }

    @Test
    void tripleAttackScenario() {
        playEffectUnderTest(playerA);
        playEffectUnderTest(playerB);
        playEffectUnderTest(playerA);
        for (int i = 0; i < 6; i++) {
            playTurn(playerB);
        }
        playTurn(playerA);
        playTurn(playerB);
        playTurn(playerA);
    }

    @Test
    void doubleAttackWithTurnInBetweenScenario() {
        playEffectUnderTest(playerA);
        playTurn(playerB);
        playEffectUnderTest(playerB);
        for (int i = 0; i < 2; i++) {
            playTurn(playerA);
        }
        playTurn(playerB);
        playTurn(playerA);
        playTurn(playerB);
    }

    @Test
    void doubleAttackWithOtherEffectInBetweenScenario() {
        playEffectUnderTest(playerA);
        playAnotherEffect(playerB);
        playEffectUnderTest(playerB);
        for (int i = 0; i < 2; i++) {
            playTurn(playerA);
        }
        playTurn(playerB);
        playTurn(playerA);
        playTurn(playerB);
    }

    private void playAnotherEffect(Player player) {
        assertSame(player, state.getCurrentPlayer());
        state.addEffectToHistory(new SkipEffect());
    }
}