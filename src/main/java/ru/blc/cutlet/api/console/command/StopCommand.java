package ru.blc.cutlet.api.console.command;

import ru.blc.cutlet.api.Cutlet;
import ru.blc.cutlet.api.command.sender.DialogType;

public class StopCommand extends ConsoleCommand {
    public StopCommand() {
        super(DialogType.ALL, "stop", "command.stop", "stops cutlet", "");
        setCommandExecutor((command, sender, alias, args) -> Cutlet.instance().shutdown());
    }
}
