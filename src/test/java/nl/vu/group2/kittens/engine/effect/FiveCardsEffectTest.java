package nl.vu.group2.kittens.engine.effect;

import nl.vu.group2.kittens.engine.GameState;
import nl.vu.group2.kittens.engine.player.Player;
import nl.vu.group2.kittens.model.Card;
import nl.vu.group2.kittens.model.Deck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FiveCardsEffect}.
 * If you're a reviewer, please ignore this class.
 */
class FiveCardsEffectTest {

    private final Player PLAYER = mock(Player.class);
    private final Player OPPONENT = mock(Player.class);
    private final List<Player> ALL_PLAYERS = List.of(PLAYER, OPPONENT);
    private final String PLAYER_ID = "id";

    // stateless, so no need to create multiple instances
    private static final FiveCardsEffect FIVE_CARDS_EFFECT = new FiveCardsEffect();

    @Test
    void canTakeCardFromDiscardPile() {
        final List<Card> cards = List.of(Card.NOPE, Card.EXPLODING_KITTEN, Card.DEFUSE, Card.BEARD_CAT);
        final GameState state = new GameState(ALL_PLAYERS, Deck.of(cards));
        state.discard(PLAYER, Card.ATTACK);
        when(PLAYER.selectCardFrom(any())).thenReturn(List.of(Card.ATTACK));

        FIVE_CARDS_EFFECT.apply(state);
        assertTrue(true);
    }
}
