package nl.vu.group2.kittens.engine.effect;

import nl.vu.group2.kittens.model.Card;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ThreeCardsEffectTest extends ComboEffectTestBase {

    @Override
    protected ComboEffect createEffectUnderTest() {
        return new ThreeCardsEffect();
    }

    @Test
    void simpleThreeCardsEffectScenario() {
        when(playerA.selectCardType()).thenReturn(Card.ATTACK);
        when(playerA.selectPlayer(any(Collection.class))).thenReturn(playerB);
        when(playerB.getHand()).thenReturn(List.of(Card.ATTACK));

        playEffectUnderTest(playerA);

        verify(playerB).removeCardFromHand(Card.ATTACK);
        verify(playerA).addCard(Card.ATTACK);
        playTurn(playerA);
        playTurn(playerB);
    }

    @Test
    void opponentDoesNotHaveRequestedCardScenario() {
        when(playerA.selectCardType()).thenReturn(Card.ATTACK);
        when(playerA.selectPlayer(any(Collection.class))).thenReturn(playerB);
        when(playerB.getHand()).thenReturn(List.of(Card.SKIP));

        playEffectUnderTest(playerA);

        verify(playerB, never()).removeCardFromHand(any(Card.class));
        verify(playerA, never()).addCard(any(Card.class));
        playTurn(playerA);
        playTurn(playerB);
    }
}
