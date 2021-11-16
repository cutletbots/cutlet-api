package ru.blc.cutlet.api.command.sender;

public enum DialogType {
    PRIVATE_MESSAGE,
    CONVERSATION,
    ALL,
    ;

    public boolean allows(DialogType other) {
        if (this == ALL || other == ALL) return true;
        return other == this;
    }
}
