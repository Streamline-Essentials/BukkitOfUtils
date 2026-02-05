package host.plas.bou;

import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import host.plas.bou.events.ListenerConglomerate;
import host.plas.bou.events.callbacks.DisableCallback;
import host.plas.bou.events.self.plugin.PluginDisableEvent;
import host.plas.bou.instances.BaseManager;
import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.DatabaseUtils;
import host.plas.bou.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
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

import java.util.function.Consumer;

@Getter @Setter
public class BetterPlugin extends JavaPlugin implements IModifierEventable, Identified, WithSync, BaseEventListener, ListenerConglomerate {
    @Getter
    private final ModifierType modifierType;

    @Getter @Setter
    private static SyncInstance syncInstance;

    public static BukkitOfUtils getBaseInstance() {
        return BaseManager.getBaseInstance();
    }

    @Getter @Setter
    private static TaskScheduler scheduler;

    @Override
    public String getIdentifier() {
        return getName();
    }

    public BetterPlugin() {
        syncInstance = new SyncInstance(this, this);
        ThreadHolder.register(syncInstance);

        modifierType = ModifierType.PLUGIN;

        onBaseConstruct();
    }

    public void onBaseConstruct() {}
    public void onBaseLoad() {}
    public void onBaseEnabling() {}
    public void onBaseEnabled() {}
    public void onBaseDisable() {}

    @Override
    public void onLoad() {
        onBaseLoad();
    }

    @Override
    public void onEnable() {
        registerSelfListener();

        onBaseEnabling();

        if (! (this instanceof BukkitOfUtils)) {
            BaseManager.otherInit(this);
        }

        onBaseEnabled();
    }

    @Override
    public void onDisable() {
        onBaseDisable();

        PluginDisableEvent event = new PluginDisableEvent(this).fire();

        unregisterSelfListener();
    }

    public void registerSelfListener() {
        registerListener((Listener) this);
        registerListener((BaseEventListener) this);
    }

    public void unregisterSelfListener() {
        unregisterListener((Listener) this);
        unregisterListener((BaseEventListener) this);
    }

    public void registerListenerConglomerate(ListenerConglomerate listener) {
        registerListener((Listener) listener);
        registerListener((BaseEventListener) listener);
    }

    public void unregisterListenerConglomerate(ListenerConglomerate listener) {
        unregisterListener((Listener) listener);
        unregisterListener((BaseEventListener) listener);
    }

    public void registerListener(BaseEventListener listener) {
        BaseEventHandler.bake(listener, this);
    }

    public void unregisterListener(BaseEventListener listener) {
        BaseEventHandler.unbake(listener);
    }

    public void unregisterAllBaseListeners() {
        BaseEventHandler.unbake((IEventable) this);
    }

    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    public void unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    public void unregisterAllBukkitListeners() {
        HandlerList.unregisterAll((Plugin) this);
    }

    public void unregisterAllListeners() {
        unregisterAllBaseListeners();
        unregisterAllBukkitListeners();
    }

    public static DisableCallback subscribeDisable(Consumer<PluginDisableEvent> consumer) {
        return new DisableCallback(consumer);
    }

    public DisableCallback subscribeDisableIfSame(Consumer<PluginDisableEvent> consumer) {
        return new DisableCallback(c -> {
            if (c.getPlugin().equals(this)) consumer.accept(c);
        });
    }

    public String getColorizedIdentifier() {
        if (isEnabled()) {
            return "&a" + getIdentifier();
        } else {
            return "&c" + getIdentifier();
        }
    }

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

    @Override
    public boolean isPlugin() {
        return true;
    }

    @Override
    public boolean isMod() {
        return false;
    }

    @Override
    public boolean isStreamline() {
        return false;
    }

    @Override
    public void initializeDataFolder() {
        if (! getDataFolder().mkdirs()) {
            BukkitOfUtils.getInstance().logWarning("Could not create data folder for " + getIdentifier() + "!");
        }
    }

    public String getLogPrefix() {
        return "&9[&c" + getIdentifier() + "&9] &f";
    }

    public void logInfo(String message) {
        MessageUtils.logInfo(message, this);
    }

    public void logWarning(String message) {
        MessageUtils.logWarning(message, this);
    }

    public void logSevere(String message) {
        MessageUtils.logSevere(message, this);
    }

    public void logDebug(String message) {
        MessageUtils.logDebug(message, this);
    }

    public void logInfo(StackTraceElement[] stackTraceElements) {
        MessageUtils.logInfo(stackTraceElements, this);
    }

    public void logWarning(StackTraceElement[] stackTraceElements) {
        MessageUtils.logWarning(stackTraceElements, this);
    }

    public void logSevere(StackTraceElement[] stackTraceElements) {
        MessageUtils.logSevere(stackTraceElements, this);
    }

    public void logDebug(StackTraceElement[] stackTraceElements) {
        MessageUtils.logDebug(stackTraceElements, this);
    }

    public void logInfo(Throwable throwable) {
        MessageUtils.logInfo(throwable, this);
    }

    public void logWarning(Throwable throwable) {
        MessageUtils.logWarning(throwable, this);
    }

    public void logSevere(Throwable throwable) {
        MessageUtils.logSevere(throwable, this);
    }

    public void logDebug(Throwable throwable) {
        MessageUtils.logDebug(throwable, this);
    }

    public void logInfo(String message, Throwable throwable) {
        MessageUtils.logInfo(message, throwable, this);
    }

    public void logWarning(String message, Throwable throwable) {
        MessageUtils.logWarning(message, throwable, this);
    }

    public void logSevere(String message, Throwable throwable) {
        MessageUtils.logSevere(message, throwable, this);
    }

    public void logDebug(String message, Throwable throwable) {
        MessageUtils.logDebug(message, throwable, this);
    }

    public void logInfoWithInfo(String message, Throwable throwable) {
        MessageUtils.logInfoWithInfo(message, throwable, this);
    }

    public void logWarningWithInfo(String message, Throwable throwable) {
        MessageUtils.logWarningWithInfo(message, throwable, this);
    }

    public void logSevereWithInfo(String message, Throwable throwable) {
        MessageUtils.logSevereWithInfo(message, throwable, this);
    }

    public void logDebugWithInfo(String message, Throwable throwable) {
        MessageUtils.logDebugWithInfo(message, throwable, this);
    }

    @Override
    public void sync(Runnable runnable) {
        TaskManager.runTask(runnable);
    }

    @Override
    public void sync(Runnable runnable, long delay) {
        TaskManager.runTaskLater(runnable, delay);
    }

    @Override
    public void sync(Runnable runnable, long delay, long period) {
        TaskManager.runTaskTimer(runnable, delay, period);
    }

    @Override
    public boolean isSync() {
        return TaskManager.isThreadSync();
    }
}
