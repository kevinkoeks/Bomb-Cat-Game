package nl.vu.group2.kittens.engine.effect;

import lombok.Value;
import nl.vu.group2.kittens.engine.GameState;
import nl.vu.group2.kittens.engine.player.Player;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static nl.vu.group2.kittens.model.GameEvent.infoEvent;

@Value
class FavorEffect implements ComboEffect {

    @Override
    public void apply(GameState state) {
        final var currentPlayer = state.getCurrentPlayer();
        final var opponents = state.getActiveOpponents();
        final var targetOpponent = currentPlayer.selectPlayer(opponents);
        final var maybeCard = targetOpponent.selectCardOf(targetOpponent);

        if (maybeCard.isEmpty()) {
            state.onEvent(infoEvent(String.format("The target opponent PLAYER %s has no cards", targetOpponent.getId())));
            return;
        }

        final var card = maybeCard.get();
        state.transferCard(targetOpponent, currentPlayer, card);
        final String baseMessage = String.format("Card transferred from PLAYER %s to PLAYER %s", targetOpponent.getId(), currentPlayer.getId());

        // notify the players involved in the Favor effect
        final Set<Player> favorPlayers = Set.of(currentPlayer, targetOpponent);
        state.onEvent(infoEvent(baseMessage + ": " + card), favorPlayers);

        // notify the remainder of the players, *without* disclosing the card being transferred
        final Set<Player> otherPlayers = state.getPlayers()
                                              .stream()
                                              .filter(not(favorPlayers::contains))
                                              .collect(Collectors.toSet());
        state.onEvent(infoEvent(baseMessage), otherPlayers);
    }
}
