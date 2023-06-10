package ru.blc.cutlet.api.timer;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.blc.cutlet.api.Cutlet;
import ru.blc.cutlet.api.Plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used for scheduling some tasks
 */
public class Timer {

    private static final AtomicBoolean started = new AtomicBoolean(false);
    private final DelayQueue<CutletTask> tasksQueue = new DelayQueue<>();
    private final Map<Plugin, Set<CutletTask>> tasksByPlugin = new WeakHashMap<>();
    private final Cutlet cutlet;

    public Timer(Cutlet cutlet) {
        this.cutlet = cutlet;
        if (!started.compareAndSet(false, true)) {
            cutlet.getLogger().error("Duplicate timer start!");
            return;
        }
        Thread t = new TimerThread();
        t.setName("Timer");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Cancels task. If task is running or finished this method does nothing
     *
     * @param task task gor cancel
     */
    public void cancelTack(CutletTask task) {
        if (task.getState() == TaskState.FINISHED) {
            return;
        }
        task.setState(TaskState.CANCELLED);
        tasksByPlugin.computeIfAbsent(task.getPlugin(), p -> new HashSet<>()).remove(task);
        tasksQueue.remove(task);
    }

    public void cancelAll(@NotNull Plugin owner) {
        Set<CutletTask> tasks = tasksByPlugin.remove(owner);
        if (tasks == null) return;
        tasks = new HashSet<>(tasks);
        tasks.forEach(this::cancelTack);
    }

    /**
     * Run task with specified delay
     * @param plugin task owner
     * @param delay delay
     * @param timeUnit delay unit
     * @param runnable task runnable
     * @return task
     */
    public CutletTask runTaskLaterAsync(@NotNull Plugin plugin, long delay, @NotNull TimeUnit timeUnit, @NotNull Runnable runnable) {
        Preconditions.checkState(plugin.isEnabled(), plugin.getName() + " attempted to start task while not enabled!");
        CutletTask task = new CutletTask(this, plugin, runnable, delay, timeUnit, false);
        tasksQueue.add(task);

        tasksByPlugin.computeIfAbsent(plugin, p -> new HashSet<>()).add(task);
        return task;
    }

    /**
     * Run repeating task
     * @param plugin task owner
     * @param delay repead period
     * @param timeUnit repeat period time unit
     * @param runnable task runnable
     * @return task
     */
    public CutletTask runTaskTimerAsync(@NotNull Plugin plugin, long delay, @NotNull TimeUnit timeUnit, @NotNull Runnable runnable) {
        Preconditions.checkState(plugin.isEnabled(), plugin.getName() + " attempted to start task while not enabled!");
        CutletTask task = new CutletTask(this, plugin, runnable, delay, timeUnit, true);
        tasksQueue.add(task);
        tasksByPlugin.computeIfAbsent(plugin, p -> new HashSet<>()).add(task);
        return task;
    }

    protected class TimerThread extends Thread {

        @Override
        public void run() {
            while (cutlet.isRunning()) {
                try {
                    CutletTask task = tasksQueue.take();
                    if (!task.getPlugin().isEnabled()) {
                        tasksByPlugin.computeIfAbsent(task.getPlugin(), p -> new HashSet<>()).remove(task);
                        continue;
                    }
                    task.setState(TaskState.RUNNING);
                    CompletableFuture<Void> future = CompletableFuture.runAsync(task::run);
                    if (task.isRounded()) {
                        future.whenComplete((v, t) -> {
                            task.setState(TaskState.WAITING);
                            task.resetStart();
                            tasksQueue.add(task);
                        });
                    } else {
                        future.whenComplete((v, t) -> {
                            task.setState(TaskState.FINISHED);
                            tasksByPlugin.computeIfAbsent(task.getPlugin(), p -> new HashSet<>()).remove(task);
                        });
                    }
                } catch (InterruptedException e) {
                    cutlet.getLogger().error("Exception at thread task", e);
                }
            }
            cutlet.getLogger().info("Cutlet disabled. Disabling");
            super.run();
        }
    }

    protected Cutlet getCutlet() {
        return cutlet;
    }
}
