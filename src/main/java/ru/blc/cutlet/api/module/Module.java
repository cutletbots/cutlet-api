package ru.blc.cutlet.api.module;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.blc.cutlet.api.Cutlet;
import ru.blc.cutlet.api.Plugin;
import ru.blc.objconfig.yml.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Represents module<br>
 * Module is provider for some social APIs (for example telegram, discord, etc.)
 */
public class Module implements Plugin {

    @Getter
    private ModuleDescription description;
    @Getter
    private Cutlet cutlet;
    @Getter
    private Logger logger;
    @Getter
    private File directory;
    @Getter
    private YamlConfiguration config;

    @Setter(value = AccessLevel.PACKAGE)
    private boolean loaded;
    private boolean enabled;

    public Module() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Preconditions.checkState(classLoader instanceof ModuleClassLoader, "Module requires " + ModuleClassLoader.class.getName());
        ((ModuleClassLoader) classLoader).init(this);
    }

    final public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /**
     * @return Module name
     */
    final public String getName() {
        return getDescription().getName();
    }

    @Override
    final public boolean isEnabled() {
        return enabled;
    }

    @Override
    final public boolean isLoaded() {
        return loaded;
    }

    final public void init(Cutlet cutlet, ModuleDescription description) {
        this.cutlet = cutlet;
        this.description = description;
        this.logger = LoggerFactory.getLogger(getName());
        this.directory = new File(cutlet.getModulesFolder(), getName());
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                getCutlet().getLogger().error("Error while initializing module {}. Can't create directory {}", getName(), getDirectory());
            }
        }
        config = new YamlConfiguration();
        try {
            InputStream in = getResourceAsStream("/config.yml");
            if (in != null) {
                config.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            getLogger().error("Failed to load configuration", e);
        }
    }

    /**
     * Reloads configuration from config.yml
     */
    public void reloadConfig() {
        File configFile = new File(getDirectory(), "config.yml");
        if (!configFile.exists()) return;
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Saves default config.yml file from module jar archive<br>
     * If file already exists does nothing
     */
    public void saveDefaultConfig() {
        File configFile = new File(getDirectory(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                getLogger().error("Failed to create config file", e);
                return;
            }
        } else {
            return;
        }
        InputStream in = this.getClass().getResourceAsStream("/config.yml");
        if (in == null) {
            getLogger().error("Failed to save default config. No config file at plugin");
            return;
        }
        try {
            FileOutputStream out = new FileOutputStream(configFile);
            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        } catch (IOException e) {
            getLogger().error("Failed to save default config.", e);
        }
        try {
            config.load(configFile);
        } catch (Exception e) {
            getLogger().error("Failed to load configuration", e);
        }
    }

    /**
     * Get resource from module jar archive
     *
     * @param name path to resource
     * @return Input stream of resource or null if resource not founded
     */
    public InputStream getResourceAsStream(@NotNull String name) {
        Preconditions.checkArgument(!name.isEmpty(), "name");
        InputStream in = null;
        File resource = new File(name);
        boolean founded = resource.exists() && resource.isFile();
        if (!founded) {
            try {
                in = this.getClass().getResourceAsStream(name);
            } catch (Exception e) {
                this.getLogger().error("Could not load resource" + name, e);
            }
        } else {
            try {
                in = new FileInputStream(resource);
            } catch (FileNotFoundException e) {
                this.getLogger().error("Could not load resource " + name, e);
            }
        }
        return in;
    }

    /**
     * Calls when module loaded. Override for own logic
     */
    public void onLoad() {
    }

    /**
     * Calls when module enabled. Override for own logic
     */
    public void onEnable() {
    }

    /**
     * Calls when module disabled. Override for own logic
     */
    public void onDisable() {
    }
}
