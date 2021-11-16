package ru.blc.cutlet.api.event;

public interface EventExecutor {

    void execute(Listener listener, Event event) throws EventException;
}
