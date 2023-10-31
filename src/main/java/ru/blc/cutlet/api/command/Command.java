package ru.blc.cutlet.api.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.blc.cutlet.api.Cutlet;
import ru.blc.cutlet.api.bot.Bot;
import ru.blc.cutlet.api.command.sender.CommandSender;
import ru.blc.cutlet.api.command.sender.DialogType;

import java.util.Arrays;
import java.util.List;

public class Command {

    /**
     * Default command executor<br/>
     * Just sends message "Not implemented yet"
     */
    public static final CommandExecutor DEFAULT_EXECUTOR = (command, sender, alias, args) -> sender.sendMessage("Not implemented yet");

    @NotNull
    private final String name;
    @NotNull
    private final String permission;
    @NotNull
    private final String description;
    @NotNull
    private final String usage;
    @NotNull
    private final DialogType dialogType;
    private final String[] aliases;
    private final Bot owner;
    private Messenger[] allowedMessengers;

    private CommandExecutor executor;

    /**
     * @param owner       owner of this command. Null for cutlet commands
     * @param dialogType  allowed dialog type
     * @param name        command name
     * @param permission  permission required to dispatch this command. Empty or null allows to use this command for everybody
     * @param description command description
     * @param usage       command usage
     * @param aliases     aliases
     */
    public Command(Bot owner, @NotNull DialogType dialogType, @NotNull String name, @Nullable String permission,
                   @Nullable String description, @Nullable String usage, String... aliases) {
        this.owner = owner;
        this.name = name;
        if (permission == null) permission = "";
        this.permission = permission;
        if (description == null) description = "";
        this.description = description;
        if (usage == null) usage = "";
        this.usage = usage;
        this.aliases = aliases;
        this.executor = DEFAULT_EXECUTOR;
        this.dialogType = dialogType;
    }

    /**
     * @return Owner of this command. Null for cutlet console commands
     */
    public @Nullable Bot getOwner() {
        return owner;
    }

    /**
     * Sets allowed messengers. If null or empty all messengers are allowed
     *
     * @param messengers allowed messengers
     */
    public void setAllowedMessengers(Messenger... messengers) {
        this.allowedMessengers = messengers;
    }

    /**
     * Check if command allowed in current messenger
     *
     * @param messenger messenger to check
     * @return true if command allowed in specified messenger
     */
    public boolean isAllowed(@Nullable Messenger messenger) {
        if (messenger == null) return true;
        if (allowedMessengers == null) return true;
        if (allowedMessengers.length == 0) return true;
        return Arrays.stream(allowedMessengers).anyMatch(m -> m == messenger);
    }

    /**
     * Dispatches current command<br>
     * Contains permissions, messengers and console checks.
     *
     * @param sender command sender
     * @param alias  alias
     * @param args   command arguments
     * @apiNote Do not Override this method for command logic. Use {@link CommandExecutor} instead
     */
    public void dispatch(@NotNull CommandSender sender, @NotNull String alias, @NotNull String @NotNull ... args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Cutlet.instance().getTranslation("no_permission"));
            return;
        }
        if (isOnlyConsole() && !sender.isConsole()) {
            sender.sendMessage(Cutlet.instance().getTranslation("only_console"));
            return;
        }
        if (!isAllowed(sender.getMessenger())) {
            sender.sendMessage(Cutlet.instance().getTranslation("unsupported_messenger"));
            return;
        }
        try {
            getCommandExecutor().onCommand(this, sender, alias, args);
        } catch (Exception e) {
            sender.sendMessage(Cutlet.instance().getTranslation("command_error"));
            owner.getLogger().error("Error while dispatching command " + getName(), e);
        }
    }

    /**
     * @return Command name
     */
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * @return Command description
     */
    public @NotNull String getDescription() {
        return description;
    }

    /**
     * Command help message. By default, similar to description.<br>
     * Override for custom
     *
     * @return Command help message
     */
    public @NotNull String getHelpMessage() {
        return getDescription();
    }

    /**
     * @return command usage
     */
    public @NotNull String getUsage() {
        return usage;
    }

    /**
     * @return Command aliases. Do not contains command name
     */
    @Unmodifiable
    public @NotNull List<@NotNull String> getAliases() {
        return Arrays.asList(aliases);
    }

    /**
     * @return Command permission
     */
    public @NotNull String getPermission() {
        return this.permission;
    }

    /**
     * Checks if command allowed to use only from console
     *
     * @return true if command allowed only from console
     * @apiNote In default implementation always return {@code false}. Overridden in {@link ru.blc.cutlet.api.console.command.ConsoleCommand}
     */
    public boolean isOnlyConsole() {
        return false;
    }

    /**
     * @return Dialog type in witch command can be executed
     */
    public @NotNull DialogType getDialogType() {
        return dialogType;
    }

    /**
     * Command executor is simple interface that implements command logic
     *
     * @return Command executor
     */
    public @NotNull CommandExecutor getCommandExecutor() {
        return executor;
    }

    /**
     * Command executor is simple interface that implements command logic
     *
     * @param executor executor for this command. {@code null} for {@link #DEFAULT_EXECUTOR}
     */
    public void setCommandExecutor(@Nullable CommandExecutor executor) {
        if (executor == null) executor = DEFAULT_EXECUTOR;
        this.executor = executor;
    }
}
