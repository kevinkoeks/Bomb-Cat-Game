package nl.vu.group2.kittens.network;

import lombok.extern.slf4j.Slf4j;
import nl.vu.group2.kittens.engine.GameRunner;
import nl.vu.group2.kittens.engine.player.HumanPlayer;
import nl.vu.group2.kittens.engine.player.Player;
import nl.vu.group2.kittens.ui.TcpInterface;
import nl.vu.group2.kittens.ui.UserInterface;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static nl.vu.group2.kittens.model.GameEvent.infoEvent;

@Slf4j
public class Network {

    public static final int PORT = 8080;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(GameRunner.MAX_PLAYER_COUNT);

    private static final Network INSTANCE = new Network();

    public static Network getInstance() {
        return INSTANCE;
    }

    public List<Player> registerPlayers(int playersCount) {
        final Queue<Player> players = new ArrayBlockingQueue<>(playersCount);
        while (players.size() < playersCount) {
            try (ServerSocket serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName(null))) {
                serverSocket.setSoTimeout(2_000);
                log.info("Listening on {}", PORT);
                Socket sock = serverSocket.accept();
                log.info("New connection established.");
                EXECUTOR.submit(() -> players.offer(createRemotePlayer(sock)));
            } catch (SocketTimeoutException e) {
                // ignored, just polling the players' size
            } catch (IOException e) {
                log.error("Error while listening for new connections.", e);
                throw new IllegalStateException(e);
            }
        }
        return List.copyOf(players);
    }

    public Socket joinGame() throws IOException {
        return new Socket(InetAddress.getLoopbackAddress(), PORT);
    }

    private static Player createRemotePlayer(Socket socket) {
        final UserInterface ui = new TcpInterface(socket);
        final String id = ui.query("Enter your name");
        log.info("{} joined the game.", id);
        ui.notify(infoEvent("Welcome " + id));
        return new HumanPlayer(id, ui);
    }
}
