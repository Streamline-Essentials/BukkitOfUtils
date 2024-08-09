package host.plas.bou.commands;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.BetterPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tv.quaint.objects.Identified;
import tv.quaint.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

@Setter
@Getter
public abstract class ComplexCommand implements TabExecutor, Identified {
    private String identifier;
    private String commandName;
    private JavaPlugin provider;
    private boolean registered;

    public ComplexCommand(String commandName, JavaPlugin provider) {
        this.identifier = CommandHandler.getIdentifier(commandName, provider);
        this.commandName = commandName;
        this.provider = provider;

        registerAndSet();
    }

    public BetterPlugin getPlugin() {
        try {
            return (BetterPlugin) getProvider();
        } catch (Exception e) {
            return null;
        }
    }

    public Optional<BetterPlugin> getPluginOptional() {
        return Optional.ofNullable(getPlugin());
    }

    public boolean isBOUPlugin() {
        return getPluginOptional().isPresent();
    }

    public void executeWithPlugin(Consumer<BetterPlugin> consumer) {
        getPluginOptional().ifPresent(consumer);
    }

    public boolean registerWithBukkit() {
        try {
            Objects.requireNonNull(getProvider().getCommand(getIdentifier())).setExecutor(this);
            return true;
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to register command '" + identifier + "'! --> No command found in plugin.yml!");
            return false;
        }
    }

    public void registerWithBOU() {
        CommandHandler.registerCommand(this);
    }

    public void unregisterWithBOU() {
        CommandHandler.unregisterCommand(this);
    }

    @Deprecated
    public boolean register() {
        return true; // Deprecated
    }

    public void registerAndSet() {
        if (registerWithBukkit()) {
            registerWithBOU();
            setRegistered(true);
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
