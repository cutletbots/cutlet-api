package ru.blc.cutlet.api.event.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.blc.cutlet.api.command.Command;
import ru.blc.cutlet.api.command.sender.CommandSender;
import ru.blc.cutlet.api.event.Cancellable;
import ru.blc.cutlet.api.event.Event;
import ru.blc.cutlet.api.event.HandlerList;

/**
 * Called when sender is ready to dispatch command.<br>
 * If cancelled - command would not dispatch
 */
@RequiredArgsConstructor
public class CommandPreprocessEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Getter
    private final CommandSender sender;
    @Getter
    private final Command command;
    @Getter
    private final String text;
    private boolean cancelled = false;

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
}
