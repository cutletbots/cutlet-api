package ru.blc.cutlet.api;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.blc.cutlet.api.bot.Bot;
import ru.blc.cutlet.api.bot.BotManager;
import ru.blc.cutlet.api.command.Command;
import ru.blc.cutlet.api.command.sender.CommandSender;
import ru.blc.cutlet.api.console.Console;
import ru.blc.cutlet.api.event.command.CommandPreprocessEvent;
import ru.blc.cutlet.api.module.Module;
import ru.blc.cutlet.api.module.ModuleLoader;
import ru.blc.cutlet.api.timer.Timer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

public class Cutlet {

    private static Cutlet instance;

    public static Cutlet instance() {
        return instance;
    }

    private boolean running;

    private final Logger logger = LoggerFactory.getLogger("Cutlet");
    private final Console console;
    private final Properties translations = new Properties();

    @Getter
    private final File botsFolder = new File("bots");
    @Getter
    private final File modulesFolder = new File("modules");
    private final BotManager botManager;
    private final ModuleLoader moduleLoader;
    @Getter
    private Timer timer;


    public Cutlet() {
        instance = this;
        this.moduleLoader = new ModuleLoader(this);
        this.botManager = new BotManager(this);
        this.console = new Console();
    }

    public void start() {
        if (this.isRunning()) throw new IllegalStateException("Cutlet already running");
        this.running = true;
        this.timer = new Timer(this);
        InputStream in = getResourceAsStream("/messages.properties");
        if (in == null) {
            this.getLogger().error("Messages properties not loaded! Translations would not work. Update cutlet or contact with developer.");
        } else {
            try {
                translations.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (IOException e) {
                this.getLogger().error("Could not load messages properties", e);
            }
        }
        if (!this.modulesFolder.exists()) {
            if (!this.modulesFolder.mkdirs()) {
                getLogger().error("Could not create modules folder!");
                shutdown();
            }
        }
        getModuleLoader().detectModules(this.modulesFolder);
        getModuleLoader().loadModules();
        getModuleLoader().enableModules();
        if (!this.botsFolder.exists()) {
            if (!this.botsFolder.mkdirs()) {
                getLogger().error("Could not create bots folder!");
                shutdown();
            }
        }
        getBotManager().detectBots(botsFolder);
        getBotManager().loadBots();
        getBotManager().enableBots();
    }

    public void shutdown() {
        getLogger().info("Disabling cutlet");
        getBotManager().disableBots();
        getModuleLoader().disableModules();
        getConsole().disable();
        running = false;
    }

    public InputStream getResourceAsStream(@NotNull String name) {
        Preconditions.checkArgument(!name.isEmpty(), "name");
        getLogger().debug("Loading cutlet resource {}", name);
        InputStream in = null;
        File resource = new File(name);
        boolean founded = resource.exists() && resource.isFile();
        if (!founded) {
            getLogger().debug("No file, try class {}", name);
            try {
                in = this.getClass().getResourceAsStream(name);
            } catch (Exception e) {
                this.getLogger().error("Could not load resource" + name, e);
            }
        } else {
            try {
                in = new FileInputStream(resource);
            } catch (FileNotFoundException e) {
                this.getLogger().error("Could not load resource " + name, e);
            }
        }
        getLogger().debug("Resource for {} is {}", name, in);
        return in;
    }

    public String getTranslation(String key) {
        return translations.getProperty(key, "No translation for key " + key);
    }

    public boolean isRunning() {
        return running;
    }

    public Logger getLogger() {
        return logger;
    }

    public Console getConsole() {
        return console;
    }

    public BotManager getBotManager() {
        return botManager;
    }

    /**
     * @deprecated use {@link Cutlet#getCommand(String, Bot)}
     */
    @Deprecated
    public Command getCommand(String alias) {
        return getBotManager().getCommand(alias);
    }

    public Command getCommand(String alias, Bot owner) {
        return getBotManager().getCommand(alias, owner);
    }

    public boolean dispatchCommand(String command, CommandSender sender) {
        return dispatchCommand(command, sender, false);
    }

    public boolean dispatchCommand(String command, CommandSender sender, boolean answerIfUnknown) {
        String[] args = command.split(" ");
        Command c = getCommand(args[0], sender.getBot());
        if (c == null) {
            if (answerIfUnknown) sender.sendMessage(String.format(getTranslation("unknown_command"), args[0]));
            return answerIfUnknown;
        }
        if (!c.getDialogType().allows(sender.getDialogType())) {
            switch (c.getDialogType()) {
                case PRIVATE_MESSAGE:
                    sender.sendMessage(getTranslation("command_only_pm"));
                    return true;
                case CONVERSATION:
                    sender.sendMessage(getTranslation("command_only_conversation"));
                    return true;
                case ALL:
                    sender.sendMessage(getTranslation("command_error"));
                    getLogger().error("Command has dialog type ALL, but sender with type {} not allowed", sender.getDialogType());
                    return true;
            }
        }
        CommandPreprocessEvent e = new CommandPreprocessEvent(sender, c, command);
        getBotManager().callEvent(e, b -> b == c.getOwner());
        if (e.isCancelled()) {
            return true;
        }
        Logger log = getLogger();
        if (sender.getBot() != null) {
            log = sender.getBot().getLogger();
        }
        log.info("{} dispatched command {} with text {}", sender.getName(), c.getName(), command);
        c.dispatch(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    public ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    /**
     * returns module by name
     *
     * @param name module name
     * @return module or null if there is no matching module
     */
    public @Nullable Module getModule(String name) {
        return getModuleLoader().getModule(name);
    }

    /**
     * returns module by class
     *
     * @param clazz module class
     * @param <T>   module type
     * @return module or null if there is no matching module
     */
    public <T extends Module> T getModule(Class<T> clazz) {
        return getModuleLoader().getModule(clazz);
    }
}
