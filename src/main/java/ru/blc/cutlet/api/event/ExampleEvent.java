package ru.blc.cutlet.api.event;

/**
 * Example event. Contains all required methods
 */
public class ExampleEvent extends Event{

    /**
     * Field that contains all event listeners
     */
    private static final HandlerList handlers = new HandlerList();

    /**
     * @return all event listeners
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * same as {@link #getHandlers()}, but static
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
