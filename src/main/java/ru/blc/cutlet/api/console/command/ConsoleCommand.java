package ru.blc.cutlet.api.console.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.blc.cutlet.api.command.Command;
import ru.blc.cutlet.api.command.sender.DialogType;

public abstract class ConsoleCommand extends Command {

    public ConsoleCommand(@Nullable DialogType dialogType, @NotNull String name, @Nullable String permission, @Nullable String description, @Nullable String usage, String... aliases) {
        super(null, dialogType == null ? DialogType.ALL : dialogType, name, permission, description, usage, aliases);
    }

    public ConsoleCommand(@NotNull String name, @Nullable String permission, @Nullable String description, @Nullable String usage, String... aliases) {
        this(null, name, permission, description, usage, aliases);
    }

    @Override
    public boolean isOnlyConsole() {
        return true;
    }
}
