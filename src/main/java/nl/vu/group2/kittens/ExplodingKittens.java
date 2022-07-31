package nl.vu.group2.kittens;

import lombok.extern.slf4j.Slf4j;
import nl.vu.group2.kittens.engine.GameRunner;
import nl.vu.group2.kittens.ui.CliInterface;

@Slf4j
public class ExplodingKittens {

    public static void main(String[] args) {
        log.info("Starting Exploding Kittens...");
        runWithCli();
        log.info("Exploding Kittens game ended");
    }

    private static void runWithCli() {
        boolean isPlayerPlaying = true;
        final CliInterface ui = new CliInterface();
        while (isPlayerPlaying) {
            new GameRunner(ui).start();
            final String anotherGameReply = ui.query("Do you want to play another game? (y/N)");
            isPlayerPlaying = anotherGameReply.equalsIgnoreCase("y");
        }
        ui.close();
    }
}
