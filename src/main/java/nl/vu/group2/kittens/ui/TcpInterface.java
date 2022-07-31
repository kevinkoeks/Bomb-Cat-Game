package nl.vu.group2.kittens.ui;

import lombok.extern.slf4j.Slf4j;
import nl.vu.group2.kittens.model.GameEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class TcpInterface implements UserInterface {

    public static final String QUERY_MARKER = "QUERY: ";
    public static final String NEWLINE_REPLACEMENT = ";;;";

    private final Socket socket;
    private OutputStream writer;
    private BufferedReader reader;

    public TcpInterface(Socket socket) {
        this.socket = socket;
        try {
            this.writer = this.socket.getOutputStream();
            final InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            reader = new BufferedReader(inputStreamReader);
        } catch (IOException e) {
            handleNetworkError(e, "Failed to create NetworkInterface");
        }
    }

    @Override
    public void notify(GameEvent event) {
        send(event.toString());
    }

    @Override
    public String query(String prompt) {
        send(QUERY_MARKER + prompt);
        return receive(prompt);
    }

    // NOTE: Timeout is ignored over the network
    @Override
    public int queryNumber(String prompt, int min, int max, Duration timeout, int defaultValue) {
        int value = min - 1;
        while (value < min || value > max) {
            final String result = query(prompt);
            Integer parsedNumber = toNumber(result);
            if (parsedNumber != null) {
                value = parsedNumber;
            }
        }
        return value;
    }

    @Override
    public List<Integer> queryNumbers(String prompt, int nCards) {
        Collection<Integer> values = Set.of();
        while (values.isEmpty()) {
            final String result = query(prompt);
            Set<Integer> numbers = Arrays.stream(result.split(","))
                                         .map(String::trim)
                                         .map(TcpInterface::toNumber)
                                         .collect(Collectors.toSet());
            boolean hasOnlyValidNumbers = numbers.stream().noneMatch(isInvalidNumber(nCards));
            if (hasOnlyValidNumbers) {
                values = numbers;
            }
        }
        return new ArrayList<>(values);
    }

    @Override
    public void close() {
        try {
            this.writer.close();
            this.reader.close();
            this.socket.close();
        } catch (IOException e) {
            log.error("Error while closing I/O TCP connections", e);
        }
    }

    private static Integer toNumber(String str) {
        if (StringUtils.isNumeric(str)) {
            return Integer.parseInt(str);
        }
        return null;
    }

    private Predicate<Integer> isInvalidNumber(int max) {
        return n -> n == null || n < 0 || n > max;
    }

    // <editor-fold desc="helper methods">
    private void send(String text) {
        try {
            text = text.replace("\n", NEWLINE_REPLACEMENT);
            writer.write((text + "\n").getBytes(StandardCharsets.UTF_8));
            writer.flush();
        } catch (IOException e) {
            handleNetworkError(e, "Failed to produce %s to the network", text);
        }
    }

    private String receive(String initialPrompt) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return handleNetworkError(e, "Failed to read reply for %s from the network", initialPrompt);
        }
    }

    // The type parameter is here only to make the compiler happy: we know that the method will throw an exception and
    // that this will never return anything, but the `receive` method doesn't know...
    private <T> T handleNetworkError(IOException e, String errorMessage, String... args) {
        final String formattedMessage = String.format(errorMessage, (Object[]) args);
        log.error(formattedMessage, e);
        throw new NetworkException(formattedMessage);
    }
    // </editor-fold>

    /**
     * Just to have a dedicated type/wrapper for networking exceptions.
     */
    private static class NetworkException extends RuntimeException {

        public NetworkException(String errorMessage) {
            super(errorMessage);
        }
    }
}
