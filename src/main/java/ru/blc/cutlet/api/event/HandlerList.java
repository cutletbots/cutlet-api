package ru.blc.cutlet.api.event;

import ru.blc.cutlet.api.bot.Bot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map.Entry;

public class HandlerList {
    private static final ArrayList<HandlerList> allLists = new ArrayList<>();

    public static void bakeAll() {
        synchronized (allLists) {
            for (HandlerList h : allLists) {
                h.bake();
            }
        }
    }

    public static void unregisterAll() {
        synchronized (allLists) {
            for (HandlerList h : allLists) {
                h.bake();
                synchronized (h) {
                    for (ArrayList<RegisteredListener> l : h.handlerslots.values()) {
                        l.clear();
                    }
                    h.handlers = null;
                }
            }
        }
    }

    public static void unregisterAll(Bot bot) {
        synchronized (allLists) {
            for (HandlerList h : allLists) {
                h.unregister(bot);
            }
        }
    }

    public static void unregisterAll(Listener listener) {
        synchronized (allLists) {
            for (HandlerList h : allLists) {
                h.unregister(listener);
            }
        }
    }

    private volatile RegisteredListener[] handlers = null;
    private final EnumMap<EventPriority, ArrayList<RegisteredListener>> handlerslots = new EnumMap<>(EventPriority.class);


    public HandlerList() {
        EventPriority[] arg0;
        int arg1 = (arg0 = EventPriority.values()).length;

        for (int arg2 = 0; arg2 < arg1; ++arg2) {
            EventPriority o = arg0[arg2];
            this.handlerslots.put(o, new ArrayList<>());
        }

        synchronized (allLists) {
            allLists.add(this);
        }
    }

    public synchronized void register(RegisteredListener listener) {
        if (this.handlerslots.get(listener.getPriority()).contains(listener)) {
            throw new IllegalStateException(
                    "This listener is already registered to priority " + listener.getPriority().toString());
        } else {
            this.handlers = null;
            this.handlerslots.get(listener.getPriority()).add(listener);
        }
    }

    public void registerAll(Collection<RegisteredListener> listeners) {
        for (RegisteredListener listener : listeners) {
            this.register(listener);
        }
    }

    public synchronized void unregister(RegisteredListener listener) {
        if (this.handlerslots.get(listener.getPriority()).remove(listener)) {
            this.handlers = null;
        }

    }

    public synchronized void unregister(Bot bot) {
        for (ArrayList<RegisteredListener> list : this.handlerslots.values()) {
            list.removeIf(registeredListener -> registeredListener.getBot() == bot);
        }
    }

    public synchronized void unregister(Listener listener) {
        for (ArrayList<RegisteredListener> list : this.handlerslots.values()) {
            list.removeIf(registeredListener -> registeredListener.getListener() == listener);
        }
    }

    public synchronized void bake() {
        if (this.handlers == null) {
            ArrayList<RegisteredListener> entries = new ArrayList<>();
            for (Entry<EventPriority, ArrayList<RegisteredListener>> entry : this.handlerslots.entrySet()) {
                entries.addAll(entry.getValue());
            }
            this.handlers = entries.toArray(new RegisteredListener[0]);
        }
    }

    public RegisteredListener[] getRegisteredListeners() {
        while (true) {
            RegisteredListener[] handlers = this.handlers;
            if (this.handlers != null) {
                return handlers;
            }

            this.bake();
        }
    }

    public static ArrayList<RegisteredListener> getRegisteredListeners(Bot bot) {
        ArrayList<RegisteredListener> listeners = new ArrayList<>();

        synchronized (allLists) {
            for (HandlerList h : allLists) {
                synchronized (h) {
                    for (ArrayList<RegisteredListener> list : h.handlerslots.values()) {
                        for (RegisteredListener listener : list) {
                            if (listener.getBot().equals(bot)) {
                                listeners.add(listener);
                            }
                        }
                    }
                }
            }
            return listeners;
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<HandlerList> getHandlerLists() {
        synchronized (allLists) {
            return (ArrayList<HandlerList>) allLists.clone();
        }
    }
}
