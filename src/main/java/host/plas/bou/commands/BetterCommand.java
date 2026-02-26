package host.plas.bou.commands;

import gg.drak.thebase.objects.Identifiable;
import gg.drak.thebase.utils.StringUtils;
import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.utils.SenderUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

public interface BetterCommand extends TabExecutor, Identifiable {
    String getCommandName();
    void setCommandName(String commandName);

    String getLabel();
    boolean setLabel(String label);

    String getDescription();
    Command setDescription(String description);

    String getUsage();
    Command setUsage(String usage);

    String getBasePermission();
    void setBasePermission(String basePermission);

    List<String> getAliases();
    Command setAliases(List<String> aliases);

    default ConcurrentSkipListSet<String> getConcurrentAliases() {
        return new ConcurrentSkipListSet<>(getAliases());
    }

    default Command setAliases(ConcurrentSkipListSet<String> aliases) {
        return setAliases(new ArrayList<>(aliases));
    }

    default String[] getSelfAliasesArray() {
        return getConcurrentAliases().toArray(new String[0]);
    }

    default void setAliases(String... aliases) {
        setAliases(Arrays.asList(aliases));
    }

    default void addSelfAliases(String... aliases) {
        ConcurrentSkipListSet<String> current = getConcurrentAliases();
        if (current == null) current = new ConcurrentSkipListSet<>();
        current.addAll(Arrays.asList(aliases));
        setAliases(current);
    }

    default void removeSelfAliases(String... aliases) {
        ConcurrentSkipListSet<String> current = getConcurrentAliases();
        if (current == null) current = new ConcurrentSkipListSet<>();
        Arrays.asList(aliases).forEach(current::remove);
        setAliases(current);
    }

    JavaPlugin getProvider();
    void setProvider(JavaPlugin provider);

    boolean isRegistered();
    void setRegistered(boolean registered);

    default BetterPlugin getPlugin() {
        try {
            return (BetterPlugin) getProvider();
        } catch (Exception e) {
            return null;
        }
    }

    default Optional<BetterPlugin> getPluginOptional() {
        return Optional.ofNullable(getPlugin());
    }

    default boolean isBOUPlugin() {
        return getPluginOptional().isPresent();
    }

    default void executeWithPlugin(Consumer<BetterPlugin> consumer) {
        getPluginOptional().ifPresent(consumer);
    }

    default void unregister() {
        if (isBuildable()) {
            unregisterBuildable();
            return;
        }

        CommandHandler.unregisterCommands(getCommandName());
    }

    default void unregisterBuildable() {
        if (isBuildable() && ! (this instanceof BuildableCommand)) {
            BukkitOfUtils.getInstance().logWarning("Command '" + getIdentifier() + "' is marked as buildable, but does not extend BuildableCommand! This may cause issues.");
            return;
        }

        BuildableCommand bc = (BuildableCommand) this;
        CommandHandler.unregisterCommands(bc.getName());
    }

    default void registerBuildable() {
        if (isBuildable() && ! (this instanceof BuildableCommand)) {
            BukkitOfUtils.getInstance().logWarning("Command '" + getIdentifier() + "' is marked as buildable, but does not extend BuildableCommand! This may cause issues.");
            return;
        }

        BuildableCommand bc = (BuildableCommand) this;
        CommandHandler.registerCommands(bc);
    }

    default boolean registerWithBukkit() {
        try {
            if (isBuildable()) {
                registerBuildable();
                return true;
            } else {
                Objects.requireNonNull(getProvider().getCommand(getIdentifier())).setExecutor(this);
                return true;
            }
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarning("Failed to register command '" + getIdentifier() + "'! --> No command found in plugin.yml!");
            return false;
        }
    }

    default void registerWithBOU() {
        CommandHandler.registerCommand(this);
    }

    default void unregisterWithBOU() {
        CommandHandler.unregisterCommand(this);
    }

    default void unregisterAndSet() {
        if (isRegistered()) {
            unregisterWithBOU();
            unregister();
            setRegistered(false);
        }
    }

    @Deprecated
    default boolean register() {
        return true; // Deprecated
    }

    default void registerAndSet() {
        if (registerWithBukkit()) {
            registerWithBOU();
            setRegistered(true);
        }
    }

    @Deprecated(since = "BOU 1.16.0. Use useCommandContext instead!", forRemoval = true)
    default boolean useNewCommandContext() {
        return true;
    }

    default boolean useCommandContext() {
        return useNewCommandContext();
    }

    default boolean isBuildable() {
        return false;
    }

    default boolean alwaysProcessTabCompletes() {
        return true;
    }

    default boolean isPermissionValid() {
        return getBasePermission() != null && ! getBasePermission().isBlank() && ! getBasePermission().equalsIgnoreCase(CommandBuilder.NULL);
    }

    default boolean hasPermission(CommandSender sender) {
        if (! isPermissionValid()) return defaultPermissionFallback();

        return sender.hasPermission(getBasePermission());
    }

    default boolean defaultPermissionFallback() {
        return true;
    }

    default boolean success() {
        return CommandResult.SUCCESS;
    }

    default boolean failure() {
        return CommandResult.FAILURE;
    }

    default boolean error() {
        return CommandResult.ERROR;
    }

    default boolean defaultNoPermission() {
        return failure();
    }

    default boolean handleNoPermission(CommandSender sender) {
        SenderUtils.getSender(sender).sendMessage("&cYou do not have permission to execute this command.");
        return defaultNoPermission();
    }

    default boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (! (this instanceof Command)) return false;
        Command cmd = (Command) this;

        return onCommand(sender, cmd, label, args);
    }

    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (! hasPermission(sender)) {
            return handleNoPermission(sender);
        }

        if (useNewCommandContext()) {
            return command(new CommandContext(sender, command, label, args));
        } else {
            return command(sender, command, label, args);
        }
    }

    default boolean command(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return command(new CommandContext(sender, command, label, args));
    }

    default boolean command(CommandContext ctx) {
        return false;
    }

    @Nullable @Override
    default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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

    default ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        return new ConcurrentSkipListSet<>();
    }

    default ConcurrentSkipListSet<String> tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return tabComplete(new CommandContext(sender, command, label, args));
    }

    default ConcurrentSkipListSet<String> getPlayerNamesForArg(CommandContext ctx, int argIndex) {
        return CommandHandler.getPlayerNamesForArg(ctx, argIndex);
    }
}
