package ru.blc.cutlet.api.event;

/**
 * Event listeners priority
 */
public enum EventPriority {

    /**
     * Lowest priority. Listeners with this priority runs first
     */
    LOWEST,
    /**
     * Low priority. Listeners with this priority runs after Lowest
     */
    LOW,
    /**
     * Default priority. Runs after Low
     */
    NORMAL,
    /**
     * High priority. Runs after normal
     */
    HIGH,
    /**
     * Highest priority. Runs after High. Last priority for manipulating event state
     */
    HIGHEST,
    /**
     * Used for monitoring. Do not manipulate event state there
     */
    MONITOR
}
