package ru.blc.cutlet.api.command.sender;

import ru.blc.cutlet.api.bot.Bot;
import ru.blc.cutlet.api.command.Messenger;

/**
 * Entity that can send command.<br/>
 * In cutlet, it's only {@link ConsoleCommandSender}, but modules and bots can create some more
 */
public interface CommandSender {

    /**
     * @return bot owning this command sender. For console is {@code null}
     */
    Bot getBot();

    /**
     * Permission check for this sender
     * @param permission permission to check
     * @return {@code true} if sender has this permission, otherwise {@code false}
     * @see ru.blc.cutlet.api.permission.PermissionCalculator#isPermissionAllows(String, String)
     */
    boolean hasPermission(String permission);

    /**
     * Checks if this sender is {@link ru.blc.cutlet.api.console.Console}
     * @return {@code true} if sender is console, otherwise {@code false}
     */
    default boolean isConsole() {
        return false;
    }

    /**
     * Custom Command sender for private dialog with this command sender.<br/>
     * If this sender already is in private dialog it <b>probably</b> returns same object
     * @return CommandSender for private dialog
     */
    CommandSender getPmSender();

    /**
     * Delete command and answers in private messages
     * @apiNote Not all implementations can support this
     */
    boolean isDeleteIfPM();

    /**
     * Delete command and answers in private messages
     * @param deleteIfPM {@code true} for deletion
     * @apiNote Not all implementations can support this
     */
    void setDeleteIfPM(boolean deleteIfPM);

    /**
     * @return CommandSender's name
     */
    String getName();

    /**
     * Send message for this command sender<br>
     * Message should be sent to same dialog that message executed calls with this command sender<br>
     * If {@link #isDeleteIfPM()} is {@code true} and {@link DialogType} is private, message should be deleted for bot (if supported)
     * @param message message to send, since 1.1 formatting should be escaped by implementation
     * @see #escapeFormatting(String)
     * @see #sendFormattedMessage(String)
     */
    void sendMessage(String message);

    /**
     * Send message for this command sender<br>
     * Message should be sent to same dialog that message executed calls with this command sender<br>
     * If {@link #isDeleteIfPM()} is {@code true} and {@link DialogType} is private, message should be deleted for bot (if supported)
     * @param message message to send with formatting symbols
     * @since 1.1
     */
    default void sendFormattedMessage(String message) {
        sendMessage(message);
    }

    /**
     * Send message for this command sender and delete it for bot
     * @param message message to send, since 1.1 formatting should be escaped by implementation
     * @apiNote Not all implementations can support this
     */
    void sendAndDeleteMessage(String message);

    /**
     * Send message for this command sender and delete it for bot
     * @param message message to send with formatting symbols
     * @apiNote Not all implementations can support this
     * @since 1.1
     */
    default void sendAndDeleteFormattedMessage(String message) {
        sendAndDeleteMessage(message);
    }

    /**
     * Send message for this command sender<br>
     * Message should be sent to same dialog that message executed calls with this command sender<br>
     * If {@link #isDeleteIfPM()} is {@code true} and {@link DialogType} is private, message should be deleted for bot (if supported)
     * @param message message to send
     */
    void sendMessage(Object message);

    /**
     * Send message for this command sender and delete it for bot
     * @param message message to send
     * @apiNote Not all implementations can support this
     */
    void sendAndDeleteMessage(Object message);

    /**
     * @return Messenger of this command sender
     */
    Messenger getMessenger();

    /**
     * @return Dialog type of this command sender
     */
    DialogType getDialogType();

    /**
     * Escapes formatting symbols for current messengers
     * @param rawText non-escaped text
     * @return escaped text
     * @since 1.1
     */
    default String escapeFormatting(String rawText) {
        return getMessenger().escaped(rawText);
    }
}
