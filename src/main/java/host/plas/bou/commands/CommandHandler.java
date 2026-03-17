package host.plas.bou.commands;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.utils.VersionTool;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Central handler for managing command registration, unregistration, and lookup.
 * Provides access to the Bukkit server command map via reflection and maintains
 * a set of loaded BetterCommand instances.
 */
public class CommandHandler {
    /**
     * Private constructor to prevent instantiation.
     */
    private CommandHandler() {
    }

    /**
     * The set of currently loaded commands.
     *
     * @param loadedCommands the set of loaded commands to set
     * @return the set of loaded commands
     */
    @Getter @Setter
    private static ConcurrentSkipListSet<BetterCommand> loadedCommands = new ConcurrentSkipListSet<>();

    private static CommandMap COMMAND_MAP = null;

    /**
     * Initializes the command map by reflecting into the Bukkit server instance.
     */
    private static void setupCommandMap() {
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            COMMAND_MAP = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the server command map, initializing it if necessary.
     *
     * @return the Bukkit CommandMap
     */
    public static CommandMap getCommandMap() {
        if (COMMAND_MAP == null) {
            setupCommandMap();
        }
        return COMMAND_MAP;
    }

    /**
     * Initializes the command handler by ensuring the command map is set up.
     */
    public static void init() {
        getCommandMap(); // Ensure command map is set up
    }

    /**
     * Registers a command with the CommandHandler.
     * Will unregister the command if it is already registered and re-register it.
     * @param command The command to register.
     * @return Whether the command was successfully registered.
     */
    public static boolean registerCommand(BetterCommand command) {
        if (isCommandRegistered(command.getIdentifier())) {
            unregisterCommand(command);
        }

        return getLoadedCommands().add(command);
    }

    /**
     * Unregisters a command with the CommandHandler.
     * @param command The command to unregister.
     * @return Whether the command was successfully unregistered. Will be false if the command is not registered.
     */
    public static boolean unregisterCommand(BetterCommand command) {
        if (! isCommandRegistered(command.getIdentifier())) return false;

        return getLoadedCommands().removeIf(loadedCommand -> loadedCommand.getIdentifier().equalsIgnoreCase(command.getIdentifier()));
    }

    /**
     * Retrieves a registered command by its identifier.
     *
     * @param identifier the command identifier to look up
     * @return an Optional containing the command if found, or empty otherwise
     */
    public static Optional<BetterCommand> getCommand(String identifier) {
        return getLoadedCommands().stream().filter(command -> command.getIdentifier().equalsIgnoreCase(identifier)).findFirst();
    }

    /**
     * Retrieves a registered command by its name and provider plugin.
     *
     * @param commandName the name of the command
     * @param provider the plugin that provides the command
     * @return an Optional containing the command if found, or empty otherwise
     */
    public static Optional<BetterCommand> getCommand(String commandName, JavaPlugin provider) {
        return getCommand(getIdentifier(commandName, provider));
    }

    /**
     * Checks whether a command with the given identifier is registered.
     *
     * @param identifier the command identifier to check
     * @return true if the command is registered
     */
    public static boolean isCommandRegistered(String identifier) {
        return getCommand(identifier).isPresent();
    }

    /**
     * Checks whether a command with the given name and provider is registered.
     *
     * @param commandName the name of the command
     * @param provider the plugin that provides the command
     * @return true if the command is registered
     */
    public static boolean isCommandRegistered(String commandName, JavaPlugin provider) {
        return getCommand(commandName, provider).isPresent();
    }

    /**
     * Generates a unique identifier for a command based on its name and provider.
     *
     * @param commandName the name of the command
     * @param provider the plugin that provides the command
     * @return the identifier in the format "pluginName:commandName"
     */
    public static String getIdentifier(String commandName, JavaPlugin provider) {
//        return getCommandKey(commandName, provider).toString(); // toString() gets "namespace:key".
        return provider.getName() + ":" + commandName;
    }

//    public static NamespacedKey getCommandKey(String commandName, JavaPlugin provider) {
//        return PluginUtils.getPluginKey(provider, commandName);
//    }

    /**
     * Register command(s) into the server command map.
     *
     * @param <C> the type of BuildableCommand to register
     * @param commands The command(s) to register
     */
    public static <C extends BuildableCommand> void registerCommands(C... commands) {
        // Get the commandMap
        try {
            // Register all the commands into the map
            for (final C command : commands) {
                try {
                    getCommandMap().register(command.getCommandName(), command.getLabel(), command);

                    try {
                        getCommandMap().register("bou", command);
                    } catch (Throwable e) {
                        BukkitOfUtils.getInstance().logDebugWithInfo("Failed to register command: " + command.getLabel(), e);
                    }
                } catch (Throwable e) {
                    BukkitOfUtils.getInstance().logDebugWithInfo("Failed to register command: " + command.getLabel(), e);
                }
            }

            CompletableFuture.runAsync(CommandHandler::syncCommands);
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Unregister command(s) from the server command map.
     *
     * @param commands The command(s) to unregister
     */
    public static void unregisterCommands(String... commands) {
        // Get the commandMap
        try {
            // Register all the commands into the map
            for (final String command : commands) {
                Command com = getCommandMap().getCommand(command);
                if (com == null) {
                    BukkitOfUtils.getInstance().logDebug("Tried to unregister a command that does not exist: " + command);
                    continue;
                }

                try {
                    com.unregister(getCommandMap());
                } catch (Throwable e) {
                    BukkitOfUtils.getInstance().logDebugWithInfo("Failed to unregister command: " + command, e);
                }

                try {
                    VersionTool.unregisterKnownCommand(com);
                } catch (Throwable e) {
                    BukkitOfUtils.getInstance().logDebugWithInfo("Failed to unregister command: " + command, e);
                }
            }

            CompletableFuture.runAsync(CommandHandler::syncCommands);
        } catch (final Exception e) {
            BukkitOfUtils.getInstance().logWarningWithInfo("Failed to unregister commands: ", e);
        }
    }

    /**
     * Synchronizes the server command map with registered commands.
     * Delegates to VersionTool for version-specific synchronization.
     */
    public static void syncCommands() {
        try {
            VersionTool.syncCommands();
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logDebugWithInfo("An unknown error occurred while syncing commands: ", e);
        }
    }

    /**
     * Gets online player names that match the partial input at the specified argument index
     * in the given command context.
     *
     * @param ctx the command context
     * @param argIndex the index of the argument to use for partial matching
     * @return a set of matching online player names
     */
    public static ConcurrentSkipListSet<String> getPlayerNamesForArg(CommandContext ctx, int argIndex) {
        ConcurrentSkipListSet<String> results = new ConcurrentSkipListSet<>();

        String partial = ctx.getStringArg(argIndex).toLowerCase();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase().startsWith(partial)) {
                results.add(p.getName());
            }
        }

        return results;
    }
}
