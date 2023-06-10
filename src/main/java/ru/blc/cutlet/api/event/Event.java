package ru.blc.cutlet.api.event;

/**
 * Base event class
 */
public abstract class Event {

    public abstract HandlerList getHandlers();

    public String getEventName() {
        return getClass().getSimpleName();
    }
}
