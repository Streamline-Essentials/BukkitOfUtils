package io.streamlined.bukkit.commands;

import io.streamlined.bukkit.MessageUtils;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tv.quaint.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class AbstractQuaintCommand implements TabExecutor {
    @Getter
    final String identifier;

    public AbstractQuaintCommand(String identifier, JavaPlugin provider) {
        this.identifier = identifier;

        try {
            Objects.requireNonNull(provider.getCommand(identifier)).setExecutor(this);
        } catch (Exception e) {
            MessageUtils.logWarning("Failed to register command '" + identifier + "'! --> No command found in plugin.yml!");
        }
    }

    public boolean useNewCommandContext() {
        return true;
    }

    public boolean alwaysProcessTabCompletes() {
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (useNewCommandContext()) {
            return command(new CommandContext(sender, command, label, args));
        } else {
            return command(sender, command, label, args);
        }
    }

    public abstract boolean command(CommandContext ctx);

    public abstract boolean command(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list;

        if (useNewCommandContext()) {
            list = new ArrayList<>(tabComplete(new CommandContext(sender, command, label, args)));
        } else {
            list = new ArrayList<>(tabComplete(sender, command, label, args));
        }

        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        String lastArg;

        try {
            lastArg = args[args.length - 1];
        } catch (Exception e) {
            lastArg = "";
        }

        if (alwaysProcessTabCompletes()) {
            list = StringUtils.getAsCompletionList(lastArg, list);
        }

        return list;
    }

    public abstract ConcurrentSkipListSet<String> tabComplete(CommandContext ctx);

    public abstract ConcurrentSkipListSet<String> tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);
}
