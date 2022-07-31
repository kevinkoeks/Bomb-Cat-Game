package nl.vu.group2.kittens.ui;

import nl.vu.group2.kittens.model.GameEvent;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CliInterface implements UserInterface {

    private static final String RESET_ON_TIMEOUT_BOOKMARK = "timeout_bookmark";
    private final TextTerminal<?> terminal;
    private final TextIO textIO;
    private static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1);

    public CliInterface() {
        this.textIO = TextIoFactory.getTextIO();
        this.terminal = this.textIO.getTextTerminal();
        this.terminal.getProperties().setPromptColor(Color.green);
        this.terminal.getProperties().setInputColor(Color.white);
    }

    @Override
    public void notify(GameEvent event) {
        final String text = event.getDescription();
        final Color color = toColor(event.getType());
        textIO.getTextTerminal()
              .executeWithPropertiesConfigurator(
                      props -> props.setPromptColor(color),
                      term -> term.println(text)
              );
    }

    @Override
    public String query(String request) {
        return textIO.newStringInputReader()
                     .withMinLength(0)
                     .read(request + "\n>");
    }

    @Override
    public int queryNumber(String prompt, int min, int max, Duration timeout, int defaultValue) {
        ScheduledFuture<?> schedule = null;
        terminal.print(prompt + "\n>");
        terminal.setBookmark(RESET_ON_TIMEOUT_BOOKMARK);
        if (timeout != null) {
            schedule = SCHEDULER.schedule(() -> {
                terminal.resetToBookmark(RESET_ON_TIMEOUT_BOOKMARK);
                terminal.println(" " + defaultValue);
            }, timeout.getSeconds(), TimeUnit.SECONDS);
        }
        final Integer number = textIO.newIntInputReader()
                                     .withMinVal(min)
                                     .withMaxVal(max)
                                     .read();
        if (schedule != null) {
            schedule.cancel(true);
        }
        return number;
    }

    @Override
    public List<Integer> queryNumbers(String prompt, int nCards) {
        return textIO.newIntInputReader()
                     .withMinVal(0)
                     .withMaxVal(nCards)
                     .readList(prompt);
    }

    @Override
    public void close() {
        this.textIO.dispose();
    }

    private Color toColor(GameEvent.EventType eventType) {
        switch (eventType) {
            case INFO:
                return Color.green;
            case SYSTEM:
                return Color.orange;
            case ERROR:
                return Color.red;
            default:
                throw new UnsupportedOperationException("Unsupported event type " + eventType);
        }
    }
}
