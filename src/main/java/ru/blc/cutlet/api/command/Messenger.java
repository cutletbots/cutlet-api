package ru.blc.cutlet.api.command;

public interface Messenger {

    /**
     * Escapes all special symbols for this messenger
     * @param text text without escapes
     * @return text with escaped special characters
     * @since 1.1
     */
    default String escaped(String text) {
        return text;
    }
}
