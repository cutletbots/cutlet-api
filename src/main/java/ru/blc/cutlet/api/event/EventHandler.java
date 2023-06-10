package ru.blc.cutlet.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {

    EventPriority eventPriority() default EventPriority.NORMAL;

    /**
     * Ignore cancelled events.<br>
     * If event is cancelled and this is turned on (true), handler would not get event<br>
     * by default true
     *
     * @return ignoring cancelled events
     */
    boolean ignoreCancelled() default true;

    /**
     * Some modules can fire event for only some consumers.<br>
     * If handler ignores filter (true) than it would get event, even handler not matches filter<br>
     * by default false (filter would not ignore)
     *
     * @return ignore filtration
     */
    boolean ignoreFilter() default false;
}
