package ru.blc.cutlet.api.timer;

/**
 * Represents task condition.<br>
 * For timer tasks condition {@link #FINISHED} never happens,
 * conditions {@link #WAITING} and {@link #RUNNING} rotates among themselves.
 */
public enum TaskState {

    /**
     * Task waiting for start
     */
    WAITING,
    /**
     * Task works right now
     */
    RUNNING,
    /**
     * Task completed
     */
    FINISHED,
    /**
     * Task cancelled before starting
     */
    CANCELLED;
}
