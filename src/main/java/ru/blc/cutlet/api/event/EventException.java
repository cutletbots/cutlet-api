package ru.blc.cutlet.api.event;

import java.io.Serial;

public class EventException extends Exception {

    @Serial private static final long serialVersionUID = -8223330820318222978L;

    public EventException() {
        super();
    }

    public EventException(String message) {
        super(message);
    }

    public EventException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventException(Throwable cause) {
        super(cause);
    }
}
