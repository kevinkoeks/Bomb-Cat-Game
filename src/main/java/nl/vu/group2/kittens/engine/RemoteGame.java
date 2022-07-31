package nl.vu.group2.kittens.engine;

import lombok.extern.slf4j.Slf4j;
import nl.vu.group2.kittens.model.GameEvent;
import nl.vu.group2.kittens.network.Network;
import nl.vu.group2.kittens.ui.TcpInterface;
import nl.vu.group2.kittens.ui.UserInterface;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static nl.vu.group2.kittens.model.GameEvent.errorEvent;

@Slf4j
class RemoteGame implements Game {

    public static final String GAME_ENDED_MESSAGE = GameEvent.EventType.SYSTEM + Game.GAME_ENDED_MARKER;
    private final Socket socket;
    private final UserInterface ui;

    public RemoteGame(UserInterface ui) {
        this.ui = ui;
        try {
            socket = Network.getInstance().joinGame();
        } catch (IOException e) {
            final String errorMessage = "Failed to connect to the game";
            log.error(errorMessage, e);
            ui.notify(errorEvent(errorMessage));
            throw new IllegalStateException(errorMessage, e);
        }
    }

    @Override
    public void run() {
        final Pair<BufferedReader, PrintWriter> ioChannels = openIoChannels();
        playGame(ioChannels);
        closeConnections(ioChannels);
    }

    private Pair<BufferedReader, PrintWriter> openIoChannels() {
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            return Pair.of(in, out);
        } catch (IOException e) {
            final String errorMessage = "Error while opening the I/O channels over the network";
            log.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    private void playGame(Pair<BufferedReader, PrintWriter> ioChannels) {
        final BufferedReader in = ioChannels.getLeft();
        final PrintWriter out = ioChannels.getRight();
        try {
            String nextMessage = "";
            while (!nextMessage.equals(GAME_ENDED_MESSAGE)) {
                nextMessage = in.readLine();
                final String reply = handleMessage(nextMessage);
                if (reply != null) {
                    out.println(reply);
                }
            }
        } catch (IOException e) {
            final String errorMessage = "Error while running remote game";
            log.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    private String handleMessage(String nextMessage) {
        nextMessage = nextMessage.replace(TcpInterface.NEWLINE_REPLACEMENT, "\n");
        if (nextMessage.startsWith(TcpInterface.QUERY_MARKER)) {
            return ui.query(nextMessage.substring(TcpInterface.QUERY_MARKER.length()));
        }
        ui.notify(GameEvent.of(nextMessage));
        return null;
    }

    private void closeConnections(Pair<BufferedReader, PrintWriter> ioChannels) {
        try {
            ioChannels.getLeft().close();
            ioChannels.getRight().close();
            socket.close();
        } catch (IOException e) {
            final String errorMessage = "Error while closing TCP connection";
            log.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
    }
}
