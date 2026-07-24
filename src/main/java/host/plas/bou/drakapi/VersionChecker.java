package host.plas.bou.drakapi;

import host.plas.bou.BetterPlugin;
import host.plas.bou.scheduling.TaskManager;
import host.plas.bou.utils.MessageUtils;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Built-in Modrinth version checker backed by the Drak API.
 * Performs network calls off the main thread and caches the latest result per project.
 *
 * @see <a href="https://api.drak.gg/api-docs">Drak API docs</a>
 */
public final class VersionChecker {
    public static final String BUKKITOFUTILS_SLUG = "bukkitofutils";

    private static final Map<String, VersionCheckResult> CACHE = new ConcurrentHashMap<>();

    private VersionChecker() {
    }

    /**
     * Synchronously checks a project version against Modrinth via the Drak API.
     * Prefer {@link #checkAsync(String, String)} on the main server thread.
     *
     * @param idOrSlug       Modrinth project ID or slug
     * @param currentVersion installed version string
     * @return parsed version check result
     */
    public static VersionCheckResult check(String idOrSlug, String currentVersion) {
        VersionCheckResult result = DrakAPI.checkVersion(idOrSlug, currentVersion);
        if (idOrSlug != null && result != null && result.isSuccess()) {
            CACHE.put(idOrSlug.toLowerCase(), result);
        }
        return result;
    }

    /**
     * Checks the given plugin's description version against the Modrinth project.
     *
     * @param plugin   plugin providing the installed version
     * @param idOrSlug Modrinth project ID or slug
     * @return parsed version check result
     */
    public static VersionCheckResult check(Plugin plugin, String idOrSlug) {
        if (plugin == null) {
            return VersionCheckResult.failure("Plugin is null.");
        }
        return check(idOrSlug, plugin.getDescription().getVersion());
    }

    /**
     * Asynchronously checks a project version.
     *
     * @param idOrSlug       Modrinth project ID or slug
     * @param currentVersion installed version string
     * @return future completing with the check result
     */
    public static CompletableFuture<VersionCheckResult> checkAsync(String idOrSlug, String currentVersion) {
        return CompletableFuture.supplyAsync(() -> check(idOrSlug, currentVersion));
    }

    /**
     * Asynchronously checks a plugin against Modrinth.
     *
     * @param plugin   plugin providing the installed version
     * @param idOrSlug Modrinth project ID or slug
     * @return future completing with the check result
     */
    public static CompletableFuture<VersionCheckResult> checkAsync(Plugin plugin, String idOrSlug) {
        if (plugin == null) {
            return CompletableFuture.completedFuture(VersionCheckResult.failure("Plugin is null."));
        }
        return checkAsync(idOrSlug, plugin.getDescription().getVersion());
    }

    /**
     * Asynchronously checks and logs the result via {@link MessageUtils}.
     *
     * @param plugin   plugin to check / log for
     * @param idOrSlug Modrinth project ID or slug
     * @return future completing with the check result
     */
    public static CompletableFuture<VersionCheckResult> checkAndLog(BetterPlugin plugin, String idOrSlug) {
        return checkAsync(plugin, idOrSlug).whenComplete((result, error) -> {
            Runnable announceTask = () -> {
                if (error != null) {
                    MessageUtils.logWarning("Version check failed for " + safeName(plugin) + ": " + error.getMessage(), plugin);
                    return;
                }
                announce(plugin, result);
            };
            runOnMain(announceTask);
        });
    }

    /**
     * Asynchronously checks and invokes {@code callback} on the main thread.
     *
     * @param plugin   plugin providing the installed version
     * @param idOrSlug Modrinth project ID or slug
     * @param callback consumer invoked on the main thread with the result
     * @return future completing with the check result
     */
    public static CompletableFuture<VersionCheckResult> checkThen(Plugin plugin, String idOrSlug,
                                                                  Consumer<VersionCheckResult> callback) {
        return checkAsync(plugin, idOrSlug).whenComplete((result, error) -> {
            Runnable run = () -> {
                if (callback == null) return;
                if (error != null) {
                    callback.accept(VersionCheckResult.failure(error.getMessage()));
                } else {
                    callback.accept(result);
                }
            };
            runOnMain(run);
        });
    }

    /**
     * Returns the last successful cached result for a project slug/id, if any.
     *
     * @param idOrSlug Modrinth project ID or slug
     * @return cached result, or {@code null} if none
     */
    @Nullable
    public static VersionCheckResult getCached(String idOrSlug) {
        if (idOrSlug == null) return null;
        return CACHE.get(idOrSlug.toLowerCase());
    }

    /**
     * Clears all cached version check results.
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Logs a human-readable summary of a version check result.
     *
     * @param plugin plugin context for logging
     * @param result check result to announce
     */
    public static void announce(BetterPlugin plugin, VersionCheckResult result) {
        if (result == null) {
            MessageUtils.logWarning("Version check returned no result.", plugin);
            return;
        }
        if (!result.isSuccess()) {
            MessageUtils.logWarning("Version check failed"
                    + (result.getMessage() == null ? "." : ": " + result.getMessage()), plugin);
            return;
        }

        String name = safeName(plugin);
        switch (result.getStatus()) {
            case UP_TO_DATE:
                MessageUtils.logInfo("&a" + name + " &7is up to date (&a" + result.getCurrentVersion() + "&7).", plugin);
                break;
            case AHEAD:
                MessageUtils.logInfo("&e" + name + " &7is ahead of Modrinth (&e" + result.getCurrentVersion()
                        + " &7vs latest &a" + result.getLatestVersion() + "&7).", plugin);
                break;
            case BEHIND:
                MessageUtils.logWarning("&c" + name + " &7is outdated (&c" + result.getCurrentVersion()
                        + " &7→ &a" + result.getLatestVersion() + "&7).", plugin);
                if (result.getDownloadUrl() != null && !result.getDownloadUrl().isBlank()) {
                    MessageUtils.logWarning("&7Download: &b" + result.getDownloadUrl(), plugin);
                } else {
                    MessageUtils.logWarning("&7Update at: &bhttps://modrinth.com/plugin/"
                            + (result.getProjectSlug() == null ? BUKKITOFUTILS_SLUG : result.getProjectSlug()), plugin);
                }
                break;
            default:
                MessageUtils.logWarning("Version check for " + name + " returned an unknown status.", plugin);
                break;
        }
    }

    private static void runOnMain(Runnable runnable) {
        if (TaskManager.isThreadSync()) {
            runnable.run();
        } else {
            TaskManager.runTask(runnable);
        }
    }

    private static String safeName(Plugin plugin) {
        return plugin == null ? "Unknown" : plugin.getName();
    }
}
