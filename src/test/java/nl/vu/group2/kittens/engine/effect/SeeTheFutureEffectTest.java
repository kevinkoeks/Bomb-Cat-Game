package nl.vu.group2.kittens.engine.effect;

import nl.vu.group2.kittens.engine.GameState;
import nl.vu.group2.kittens.engine.player.Player;
import nl.vu.group2.kittens.model.Card;
import nl.vu.group2.kittens.model.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.vu.group2.kittens.model.GameEvent.infoEvent;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SeeTheFutureEffect}.
 * If you're a reviewer, please ignore this class.
 */
class SeeTheFutureEffectTest {

    private static final Player PLAYER = mock(Player.class);
    private static final Player OPPONENT = mock(Player.class);
    private static final List<Player> ALL_PLAYERS = List.of(PLAYER, OPPONENT);
    private static final String PLAYER_ID = "id";

    // stateless, so no need to create multiple instances
    private static final SeeTheFutureEffect SEE_THE_FUTURE = new SeeTheFutureEffect();

    @BeforeEach
    void initEach() {
        reset(PLAYER);
        reset(OPPONENT);
        when(PLAYER.getId()).thenReturn(PLAYER_ID);
    }

    @Test
    void deckWithMoreThanThreeCards() {
        final List<Card> cards = List.of(Card.NOPE, Card.EXPLODING_KITTEN, Card.DEFUSE, Card.BEARD_CAT);
        final GameState state = new GameState(ALL_PLAYERS, Deck.of(cards));
        SEE_THE_FUTURE.apply(state);
        verify(PLAYER).onEvent(infoEvent("The top 3 cards on the deck are NOPE, EXPLODING_KITTEN, DEFUSE"));
        verify(OPPONENT).onEvent(infoEvent(PLAYER_ID + " saw the future..."));
    }

    @Test
    void deckWithThreeCards() {
        final List<Card> cards = List.of(Card.NOPE, Card.EXPLODING_KITTEN, Card.BEARD_CAT);
        final GameState state = new GameState(ALL_PLAYERS, Deck.of(cards));
        SEE_THE_FUTURE.apply(state);
        verify(PLAYER).onEvent(infoEvent("The top 3 cards on the deck are NOPE, EXPLODING_KITTEN, BEARD_CAT"));
        verify(OPPONENT).onEvent(infoEvent(PLAYER_ID + " saw the future..."));
    }

    @Test
    void deckWithTwoCards() {
        final List<Card> cards = List.of(Card.EXPLODING_KITTEN, Card.ATTACK);
        final GameState state = new GameState(ALL_PLAYERS, Deck.of(cards));
        SEE_THE_FUTURE.apply(state);
        verify(PLAYER).onEvent(infoEvent("The top 2 cards on the deck are EXPLODING_KITTEN, ATTACK"));
        verify(OPPONENT).onEvent(infoEvent(PLAYER_ID + " saw the future..."));
    }

    @Test
    void deckWithOneCard() {
        final List<Card> cards = List.of(Card.EXPLODING_KITTEN);
        final GameState state = new GameState(ALL_PLAYERS, Deck.of(cards));
        SEE_THE_FUTURE.apply(state);
        verify(PLAYER).onEvent(infoEvent("The top card on the deck is EXPLODING_KITTEN"));
        verify(OPPONENT).onEvent(infoEvent(PLAYER_ID + " saw the future..."));
    }

    // no test for empty deck: in the real game, this situation cannot happen
}