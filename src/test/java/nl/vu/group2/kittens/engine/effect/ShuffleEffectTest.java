package nl.vu.group2.kittens.engine.effect;

import nl.vu.group2.kittens.model.GameEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

class ShuffleEffectTest extends ComboEffectTestBase {

    @Override
    protected ComboEffect createEffectUnderTest() {
        return new ShuffleEffect();
    }

    @Test
    void shuffleDoesNotModifyTurns() {
        playEffectUnderTest(playerA);
        playTurn(playerA);
        playTurn(playerB);
    }

    @Test
    void shuffleNotifiesAllPlayers() {
        playEffectUnderTest(playerA);
        verify(playerA).onEvent(any(GameEvent.class));
        verify(playerB).onEvent(any(GameEvent.class));
        playTurn(playerA);
        playTurn(playerB);
    }
}
