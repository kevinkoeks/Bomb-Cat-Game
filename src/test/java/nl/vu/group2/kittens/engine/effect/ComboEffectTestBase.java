package nl.vu.group2.kittens.engine.effect;

import nl.vu.group2.kittens.engine.GameState;
import nl.vu.group2.kittens.engine.player.Player;
import nl.vu.group2.kittens.model.Card;
import nl.vu.group2.kittens.model.Deck;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 * Base class for tests on @{@link ComboEffect} children.
 * If you're a reviewer, please ignore this class since it belongs to the test suite.
 */
public abstract class ComboEffectTestBase {

    protected Player playerA;
    protected Player playerB;
    protected GameState state;
    protected ComboEffect effect;

    @BeforeEach
    void init() {
        playerA = mock(Player.class);
        playerB = mock(Player.class);
        final List<Player> players = List.of(playerA, playerB);
        List<Card> enoughSkipCards = IntStream.range(0, 100).mapToObj(i -> Card.SKIP).collect(Collectors.toList());
        List<Card> fakeDeckCards = Stream.concat(
                enoughSkipCards.stream(),
                Stream.of(Card.EXPLODING_KITTEN)
        ).collect(Collectors.toList());
        final Deck deck = Deck.of(fakeDeckCards);
        state = new GameState(players, deck);
        effect = createEffectUnderTest();
    }

    protected final void playEffectUnderTest(Player player) {
        assertSame(player, state.getCurrentPlayer());
        effect.apply(state);
        // just not to reuse the same reference over and over again, new AttackEffect()
        state.addEffectToHistory(effect);
    }

    /**
     * At each end (draw) of every turn, a no-op effect gets added by the game.
     * See NoOperationEffect there.
     * To keep things clean, it's better not to increase the visibility of that class, so here we just use a lambda
     * which, incidentally, happens to have the same behaviour ;)
     **/
    protected final void playTurn(Player player) {
        assertSame(player, state.getCurrentPlayer());
        state.nextTurn();
        final ComboEffect noOperationEffect = ignoredState -> { /* NO-OP */ };
        state.addEffectToHistory(noOperationEffect);
    }

    protected abstract ComboEffect createEffectUnderTest();
}
