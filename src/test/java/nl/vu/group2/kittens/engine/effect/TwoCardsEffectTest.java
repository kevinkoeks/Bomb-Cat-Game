package nl.vu.group2.kittens.engine.effect;

import nl.vu.group2.kittens.engine.player.Player;
import nl.vu.group2.kittens.model.Card;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TwoCardsEffect}.
 * If you're a reviewer, please ignore this class.
 */
class TwoCardsEffectTest extends ComboEffectTestBase {

    @Override
    protected ComboEffect createEffectUnderTest() {
        return new TwoCardsEffect();
    }

    @Test
    void simpleTwoCardsEffectScenario() {
        when(playerA.selectPlayer(anyCollectionOf(Player.class)))
                .thenReturn(playerB);
        when(playerA.selectCardOf(playerB))
                .thenReturn(Optional.of(Card.ATTACK));

        playEffectUnderTest(playerA);

        verify(playerB).removeCardFromHand(Card.ATTACK);
        verify(playerA).addCard(Card.ATTACK);
        playTurn(playerA); // check if still playerA turn
        playTurn(playerB); // check if turns are not modified
    }

    @Test
    void opponentWithNoCardsScenario() {
        when(playerA.selectPlayer(anyCollectionOf(Player.class)))
                .thenReturn(playerB);
        when(playerA.selectCardOf(playerB))
                .thenReturn(Optional.empty());

        playEffectUnderTest(playerA);

        verify(playerB, never()).removeCardFromHand(any(Card.class));
        verify(playerA, never()).addCard(any(Card.class));
        playTurn(playerA);
        playTurn(playerB);
    }
}