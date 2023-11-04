package ru.blc.cutlet.api;

import org.slf4j.Logger;

/**
 * Base interface for addons such as {@link ru.blc.cutlet.api.module.Module} or {@link ru.blc.cutlet.api.bot.Bot}
 */
public interface Plugin {

    String getName();

    Logger getLogger();

    Cutlet getCutlet();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean isLoaded();

    static <T extends Plugin> T get(Class<T> clazz) {
        return Cutlet.instance().getPlugin(clazz);
    }
}
