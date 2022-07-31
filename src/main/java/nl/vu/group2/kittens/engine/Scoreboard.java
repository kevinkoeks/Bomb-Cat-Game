package nl.vu.group2.kittens.engine;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class Scoreboard {

    private static final Path SCOREBOARD_FILE_PATH = Paths.get("scoreboard.json");
    private final List<Result> results = new ArrayList<>();

    public Scoreboard() {
        log.info("Creating new scoreboard");
        try {
            File scoreboard = new File(SCOREBOARD_FILE_PATH.toString());
            deleteScoreboard();
            if (scoreboard.createNewFile()) {
                log.info("Successfully created scoreboard");
            }
        } catch (IOException e) {
            log.warn("Error creating scoreboard", e);
        }
    }

    public void update() {
        final List<Result> resultsCopy = new ArrayList<>(results);
        Collections.sort(resultsCopy);
        try {
            final String jsonResults = JSON.toJSONString(resultsCopy);
            Files.write(SCOREBOARD_FILE_PATH, jsonResults.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Failed to update scoreboard", e);
            throw new IllegalStateException(e);
        }
    }

    public void add(String player, int score) {
        results.add(new Result(player, score));
    }

    private void deleteScoreboard() throws IOException {
        Files.deleteIfExists(SCOREBOARD_FILE_PATH);
    }

    @Override
    public String toString() {
        StringBuilder scoreboardString = new StringBuilder();
        scoreboardString.append("<---- SCOREBOARD ---->").append("\n");
        for (Result result : results) {
            scoreboardString.append(result.getScore()).append("\t\t").append(result.getPlayerName()).append("\n");
        }
        scoreboardString.append("<---- ********** ---->").append("\n");
        return scoreboardString.toString();
    }
}
