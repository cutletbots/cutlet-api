package ru.blc.cutlet.api.command.sender;

import ru.blc.cutlet.api.bot.Bot;
import ru.blc.cutlet.api.command.Messenger;

public interface CommandSender {

    Bot getBot();

    boolean hasPermission(String permission);

    default boolean isConsole() {
        return false;
    }

    CommandSender getPmSender();

    boolean isDeleteIfPM();

    void setDeleteIfPM(boolean deleteIfPM);

    String getName();

    void sendMessage(String message);

    void sendAndDeleteMessage(String message);

    void sendMessage(Object message);

    void sendAndDeleteMessage(Object message);

    Messenger getMessenger();

    DialogType getDialogType();
}
