package ru.blc.cutlet.api.event;

import ru.blc.cutlet.api.Cutlet;
import ru.blc.cutlet.api.bot.Bot;

import java.util.function.Predicate;

public class RegisteredListener {

    private final Listener listener;
    private final EventPriority priority;
    private final boolean ignoringCancelled;
    private final boolean ignoringFilter;
    private final Bot bot;
    private final EventExecutor executor;

    public RegisteredListener(Listener listener, EventExecutor executor, EventPriority priority, Bot bot,
                              boolean ignoreCancelled, boolean ignoringFilter) {
        this.listener = listener;
        this.priority = priority;
        this.bot = bot;
        this.executor = executor;
        this.ignoringCancelled = ignoreCancelled;
        this.ignoringFilter = ignoringFilter;
        if (isIgnoringFilter()) {
            Cutlet.instance().getLogger().warn("Registered event listener {} at bot {} with ignoring filtering.\n" +
                    "Your CONFIDENTIAL data can be STOLEN", getListener(), getBot().getName());
        }
    }

    public Listener getListener() {
        return listener;
    }

    public Bot getBot() {
        return bot;
    }

    public EventPriority getPriority() {
        return priority;
    }

    public void callEvent(Event event, Predicate<Bot> filter) throws EventException {
        if (!(event instanceof Cancellable) || !((Cancellable) event).isCancelled() || !this.isIgnoringCancelled()) {
            if (filter != null && !isIgnoringFilter()) {
                try {
                    if (!filter.test(getBot())) {
                        return;
                    }
                } catch (Exception e) {
                    Cutlet.instance().getLogger().error("Error while trying filter event " + event.getEventName() + "." +
                            " Event would not be fired for this consumer (Listener " + getListener() + " at bot " + getBot().getName() + ")", e);
                    return;
                }
            }
            this.executor.execute(this.listener, event);
        }
    }

    public boolean isIgnoringCancelled() {
        return ignoringCancelled;
    }

    public boolean isIgnoringFilter() {
        return ignoringFilter;
    }
}
