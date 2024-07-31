package host.plas.bou.commands;

import host.plas.bou.utils.PluginUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class CommandHandler {
    @Getter @Setter
    private static ConcurrentSkipListSet<ComplexCommand> loadedCommands = new ConcurrentSkipListSet<>();

    /**
     * Registers a command with the CommandHandler.
     * Will unregister the command if it is already registered and re-register it.
     * @param command The command to register.
     * @return Whether the command was successfully registered.
     */
    public static boolean registerCommand(ComplexCommand command) {
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
    public static boolean unregisterCommand(ComplexCommand command) {
        if (! isCommandRegistered(command.getIdentifier())) return false;

        return getLoadedCommands().removeIf(loadedCommand -> loadedCommand.getIdentifier().equalsIgnoreCase(command.getIdentifier()));
    }

    public static Optional<ComplexCommand> getCommand(String identifier) {
        return getLoadedCommands().stream().filter(command -> command.getIdentifier().equalsIgnoreCase(identifier)).findFirst();
    }

    public static Optional<ComplexCommand> getCommand(String commandName, JavaPlugin provider) {
        return getCommand(getIdentifier(commandName, provider));
    }

    public static boolean isCommandRegistered(String identifier) {
        return getCommand(identifier).isPresent();
    }

    public static boolean isCommandRegistered(String commandName, JavaPlugin provider) {
        return getCommand(commandName, provider).isPresent();
    }

    public static String getIdentifier(String commandName, JavaPlugin provider) {
        return getCommandKey(commandName, provider).toString(); // toString() gets "namespace:key".
    }

    public static NamespacedKey getCommandKey(String commandName, JavaPlugin provider) {
        return PluginUtils.getPluginKey(provider, commandName);
    }
}
