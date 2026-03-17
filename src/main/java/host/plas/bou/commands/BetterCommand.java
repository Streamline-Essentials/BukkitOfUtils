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

/**
 * An interface representing an enhanced command that extends Bukkit's TabExecutor
 * with additional registration, permission, and context features.
 */
public interface BetterCommand extends TabExecutor, Identifiable {
    /**
     * Gets the name of this command.
     *
     * @return the command name
     */
    String getCommandName();

    /**
     * Sets the name of this command.
     *
     * @param commandName the command name to set
     */
    void setCommandName(String commandName);

    /**
     * Gets the label of this command.
     *
     * @return the command label
     */
    String getLabel();

    /**
     * Sets the label of this command.
     *
     * @param label the label to set
     * @return true if the label was successfully set
     */
    boolean setLabel(String label);

    /**
     * Gets the description of this command.
     *
     * @return the command description
     */
    String getDescription();

    /**
     * Sets the description of this command.
     *
     * @param description the description to set
     * @return the Command instance for chaining
     */
    Command setDescription(String description);

    /**
     * Gets the usage string for this command.
     *
     * @return the usage string
     */
    String getUsage();

    /**
     * Sets the usage string for this command.
     *
     * @param usage the usage string to set
     * @return the Command instance for chaining
     */
    Command setUsage(String usage);

    /**
     * Gets the base permission required to execute this command.
     *
     * @return the base permission string
     */
    String getBasePermission();

    /**
     * Sets the base permission required to execute this command.
     *
     * @param basePermission the base permission string to set
     */
    void setBasePermission(String basePermission);

    /**
     * Gets the list of aliases for this command.
     *
     * @return the list of aliases
     */
    List<String> getAliases();

    /**
     * Sets the aliases for this command.
     *
     * @param aliases the list of aliases to set
     * @return the Command instance for chaining
     */
    Command setAliases(List<String> aliases);

    /**
     * Gets the aliases as a thread-safe ConcurrentSkipListSet.
     *
     * @return a ConcurrentSkipListSet containing the command aliases
     */
    default ConcurrentSkipListSet<String> getConcurrentAliases() {
        return new ConcurrentSkipListSet<>(getAliases());
    }

    /**
     * Sets the aliases from a ConcurrentSkipListSet.
     *
     * @param aliases the set of aliases to set
     * @return the Command instance for chaining
     */
    default Command setAliases(ConcurrentSkipListSet<String> aliases) {
        return setAliases(new ArrayList<>(aliases));
    }

    /**
     * Gets the aliases as a string array.
     *
     * @return an array of alias strings
     */
    default String[] getSelfAliasesArray() {
        return getConcurrentAliases().toArray(new String[0]);
    }

    /**
     * Sets the aliases from a varargs array.
     *
     * @param aliases the aliases to set
     */
    default void setAliases(String... aliases) {
        setAliases(Arrays.asList(aliases));
    }

    /**
     * Adds additional aliases to the existing set of aliases.
     *
     * @param aliases the aliases to add
     */
    default void addSelfAliases(String... aliases) {
        ConcurrentSkipListSet<String> current = getConcurrentAliases();
        if (current == null) current = new ConcurrentSkipListSet<>();
        current.addAll(Arrays.asList(aliases));
        setAliases(current);
    }

    /**
     * Removes the specified aliases from the existing set of aliases.
     *
     * @param aliases the aliases to remove
     */
    default void removeSelfAliases(String... aliases) {
        ConcurrentSkipListSet<String> current = getConcurrentAliases();
        if (current == null) current = new ConcurrentSkipListSet<>();
        Arrays.asList(aliases).forEach(current::remove);
        setAliases(current);
    }

    /**
     * Gets the plugin that provides this command.
     *
     * @return the JavaPlugin provider
     */
    JavaPlugin getProvider();

    /**
     * Sets the plugin that provides this command.
     *
     * @param provider the JavaPlugin provider to set
     */
    void setProvider(JavaPlugin provider);

    /**
     * Checks whether this command is currently registered.
     *
     * @return true if this command is registered
     */
    boolean isRegistered();

    /**
     * Sets the registration state of this command.
     *
     * @param registered true if the command should be marked as registered
     */
    void setRegistered(boolean registered);

    /**
     * Gets the provider as a BetterPlugin instance, if possible.
     *
     * @return the provider cast to BetterPlugin, or null if the cast fails
     */
    default BetterPlugin getPlugin() {
        try {
            return (BetterPlugin) getProvider();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the provider as an Optional BetterPlugin.
     *
     * @return an Optional containing the BetterPlugin, or empty if not available
     */
    default Optional<BetterPlugin> getPluginOptional() {
        return Optional.ofNullable(getPlugin());
    }

    /**
     * Checks whether the provider is a BetterPlugin (BOU plugin).
     *
     * @return true if the provider is a BetterPlugin instance
     */
    default boolean isBOUPlugin() {
        return getPluginOptional().isPresent();
    }

    /**
     * Executes a consumer with the BetterPlugin instance if the provider is a BOU plugin.
     *
     * @param consumer the consumer to execute with the BetterPlugin
     */
    default void executeWithPlugin(Consumer<BetterPlugin> consumer) {
        getPluginOptional().ifPresent(consumer);
    }

    /**
     * Unregisters this command from both BOU and the server command map.
     * Delegates to buildable unregistration if this command is buildable.
     */
    default void unregister() {
        if (isBuildable()) {
            unregisterBuildable();
            return;
        }

        CommandHandler.unregisterCommands(getCommandName());
    }

    /**
     * Unregisters this command as a buildable command from the server command map.
     * Logs a warning if the command is marked as buildable but does not extend BuildableCommand.
     */
    default void unregisterBuildable() {
        if (isBuildable() && ! (this instanceof BuildableCommand)) {
            BukkitOfUtils.getInstance().logWarning("Command '" + getIdentifier() + "' is marked as buildable, but does not extend BuildableCommand! This may cause issues.");
            return;
        }

        BuildableCommand bc = (BuildableCommand) this;
        CommandHandler.unregisterCommands(bc.getName());
    }

    /**
     * Registers this command as a buildable command with the server command map.
     * Logs a warning if the command is marked as buildable but does not extend BuildableCommand.
     */
    default void registerBuildable() {
        if (isBuildable() && ! (this instanceof BuildableCommand)) {
            BukkitOfUtils.getInstance().logWarning("Command '" + getIdentifier() + "' is marked as buildable, but does not extend BuildableCommand! This may cause issues.");
            return;
        }

        BuildableCommand bc = (BuildableCommand) this;
        CommandHandler.registerCommands(bc);
    }

    /**
     * Registers this command with Bukkit's command system.
     * Uses buildable registration if applicable, otherwise registers via plugin.yml.
     *
     * @return true if the command was successfully registered with Bukkit
     */
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

    /**
     * Registers this command with BOU's command handler.
     */
    default void registerWithBOU() {
        CommandHandler.registerCommand(this);
    }

    /**
     * Unregisters this command from BOU's command handler.
     */
    default void unregisterWithBOU() {
        CommandHandler.unregisterCommand(this);
    }

    /**
     * Unregisters this command from both BOU and Bukkit if it is currently registered,
     * and sets the registered state to false.
     */
    default void unregisterAndSet() {
        if (isRegistered()) {
            unregisterWithBOU();
            unregister();
            setRegistered(false);
        }
    }

    /**
     * Registers this command. Deprecated and no longer functional.
     *
     * @return always returns true
     */
    @Deprecated
    default boolean register() {
        return true; // Deprecated
    }

    /**
     * Registers this command with both Bukkit and BOU, and sets the registered state to true
     * if Bukkit registration succeeds.
     */
    default void registerAndSet() {
        if (registerWithBukkit()) {
            registerWithBOU();
            setRegistered(true);
        }
    }

    /**
     * Checks whether this command uses the new command context system.
     *
     * @return always returns true
     */
    @Deprecated(since = "BOU 1.16.0. Use useCommandContext instead!", forRemoval = true)
    default boolean useNewCommandContext() {
        return true;
    }

    /**
     * Checks whether this command uses the command context system.
     *
     * @return true if the command context system should be used
     */
    default boolean useCommandContext() {
        return useNewCommandContext();
    }

    /**
     * Checks whether this command is buildable (registered via the command map rather than plugin.yml).
     *
     * @return true if this command is buildable; false by default
     */
    default boolean isBuildable() {
        return false;
    }

    /**
     * Checks whether tab completions should always be processed (filtered by partial input).
     *
     * @return true if tab completes should always be processed; true by default
     */
    default boolean alwaysProcessTabCompletes() {
        return true;
    }

    /**
     * Checks whether the base permission is valid (non-null, non-blank, and not the NULL sentinel).
     *
     * @return true if the base permission is valid
     */
    default boolean isPermissionValid() {
        return getBasePermission() != null && ! getBasePermission().isBlank() && ! getBasePermission().equalsIgnoreCase(CommandBuilder.NULL);
    }

    /**
     * Checks whether the given sender has permission to execute this command.
     *
     * @param sender the command sender to check
     * @return true if the sender has the required permission
     */
    default boolean hasPermission(CommandSender sender) {
        if (! isPermissionValid()) return defaultPermissionFallback();

        return sender.hasPermission(getBasePermission());
    }

    /**
     * Returns the default permission fallback value when no valid permission is set.
     *
     * @return true by default, allowing execution when no permission is configured
     */
    default boolean defaultPermissionFallback() {
        return true;
    }

    /**
     * Returns the success result constant.
     *
     * @return the success boolean value
     */
    default boolean success() {
        return CommandResult.SUCCESS;
    }

    /**
     * Returns the failure result constant.
     *
     * @return the failure boolean value
     */
    default boolean failure() {
        return CommandResult.FAILURE;
    }

    /**
     * Returns the error result constant.
     *
     * @return the error boolean value
     */
    default boolean error() {
        return CommandResult.ERROR;
    }

    /**
     * Returns the default result for when a sender lacks permission.
     *
     * @return the failure boolean value
     */
    default boolean defaultNoPermission() {
        return failure();
    }

    /**
     * Handles the case when a sender does not have permission to execute this command
     * by sending a no-permission message and returning a failure result.
     *
     * @param sender the command sender who lacks permission
     * @return the no-permission result value
     */
    default boolean handleNoPermission(CommandSender sender) {
        SenderUtils.getSender(sender).sendMessage("&cYou do not have permission to execute this command.");
        return defaultNoPermission();
    }

    /**
     * Executes this command by delegating to onCommand if this instance is a Command.
     *
     * @param sender the command sender
     * @param label the alias used to invoke the command
     * @param args the command arguments
     * @return true if the command executed successfully, false otherwise
     */
    default boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (! (this instanceof Command)) return false;
        Command cmd = (Command) this;

        return onCommand(sender, cmd, label, args);
    }

    /**
     * Handles command execution with permission checking and context delegation.
     *
     * @param sender the command sender
     * @param command the Bukkit command
     * @param label the alias used to invoke the command
     * @param args the command arguments
     * @return true if the command executed successfully
     */
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

    /**
     * Executes the command logic with individual parameters. Delegates to the context-based method.
     *
     * @param sender the command sender
     * @param command the Bukkit command
     * @param label the alias used to invoke the command
     * @param args the command arguments
     * @return true if the command executed successfully
     */
    default boolean command(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return command(new CommandContext(sender, command, label, args));
    }

    /**
     * Executes the command logic using a CommandContext. Override this to implement command behavior.
     *
     * @param ctx the command context containing sender, command, label, and arguments
     * @return true if the command executed successfully; false by default
     */
    default boolean command(CommandContext ctx) {
        return false;
    }

    /**
     * Handles tab completion with permission-independent processing and partial input filtering.
     *
     * @param sender the command sender
     * @param command the Bukkit command
     * @param label the alias used to invoke the command
     * @param args the current command arguments
     * @return a list of tab completion suggestions, or an empty list
     */
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

    /**
     * Provides tab completion suggestions using a CommandContext. Override this to implement tab completion.
     *
     * @param ctx the command context containing sender, command, label, and arguments
     * @return a set of tab completion suggestions; empty by default
     */
    default ConcurrentSkipListSet<String> tabComplete(CommandContext ctx) {
        return new ConcurrentSkipListSet<>();
    }

    /**
     * Provides tab completion suggestions with individual parameters. Delegates to the context-based method.
     *
     * @param sender the command sender
     * @param command the Bukkit command
     * @param label the alias used to invoke the command
     * @param args the current command arguments
     * @return a set of tab completion suggestions
     */
    default ConcurrentSkipListSet<String> tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return tabComplete(new CommandContext(sender, command, label, args));
    }

    /**
     * Gets online player names that match the partial input at the specified argument index.
     *
     * @param ctx the command context
     * @param argIndex the index of the argument to use for partial matching
     * @return a set of matching player names
     */
    default ConcurrentSkipListSet<String> getPlayerNamesForArg(CommandContext ctx, int argIndex) {
        return CommandHandler.getPlayerNamesForArg(ctx, argIndex);
    }
}
