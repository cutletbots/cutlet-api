package ru.blc.cutlet.api.event.bot;

import ru.blc.cutlet.api.bot.Bot;
import ru.blc.cutlet.api.event.HandlerList;

public class BotEnabledEvent extends BotEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public BotEnabledEvent(Bot bot) {
        super(bot);
    }
}
