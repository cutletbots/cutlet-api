package ru.blc.cutlet.api.command.sender;

import ru.blc.cutlet.api.command.Command;

public enum DialogType {
    /**
     * Private message
     */
    PRIVATE_MESSAGE,
    /**
     * Conversations
     */
    CONVERSATION,
    /**
     * Special type for all dialog types<br>
     * Used for commands ({@link Command#getDialogType()})<br>
     * Never should be used in command sender
     */
    ALL,
    ;

    /**
     * Check if this Dialog allows other dialog type<br>
     * Used for command checks ({@link Command#getDialogType()})<br>
     * Check is:
     * <pre>
     *     if (this == ALL || other == ALL) return true;
     *     return other == this;
     * </pre>
     * @param other dialog type to check
     * @return true if dialog type allowed
     */
    public boolean allows(DialogType other) {
        if (this == ALL || other == ALL) return true;
        return other == this;
    }
}
