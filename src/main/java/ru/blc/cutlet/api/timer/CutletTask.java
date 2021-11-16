package ru.blc.cutlet.api.timer;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.blc.cutlet.api.Plugin;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class CutletTask implements Delayed {

    private final Timer timer;
    private final Runnable runnable;
    private final Plugin plugin;
    private final long delay;
    /**
     * end time in milisec
     */
    private long end;
    private final TimeUnit timeUnit;
    private TaskState state = TaskState.WAITING;
    private final boolean rounded;

    public CutletTask(@NotNull Timer timer, @NotNull Plugin plugin, @NotNull Runnable runnable, long delay, @NotNull TimeUnit timeUnit, boolean rounded) {
        Preconditions.checkArgument(delay > 0, "delay should be more than zero!");
        Preconditions.checkArgument(TimeUnit.MILLISECONDS.compareTo(timeUnit) <= 0, "Can't run task with time unit lower than millisecond!");
        this.timer = timer;
        this.plugin = plugin;
        this.runnable = runnable;
        this.delay = delay;
        this.timeUnit = timeUnit;
        this.rounded = rounded;
        resetStart();
    }

    void run() {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            timer.getCutlet().getLogger().error("Task for " + plugin.getName() + " generated exception", throwable);
        }
    }

    public void cancel() {
        timer.cancelTack(this);
    }

    public boolean isCancelled() {
        return getState() == TaskState.CANCELLED;
    }

    public TaskState getState() {
        return this.state;
    }

    void setState(TaskState state) {
        this.state = state;
    }

    void resetStart() {
        end = now() + TimeUnit.MILLISECONDS.convert(delay, timeUnit);
    }

    boolean isRounded() {
        return rounded;
    }

    static long now() {
        return System.currentTimeMillis();
    }

    Plugin getPlugin() {
        return plugin;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return unit.convert(end - now(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
        return Long.compare(getDelay(timeUnit), o.getDelay(timeUnit));
    }
}
