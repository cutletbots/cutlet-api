package ru.blc.cutlet.api.timer;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.blc.cutlet.api.Cutlet;
import ru.blc.cutlet.api.Plugin;

import java.util.concurrent.TimeUnit;

public abstract class CutletRunnable implements Runnable {

    private CutletTask task;

    public CutletTask getTask() {
        return task;
    }

    public void cancel() {
        Preconditions.checkState(task != null, "Not started yet!");
        task.cancel();
    }

    public boolean isCancelled() {
        Preconditions.checkState(task != null, "Not started yet!");
        return task.isCancelled();
    }

    public TaskState getState() {
        Preconditions.checkState(task != null, "Not started yet!");
        return task.getState();
    }

    public void runTaskLaterAsync(@NotNull Plugin plugin, long delay, @NotNull TimeUnit timeUnit) {
        Preconditions.checkState(task == null, "Already started!");
        task = Cutlet.instance().getTimer().runTaskLaterAsync(plugin, delay, timeUnit, this);
    }

    public void runTaskTimerAsync(@NotNull Plugin plugin, long delay, @NotNull TimeUnit timeUnit) {
        Preconditions.checkState(task == null, "Already started!");
        task = Cutlet.instance().getTimer().runTaskTimerAsync(plugin, delay, timeUnit, this);
    }
}
