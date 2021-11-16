package ru.blc.cutlet.api.command;

import ru.blc.cutlet.api.command.sender.CommandSender;

public interface CommandExecutor {

    void onCommand(Command command, CommandSender sender, String alias, String[] args);
}
