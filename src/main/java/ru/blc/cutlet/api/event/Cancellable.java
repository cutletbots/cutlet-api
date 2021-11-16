package ru.blc.cutlet.api.event;

public interface Cancellable {

    void setCancelled(boolean cancelled);

    boolean isCancelled();
}
