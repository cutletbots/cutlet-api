package ru.blc.cutlet.api.bot;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.blc.cutlet.api.Cutlet;
import ru.blc.cutlet.api.command.Command;
import ru.blc.cutlet.api.event.*;
import ru.blc.cutlet.api.event.bot.BotDisabledEvent;
import ru.blc.cutlet.api.event.bot.BotEnabledEvent;
import ru.blc.objconfig.yml.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class BotManager {

    private final Cutlet cutlet;
    private final Map<Bot, Map<String, Command>> commandsByBots = new HashMap<>();

    private final Map<String, Bot> bots = new HashMap<>();
    private Map<String, BotDescription> toLoad = new HashMap<>();

    public BotManager(Cutlet cutlet) {
        this.cutlet = cutlet;
    }

    public Cutlet getCutlet() {
        return cutlet;
    }

    /**
     * Register command<br>
     * If command name or at least one command alias is already taken by other command - command would not register
     *
     * @param bot     command owner
     * @param command command
     * @return true, if command registered, otherwise false
     */
    public boolean registerCommand(Bot bot, @NotNull Command command) {
        if (bot != null && !bot.isEnabled()) {
            getCutlet().getLogger().warn("{} attempted to register commands while not enabled!", bot.getName());
            return false;
        }
        if (commandsByBots.computeIfAbsent(bot, b -> new HashMap<>()).containsKey(command.getName().toLowerCase(Locale.ROOT)))
            return false;
        for (String alias : command.getAliases()) {
            if (commandsByBots.computeIfAbsent(bot, b -> new HashMap<>()).containsKey(alias.toLowerCase(Locale.ROOT)))
                return false;
        }
        commandsByBots.computeIfAbsent(bot, b -> new HashMap<>()).put(command.getName().toLowerCase(Locale.ROOT), command);
        for (String alias : command.getAliases()) {
            commandsByBots.computeIfAbsent(bot, b -> new HashMap<>()).put(alias.toLowerCase(Locale.ROOT), command);
        }
        return true;
    }

    /**
     * unregisters command
     *
     * @param command command
     */
    public void unregisterCommand(Command command) {
        commandsByBots.computeIfAbsent(command.getOwner(), c -> new HashMap<>()).values().remove(command);
    }

    /**
     * Unregister all bots command
     *
     * @param bot command owner
     */
    public void unregisterCommands(Bot bot) {
        commandsByBots.remove(bot);
    }


    /**
     * returns command by name or alias
     *
     * @param alias command name or alias
     * @return command or null if no matching command founded
     * @deprecated use {@link BotManager#getCommand(String, Bot)}
     */
    @Deprecated
    public Command getCommand(String alias) {
        for (Map<String, Command> value : commandsByBots.values()) {
            if (value.containsKey(alias.toLowerCase(Locale.ROOT))) {
                return value.get(alias.toLowerCase(Locale.ROOT));
            }
        }
        return null;
    }

    /**
     * returns command by name or alias
     *
     * @param alias command name or alias
     * @param owner command owner. null for cutlet commands
     * @return command or null if no matching command founded
     */
    public @Nullable Command getCommand(@NotNull String alias, @Nullable Bot owner) {
        return commandsByBots.computeIfAbsent(owner, b -> new HashMap<>()).get(alias.toLowerCase(Locale.ROOT));
    }

    /**
     * @param owner command owner. null for cutlet commands
     * @return all commands owned by specified bot
     */
    public @NotNull Collection<@NotNull Command> getCommands(@Nullable Bot owner) {
        return new HashSet<>(commandsByBots.getOrDefault(owner, new HashMap<>()).values());
    }

    /**
     * @param name bot name
     * @return Bot with specified name or null if no bot was found
     */
    public @Nullable Bot getBot(@NotNull String name) {
        return this.bots.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * @param clazz bot class
     * @param <T>   bot
     * @return bot for specified class or null if no bot was found
     */
    @SuppressWarnings("unchecked")
    public <T extends Bot> @Nullable T getBot(@NotNull Class<T> clazz) {
        T r = null;
        for (Bot value : this.bots.values()) {
            if (value.getClass() == clazz) {
                r = (T) value;
                break;
            }
        }
        return r;
    }

    public void detectBots(File folder) {
        Preconditions.checkNotNull(folder, "bots folder");
        Preconditions.checkArgument(folder.isDirectory(), "should be directory");
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                try {
                    JarFile jar = new JarFile(file);
                    Throwable t1 = null;
                    try {
                        JarEntry desc = jar.getJarEntry("bot.yml");
                        Preconditions.checkNotNull(desc, "Bot should has bot.yml");
                        Throwable t2 = null;
                        InputStream in = jar.getInputStream(desc);
                        try {
                            BotDescription description = YamlConfiguration.loadToType(new InputStreamReader(in), BotDescription.class);
                            Preconditions.checkNotNull(description.getName(), "Bot from %s has not name", file);
                            Preconditions.checkNotNull(description.getMain(), "Bot from %s has not main", file);
                            description.setFile(file);
                            if (this.toLoad.containsKey(description.getName())) {
                                throw new IllegalStateException("Duplicate bots " + description.getName() + " at " + file + " and "
                                        + toLoad.get(description.getName()).getFile());
                            }
                            this.toLoad.put(description.getName(), description);
                        } catch (Throwable t) {
                            t2 = t;
                            throw t;
                        } finally {
                            if (in != null) {
                                if (t2 != null) {
                                    try {
                                        in.close();
                                    } catch (Throwable t) {
                                        t2.addSuppressed(t);
                                    }
                                } else {
                                    in.close();
                                }
                            }
                        }
                    } catch (Throwable t) {
                        t1 = t;
                        throw t;
                    } finally {
                        if (t1 != null) {
                            try {
                                jar.close();
                            } catch (Throwable t) {
                                t1.addSuppressed(t);
                            }
                        } else {
                            jar.close();
                        }
                    }
                } catch (Exception e) {
                    cutlet.getLogger().error("Could not load bot from file " + file, e);
                }
            }
        }

    }

    public void loadBots() {
        Map<BotDescription, Boolean> stats = new HashMap<>();

        toLoad.values().forEach(bd -> loadBot(bd, new Stack<>(), stats));

        toLoad.clear();
        toLoad = null;
    }

    protected boolean loadBot(BotDescription description, Stack<BotDescription> dependencies, Map<BotDescription, Boolean> stats) {
        boolean state;
        if (stats.containsKey(description)) {
            return stats.get(description);
        } else {
            state = true;
            String name = description.getName();
            for (String module : description.getModules()) {
                if (getCutlet().getModule(module) == null) {
                    state = false;
                    getCutlet().getLogger().warn("Could not load bot {}. Dependency module {} not founded", name, module);
                    break;
                }
            }
            if (state) {
                dependencies.add(description);
                Set<String> depend = new HashSet<>();
                depend.addAll(description.getDepends());
                depend.addAll(description.getSoftDepends());
                for (String s : depend) {
                    BotDescription dependency = toLoad.get(s);
                    if (dependency == null) {
                        state = false;
                        getCutlet().getLogger().warn("Could not load bot {}. Dependency bot {} not founded", name, s);
                        break;
                    }
                    if (dependencies.contains(dependency)) {
                        //Recursive dependencies
                        String way = dependency.getName() + " -> " +
                                dependencies.stream().map(BotDescription::getName).collect(Collectors.joining(" -> "));
                        state = false;
                        getCutlet().getLogger().warn("Could not load bot {}. Recursive bot dependencies detected {}", name, way);
                        break;
                    }
                    if (!loadBot(dependency, dependencies, stats)) {
                        state = false;
                        getCutlet().getLogger().warn("Could not load bot {}. Failed loading bot dependency {}", name, s);
                        break;
                    }
                }
            }
        }
        if (state) {
            try {
                URLClassLoader loader = new BotClassLoader(cutlet, description.getFile(), description);
                Class<?> mainClazz = loader.loadClass(description.getMain());
                Bot clazz = (Bot) mainClazz.getDeclaredConstructor().newInstance();
                clazz.setLoaded(true);
                clazz.onLoad();
                this.bots.put(clazz.getName().toLowerCase(Locale.ROOT), clazz);
                getCutlet().getLogger().info("Loaded bot {} version {} by {}", description.getName(), description.getVersion(), description.getAuthor());
            } catch (Exception e) {
                state = false;
                getCutlet().getLogger().error("Could not load bot " + description.getName(), e);
            }
        }
        stats.put(description, state);
        return state;
    }

    public void enableBots() {
        this.bots.values().forEach(this::enableBot);
    }

    protected boolean enableBot(Bot bot) {
        Set<String> depend = new HashSet<>(bot.getDescription().getSoftDepends());
        boolean status = true;
        for (String s : depend) {
            Bot softDependency = getBot(s);
            if (!enableBot(softDependency)) {
                status = false;
                getCutlet().getLogger().warn("Failed to enable bot {} via dependency {} can not be enabled", bot.getName(), s);
                break;
            }
        }
        if (status) {
            try {
                bot.setEnabled(true);
                callEvent(new BotEnabledEvent(bot));
                getCutlet().getLogger().info("Enabled bot {} version {} by {}", bot.getName(), bot.getDescription().getVersion(), bot.getDescription().getAuthor());
            } catch (Exception e) {
                status = false;
                getCutlet().getLogger().error("Exception while enabling bot " + bot.getName(), e);
                try {
                    HandlerList.unregisterAll(bot);
                    getCutlet().getTimer().cancelAll(bot);
                    this.unregisterCommands(bot);
                    bot.setEnabled(false);
                } catch (Exception ignored) {
                }
            }
        }
        return status;
    }

    public void disableBots() {
        for (Bot bot : this.bots.values()) {
            if (bot.isEnabled()) {
                try {
                    bot.onDisable();
                } catch (Exception e) {
                    getCutlet().getLogger().error("Exception while disabling bot " + bot.getName(), e);
                } finally {
                    bot.setEnabled(false);
                    HandlerList.unregisterAll(bot);
                    this.unregisterCommands(bot);
                    callEvent(new BotDisabledEvent(bot));
                    getCutlet().getLogger().info("Disabled bot {} version {} by {}", bot.getName(), bot.getDescription().getVersion(), bot.getDescription().getAuthor());
                }
            }
        }
    }

    /**
     * Calls specified event
     *
     * @param event event to call
     */
    public void callEvent(@NotNull Event event) {
        this.callEvent(event, null);
    }

    /**
     * Calls specified event with bot filter<br>
     * Only bots, that's match specified filter would be called
     *
     * @param event  event to call
     * @param filter bot filter
     */
    public void callEvent(@NotNull Event event, @Nullable Predicate<Bot> filter) {
        this.fireEvent(event, filter);
    }

    private void fireEvent(Event event, Predicate<Bot> filter) {
        HandlerList handlers = event.getHandlers();
        RegisteredListener[] listeners = handlers.getRegisteredListeners();
        for (RegisteredListener registration : listeners) {
            try {
                registration.callEvent(event, filter);
            } catch (Throwable arg10) {
                getCutlet().getLogger().warn("Could not pass event " + event.getEventName() + " to "
                        + registration.getBot().getName(), arg10);
            }
        }

    }

    /**
     * Registers events listener
     *
     * @param bot      bot that listen event
     * @param listener listener
     */
    @SuppressWarnings("unchecked")
    public void registerEvents(@NotNull Bot bot, @NotNull Listener listener) {
        Preconditions.checkNotNull(bot, "bot");
        Preconditions.checkNotNull(listener, "Listener");
        if (!bot.isEnabled()) {
            getCutlet().getLogger().warn("{} attempted to register events while not enabled!", bot.getName());
            return;
        }
        HashMap<Class<? extends Event>, Set<RegisteredListener>> map = new HashMap<>();

        for (Method m : listener.getClass().getMethods()) {
            if (m.isAnnotationPresent(EventHandler.class)) {
                if (m.getParameterTypes().length == 1 && Event.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    map.put((Class<? extends Event>) m.getParameterTypes()[0], new HashSet<>());
                } else {
                    bot.getLogger().warn("{} attempted to register an invalid EventHandler method signature \"{}\" in {}",
                            bot.getName(), m.toGenericString(), listener.getClass());
                }
            }
        }
        for (Method m : listener.getClass().getMethods()) {
            if (m.isAnnotationPresent(EventHandler.class)) {
                if (m.getParameterTypes().length == 1 && Event.class.isAssignableFrom(m.getParameterTypes()[0])) {

                    map.get(m.getParameterTypes()[0]).add(new RegisteredListener(listener, new BotManagerEventExecutor(m.getParameterTypes()[0], m),
                            m.getAnnotation(EventHandler.class).eventPriority(), bot,
                            m.getAnnotation(EventHandler.class).ignoreCancelled(),
                            m.getAnnotation(EventHandler.class).ignoreFilter()));
                }
            }
        }

        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : map.entrySet()) {
            this.getEventListeners(this.getRegistrationClass(entry.getKey()))
                    .registerAll(entry.getValue());
        }
    }

    /**
     * @param type event type
     * @return all event listeners for specified event
     */
    private @NotNull HandlerList getEventListeners(@NotNull Class<? extends Event> type) {
        try {
            Method e = this.getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            e.setAccessible(true);
            return (HandlerList) e.invoke(null, new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }


    /**
     * @param clazz event type
     * @return event class that registers listeners.
     */
    private Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException arg1) {
            if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Event.class) && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return this.getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new RuntimeException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
            }
        }
    }

    static class BotManagerEventExecutor implements EventExecutor {

        private final Class<?> clazz;
        private final Method method;

        public BotManagerEventExecutor(Class<?> clazz, Method method) {
            this.clazz = clazz;
            this.method = method;
        }

        @Override
        public void execute(Listener listener, Event event) throws EventException {
            if (clazz.isAssignableFrom(event.getClass())) {
                try {
                    method.invoke(listener, event);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new EventException("Failed execute event ", e);
                }
            }
        }
    }
}
