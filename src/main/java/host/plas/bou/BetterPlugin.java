package host.plas.bou;

import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import host.plas.bou.configs.BaseConfig;
import host.plas.bou.instances.BaseManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import tv.quaint.objects.Identified;
import tv.quaint.objects.handling.derived.IModifierEventable;

@Getter @Setter
public class BetterPlugin extends JavaPlugin implements IModifierEventable, Identified {
    @Getter
    private final ModifierType modifierType;

    @Getter @Setter
    private static BaseConfig baseConfig;

    @Getter @Setter
    private static TaskScheduler scheduler;

    @Override
    public String getIdentifier() {
        return getName();
    }

    public BetterPlugin() {
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
        onBaseEnabling();

        if (this instanceof BukkitOfUtils) BaseManager.init(this);
        else BaseManager.otherInit(this);

        onBaseEnabled();
    }

    @Override
    public void onDisable() {
        onBaseDisable();
        if (this instanceof BukkitOfUtils) BaseManager.stop();
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
            MessageUtils.logWarning("Could not create data folder for " + getIdentifier() + "!");
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
}
