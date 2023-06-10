package ru.blc.cutlet.api.event;

/**
 * For cancellable commands.
 */
public interface Cancellable {

    void setCancelled(boolean cancelled);

    boolean isCancelled();
}
