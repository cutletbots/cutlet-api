package ru.blc.cutlet.api.command.sender;

import ru.blc.cutlet.api.command.Messenger;

/**
 * For Cutlet console
 */
public interface ConsoleCommandSender extends CommandSender {

    @Override
    default boolean hasPermission(String permission) {
        return true;
    }

    @Override
    default boolean isConsole() {
        return true;
    }

    @Override
    default String getName() {
        return "Console";
    }

    @Override
    default Messenger getMessenger() {
        return null;
    }

    @Override
    default DialogType getDialogType() {
        return DialogType.ALL;
    }
}
