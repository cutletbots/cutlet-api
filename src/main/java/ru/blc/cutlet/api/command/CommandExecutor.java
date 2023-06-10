package ru.blc.cutlet.api.command;

import org.jetbrains.annotations.NotNull;
import ru.blc.cutlet.api.command.sender.CommandSender;

/**
 * Simple interface for implementing command logic
 */
public interface CommandExecutor {

    /**
     * For default Command implementation calls after all checks (permission, messenger etc.) has been passed
     * @param command dispatched command
     * @param sender command sender executed this command
     * @param alias used command alias. command name also can be here
     * @param args command args
     */
    void onCommand(@NotNull Command command,
                   @NotNull CommandSender sender,
                   @NotNull String alias,
                   @NotNull String @NotNull[] args);
}
