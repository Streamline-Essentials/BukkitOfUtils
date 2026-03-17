package host.plas.bou;

import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import host.plas.bou.events.ListenerConglomerate;
import host.plas.bou.events.callbacks.DisableCallback;
import host.plas.bou.events.self.plugin.PluginDisableEvent;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.items.ItemFactory;
import host.plas.bou.items.retrievables.RetrievableItem;
import host.plas.bou.items.retrievables.RetrievableKey;
import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.DatabaseUtils;
import host.plas.bou.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import gg.drak.thebase.async.SyncInstance;
import gg.drak.thebase.async.ThreadHolder;
import gg.drak.thebase.async.WithSync;
import gg.drak.thebase.events.BaseEventHandler;
import gg.drak.thebase.events.BaseEventListener;
import gg.drak.thebase.objects.Identified;
import gg.drak.thebase.objects.handling.IEventable;
import gg.drak.thebase.objects.handling.derived.IModifierEventable;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Base plugin class that extends {@link JavaPlugin} and provides enhanced functionality
 * including event listener management, logging utilities, item factory access,
 * synchronization support, and integration with the BukkitOfUtils framework.
 * All plugins using BukkitOfUtils should extend this class instead of {@link JavaPlugin}.
 */
@Getter @Setter
public class BetterPlugin extends JavaPlugin implements IModifierEventable, Identified, WithSync, BaseEventListener, ListenerConglomerate {
    @Getter
    private final ModifierType modifierType;

    /**
     * The synchronization instance used for thread-safe operations.
     * @param syncInstance the sync instance to set
     * @return the current sync instance
     */
    @Getter @Setter
    private static SyncInstance syncInstance;

    /**
     * Retrieves the base BukkitOfUtils instance from the BaseManager.
     *
     * @return the base BukkitOfUtils plugin instance
     */
    public static BukkitOfUtils getBaseInstance() {
        return BaseManager.getBaseInstance();
    }

    /**
     * The task scheduler used for scheduling plugin tasks.
     * @param scheduler the task scheduler to set
     * @return the current task scheduler
     */
    @Getter @Setter
    private static TaskScheduler scheduler;

    /**
     * {@inheritDoc}
     * Returns the plugin's name as its identifier.
     *
     * @return the plugin name
     */
    @Override
    public String getIdentifier() {
        return getName();
    }

    /**
     * Constructs a new BetterPlugin instance, initializing the sync instance,
     * registering it with the thread holder, setting the modifier type, and
     * invoking the base construction callback.
     */
    public BetterPlugin() {
        syncInstance = new SyncInstance(this, this);
        ThreadHolder.register(syncInstance);

        modifierType = ModifierType.PLUGIN;

        onBaseConstruct();
    }

    /**
     * Called during plugin construction. Override to perform custom initialization
     * during the constructor phase.
     */
    public void onBaseConstruct() {}

    /**
     * Called when the plugin is loaded. Override to perform custom logic during the load phase.
     */
    public void onBaseLoad() {}

    /**
     * Called at the start of plugin enabling, before other initialization occurs.
     * Override to perform custom logic during the early enable phase.
     */
    public void onBaseEnabling() {}

    /**
     * Called after the plugin has been fully enabled and all base initialization is complete.
     * Override to perform custom logic after enabling.
     */
    public void onBaseEnabled() {}

    /**
     * Called when the plugin is being disabled. Override to perform custom cleanup logic.
     */
    public void onBaseDisable() {}

    /**
     * {@inheritDoc}
     * Delegates to {@link #onBaseLoad()}.
     */
    @Override
    public void onLoad() {
        onBaseLoad();
    }

    /**
     * {@inheritDoc}
     * Registers self as a listener, calls {@link #onBaseEnabling()}, performs base manager
     * initialization for non-BukkitOfUtils plugins, then calls {@link #onBaseEnabled()}.
     */
    @Override
    public void onEnable() {
        registerSelfListener();

        onBaseEnabling();

        if (! (this instanceof BukkitOfUtils)) {
            BaseManager.otherInit(this);
        }

        onBaseEnabled();
    }

    /**
     * {@inheritDoc}
     * Calls {@link #onBaseDisable()}, fires a {@link PluginDisableEvent}, and unregisters
     * self as a listener.
     */
    @Override
    public void onDisable() {
        onBaseDisable();

        PluginDisableEvent event = new PluginDisableEvent(this).fire();

        unregisterSelfListener();
    }

    /**
     * Retrieves the item factory associated with the given key for this plugin.
     *
     * @param key the key identifying the retrievable item factory
     * @return an Optional containing the RetrievableItem if found, or empty otherwise
     */
    public Optional<RetrievableItem> getFactory(String key) {
        return ItemFactory.getFactory(getRetrievableKey(key));
    }

    /**
     * Retrieves an ItemStack from the item factory for the given key.
     *
     * @param key the key identifying the item
     * @return an Optional containing the ItemStack if found, or empty otherwise
     */
    public Optional<ItemStack> getItem(String key) {
        return ItemFactory.getItem(getRetrievableKey(key));
    }

    /**
     * Registers an item factory with the given key for this plugin.
     *
     * @param key the key to associate with the item factory
     * @param item the retrievable item to register
     */
    public void registerFactory(String key, RetrievableItem item) {
        ItemFactory.registerFactory(getRetrievableKey(key), item);
    }

    /**
     * Creates a RetrievableKey scoped to this plugin for the given key string.
     *
     * @param key the key string to wrap
     * @return a RetrievableKey associated with this plugin and the given key
     */
    public RetrievableKey getRetrievableKey(String key) {
        return RetrievableKey.of(this, key);
    }

    /**
     * Registers this plugin as both a Bukkit listener and a BaseEventListener.
     */
    public void registerSelfListener() {
        registerListener((Listener) this);
        registerListener((BaseEventListener) this);
    }

    /**
     * Unregisters this plugin from both Bukkit and BaseEvent listener systems.
     */
    public void unregisterSelfListener() {
        unregisterListener((Listener) this);
        unregisterListener((BaseEventListener) this);
    }

    /**
     * Registers a ListenerConglomerate as both a Bukkit listener and a BaseEventListener.
     *
     * @param listener the listener conglomerate to register
     */
    public void registerListenerConglomerate(ListenerConglomerate listener) {
        registerListener((Listener) listener);
        registerListener((BaseEventListener) listener);
    }

    /**
     * Unregisters a ListenerConglomerate from both Bukkit and BaseEvent listener systems.
     *
     * @param listener the listener conglomerate to unregister
     */
    public void unregisterListenerConglomerate(ListenerConglomerate listener) {
        unregisterListener((Listener) listener);
        unregisterListener((BaseEventListener) listener);
    }

    /**
     * Registers a BaseEventListener with the BaseEventHandler.
     *
     * @param listener the base event listener to register
     */
    public void registerListener(BaseEventListener listener) {
        BaseEventHandler.bake(listener, this);
    }

    /**
     * Unregisters a BaseEventListener from the BaseEventHandler.
     *
     * @param listener the base event listener to unregister
     */
    public void unregisterListener(BaseEventListener listener) {
        BaseEventHandler.unbake(listener);
    }

    /**
     * Unregisters all BaseEvent listeners associated with this plugin.
     */
    public void unregisterAllBaseListeners() {
        BaseEventHandler.unbake((IEventable) this);
    }

    /**
     * Registers a Bukkit event listener with the Bukkit plugin manager.
     *
     * @param listener the Bukkit listener to register
     */
    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    /**
     * Unregisters a Bukkit event listener from all handler lists.
     *
     * @param listener the Bukkit listener to unregister
     */
    public void unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    /**
     * Unregisters all Bukkit event listeners associated with this plugin.
     */
    public void unregisterAllBukkitListeners() {
        HandlerList.unregisterAll((Plugin) this);
    }

    /**
     * Unregisters all listeners (both BaseEvent and Bukkit) associated with this plugin.
     */
    public void unregisterAllListeners() {
        unregisterAllBaseListeners();
        unregisterAllBukkitListeners();
    }

    /**
     * Creates a callback that is invoked when any plugin is disabled.
     *
     * @param consumer the consumer to execute when a plugin disable event occurs
     * @return the created DisableCallback
     */
    public static DisableCallback subscribeDisable(Consumer<PluginDisableEvent> consumer) {
        return new DisableCallback(consumer);
    }

    /**
     * Creates a callback that is invoked only when this specific plugin is disabled.
     *
     * @param consumer the consumer to execute when this plugin's disable event occurs
     * @return the created DisableCallback
     */
    public DisableCallback subscribeDisableIfSame(Consumer<PluginDisableEvent> consumer) {
        return new DisableCallback(c -> {
            if (c.getPlugin().equals(this)) consumer.accept(c);
        });
    }

    /**
     * Returns the plugin identifier with a color code prefix indicating its enabled state.
     * Green ({@code &a}) if enabled, red ({@code &c}) if disabled.
     *
     * @return the colorized identifier string
     */
    public String getColorizedIdentifier() {
        if (isEnabled()) {
            return "&a" + getIdentifier();
        } else {
            return "&c" + getIdentifier();
        }
    }

    /**
     * Builds a formatted information string about this plugin, including its name,
     * enabled/disabled status, version, and associated databases.
     *
     * @return a multi-line formatted string containing plugin information
     */
    public String getAsInfoComponent() {
        StringBuilder builder = new StringBuilder();
        builder.append("&7- &b").append(getIdentifier()).append(" &7(").append(isEnabled() ? "&aEnabled" : "&cDisabled").append("&7)").append("\n");
        builder.append("  &f> &eVersion&7: &b").append(getDescription().getVersion()).append("\n");
        builder.append("  &f> &eDatabase&7(&es&7)&7:").append("\n");
        if (! DatabaseUtils.hasAny(getIdentifier())) builder.append("   &f- &cNo databases.");
        else {
            DatabaseUtils.get(getIdentifier()).forEach(db -> {
                builder.append("    &f+ &bID&7: &a").append(db.getId());
                builder.append("    &f+ &bType&7: &c").append(db.getConnectorSet().getType());
            });
        }
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @return always {@code true}, as this is a plugin
     */
    @Override
    public boolean isPlugin() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return always {@code false}, as this is not a mod
     */
    @Override
    public boolean isMod() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return always {@code false}, as this is not a Streamline instance
     */
    @Override
    public boolean isStreamline() {
        return false;
    }

    /**
     * {@inheritDoc}
     * Creates the plugin's data folder if it does not already exist.
     */
    @Override
    public void initializeDataFolder() {
        if (! getDataFolder().mkdirs()) {
            BukkitOfUtils.getInstance().logWarning("Could not create data folder for " + getIdentifier() + "!");
        }
    }

    /**
     * Returns the formatted log prefix for this plugin, used in log messages.
     *
     * @return the log prefix string with color codes
     */
    public String getLogPrefix() {
        return "&9[&c" + getIdentifier() + "&9] &f";
    }

    /**
     * Logs an informational message for this plugin.
     *
     * @param message the message to log
     */
    public void logInfo(String message) {
        MessageUtils.logInfo(message, this);
    }

    /**
     * Logs a warning message for this plugin.
     *
     * @param message the message to log
     */
    public void logWarning(String message) {
        MessageUtils.logWarning(message, this);
    }

    /**
     * Logs a severe message for this plugin.
     *
     * @param message the message to log
     */
    public void logSevere(String message) {
        MessageUtils.logSevere(message, this);
    }

    /**
     * Logs a debug message for this plugin.
     *
     * @param message the message to log
     */
    public void logDebug(String message) {
        MessageUtils.logDebug(message, this);
    }

    /**
     * Logs stack trace elements at the info level for this plugin.
     *
     * @param stackTraceElements the stack trace elements to log
     */
    public void logInfo(StackTraceElement[] stackTraceElements) {
        MessageUtils.logInfo(stackTraceElements, this);
    }

    /**
     * Logs stack trace elements at the warning level for this plugin.
     *
     * @param stackTraceElements the stack trace elements to log
     */
    public void logWarning(StackTraceElement[] stackTraceElements) {
        MessageUtils.logWarning(stackTraceElements, this);
    }

    /**
     * Logs stack trace elements at the severe level for this plugin.
     *
     * @param stackTraceElements the stack trace elements to log
     */
    public void logSevere(StackTraceElement[] stackTraceElements) {
        MessageUtils.logSevere(stackTraceElements, this);
    }

    /**
     * Logs stack trace elements at the debug level for this plugin.
     *
     * @param stackTraceElements the stack trace elements to log
     */
    public void logDebug(StackTraceElement[] stackTraceElements) {
        MessageUtils.logDebug(stackTraceElements, this);
    }

    /**
     * Logs a throwable at the info level for this plugin.
     *
     * @param throwable the throwable to log
     */
    public void logInfo(Throwable throwable) {
        MessageUtils.logInfo(throwable, this);
    }

    /**
     * Logs a throwable at the warning level for this plugin.
     *
     * @param throwable the throwable to log
     */
    public void logWarning(Throwable throwable) {
        MessageUtils.logWarning(throwable, this);
    }

    /**
     * Logs a throwable at the severe level for this plugin.
     *
     * @param throwable the throwable to log
     */
    public void logSevere(Throwable throwable) {
        MessageUtils.logSevere(throwable, this);
    }

    /**
     * Logs a throwable at the debug level for this plugin.
     *
     * @param throwable the throwable to log
     */
    public void logDebug(Throwable throwable) {
        MessageUtils.logDebug(throwable, this);
    }

    /**
     * Logs a message with a throwable at the info level for this plugin.
     *
     * @param message the message to log
     * @param throwable the throwable to log
     */
    public void logInfo(String message, Throwable throwable) {
        MessageUtils.logInfo(message, throwable, this);
    }

    /**
     * Logs a message with a throwable at the warning level for this plugin.
     *
     * @param message the message to log
     * @param throwable the throwable to log
     */
    public void logWarning(String message, Throwable throwable) {
        MessageUtils.logWarning(message, throwable, this);
    }

    /**
     * Logs a message with a throwable at the severe level for this plugin.
     *
     * @param message the message to log
     * @param throwable the throwable to log
     */
    public void logSevere(String message, Throwable throwable) {
        MessageUtils.logSevere(message, throwable, this);
    }

    /**
     * Logs a message with a throwable at the debug level for this plugin.
     *
     * @param message the message to log
     * @param throwable the throwable to log
     */
    public void logDebug(String message, Throwable throwable) {
        MessageUtils.logDebug(message, throwable, this);
    }

    /**
     * Logs a message with additional throwable info at the info level for this plugin.
     *
     * @param message the message to log
     * @param throwable the throwable whose info to include
     */
    public void logInfoWithInfo(String message, Throwable throwable) {
        MessageUtils.logInfoWithInfo(message, throwable, this);
    }

    /**
     * Logs a message with additional throwable info at the warning level for this plugin.
     *
     * @param message the message to log
     * @param throwable the throwable whose info to include
     */
    public void logWarningWithInfo(String message, Throwable throwable) {
        MessageUtils.logWarningWithInfo(message, throwable, this);
    }

    /**
     * Logs a message with additional throwable info at the severe level for this plugin.
     *
     * @param message the message to log
     * @param throwable the throwable whose info to include
     */
    public void logSevereWithInfo(String message, Throwable throwable) {
        MessageUtils.logSevereWithInfo(message, throwable, this);
    }

    /**
     * Logs a message with additional throwable info at the debug level for this plugin.
     *
     * @param message the message to log
     * @param throwable the throwable whose info to include
     */
    public void logDebugWithInfo(String message, Throwable throwable) {
        MessageUtils.logDebugWithInfo(message, throwable, this);
    }

    /**
     * {@inheritDoc}
     * Executes the given runnable on the main server thread.
     *
     * @param runnable the task to execute synchronously
     */
    @Override
    public void sync(Runnable runnable) {
        TaskManager.runTask(runnable);
    }

    /**
     * {@inheritDoc}
     * Executes the given runnable on the main server thread after the specified delay.
     *
     * @param runnable the task to execute synchronously
     * @param delay the delay in ticks before execution
     */
    @Override
    public void sync(Runnable runnable, long delay) {
        TaskManager.runTaskLater(runnable, delay);
    }

    /**
     * {@inheritDoc}
     * Executes the given runnable on the main server thread repeatedly with the specified
     * delay and period.
     *
     * @param runnable the task to execute synchronously
     * @param delay the delay in ticks before the first execution
     * @param period the period in ticks between subsequent executions
     */
    @Override
    public void sync(Runnable runnable, long delay, long period) {
        TaskManager.runTaskTimer(runnable, delay, period);
    }

    /**
     * {@inheritDoc}
     * Checks whether the current thread is the main server thread.
     *
     * @return {@code true} if the current thread is the main server thread
     */
    @Override
    public boolean isSync() {
        return TaskManager.isThreadSync();
    }
}
