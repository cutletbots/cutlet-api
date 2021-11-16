package ru.blc.cutlet.api.console;

import org.slf4j.Logger;
import ru.blc.cutlet.api.Cutlet;
import ru.blc.cutlet.api.bot.Bot;
import ru.blc.cutlet.api.bot.BotManager;
import ru.blc.cutlet.api.command.Command;
import ru.blc.cutlet.api.command.sender.CommandSender;
import ru.blc.cutlet.api.command.sender.ConsoleCommandSender;
import ru.blc.cutlet.api.console.command.StopCommand;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Console implements ConsoleCommandSender {

    private static final AtomicBoolean state = new AtomicBoolean(false);
    private static final Logger LOG = Cutlet.instance().getLogger();

    private final Scanner scanner;
    private boolean stopped = false;
    private final BotManager botManager;

    public Console() {
        LOG.info("Enabling console");
        if (state.get()) {
            throw new IllegalStateException("Console already started!");
        } else {
            if (!state.compareAndSet(false, true)) {
                throw new IllegalStateException("Console already started! Is there multi console starters?");
            }
        }
        Cutlet c = Cutlet.instance();
        scanner = new Scanner(System.in);
        Thread listener = new Thread(() -> {
            while (!stopped) {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    input(line);
                }
            }
            LOG.info("Console stopped. Cutlet not controlled by console now :c");
        });
        botManager = c.getBotManager();
        listener.setDaemon(false);
        listener.setName("Console");
        listener.start();
        c.getBotManager().registerCommand(null, new StopCommand());
        LOG.info("Console enabled. Wanna cutlet, bro?");
    }

    public void disable() {
        LOG.info("Disabling console");
        stopped = true;
    }

    protected void input(String input) {
        String[] in = input.split(" ");
        String[] cmd = in[0].split(":");
        Bot owner = null;
        String cmdN = cmd[0];
        if (cmd.length == 2) {
            cmdN = cmd[1];
            owner = botManager.getBot(cmd[0]);
        }
        Command command = botManager.getCommand(cmdN, owner);
        if (command != null) {
            try {
                command.dispatch(this, in[0], Arrays.copyOfRange(in, 1, in.length));
            } catch (Exception e) {
                LOG.error("Error while dispatching command", e);
            }
        } else {
            sendMessage(String.format(botManager.getCutlet().getTranslation("unknown_command"), in[0]));
        }
    }

    @Override
    public Bot getBot() {
        return null;
    }

    @Override
    public CommandSender getPmSender() {
        return this;
    }

    @Override
    public boolean isDeleteIfPM() {
        return false;
    }

    @Override
    public void setDeleteIfPM(boolean deleteIfPM) {
    }

    @Override
    public void sendMessage(String message) {
        LOG.info(message);
    }

    @Override
    public void sendAndDeleteMessage(String message) {
        sendMessage(message);
    }

    @Override
    public void sendMessage(Object message) {
        LOG.info("Hard message for you: {}", message);
    }

    @Override
    public void sendAndDeleteMessage(Object message) {
        sendMessage(message);
    }
}
