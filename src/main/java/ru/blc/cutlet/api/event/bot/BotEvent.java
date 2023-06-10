package ru.blc.cutlet.api.event.bot;

import ru.blc.cutlet.api.bot.Bot;
import ru.blc.cutlet.api.event.Event;

/**
 * Represents bot events
 */
public abstract class BotEvent extends Event {

    private final Bot bot;

    public BotEvent(Bot bot) {
        this.bot = bot;
    }

    /**
     * Bot invoked this event
     */
    public Bot getBot() {
        return bot;
    }
}
