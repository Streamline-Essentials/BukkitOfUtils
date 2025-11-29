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

public class CommandHandler {
    @Getter @Setter
    private static ConcurrentSkipListSet<BetterCommand> loadedCommands = new ConcurrentSkipListSet<>();

    private static CommandMap COMMAND_MAP = null;

    private static void setupCommandMap() {
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            COMMAND_MAP = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CommandMap getCommandMap() {
        if (COMMAND_MAP == null) {
            setupCommandMap();
        }
        return COMMAND_MAP;
    }

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

    public static Optional<BetterCommand> getCommand(String identifier) {
        return getLoadedCommands().stream().filter(command -> command.getIdentifier().equalsIgnoreCase(identifier)).findFirst();
    }

    public static Optional<BetterCommand> getCommand(String commandName, JavaPlugin provider) {
        return getCommand(getIdentifier(commandName, provider));
    }

    public static boolean isCommandRegistered(String identifier) {
        return getCommand(identifier).isPresent();
    }

    public static boolean isCommandRegistered(String commandName, JavaPlugin provider) {
        return getCommand(commandName, provider).isPresent();
    }

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

    public static void syncCommands() {
        try {
            VersionTool.syncCommands();
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logDebugWithInfo("An unknown error occurred while syncing commands: ", e);
        }
    }

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
