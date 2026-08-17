package host.plas.bou.utils;

import host.plas.bou.BetterPlugin;
import host.plas.bou.BukkitOfUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Helpers for enabling, disabling, loading, and unloading Bukkit plugins at runtime.
 */
public final class PluginLifecycleHelper {
    private PluginLifecycleHelper() {
    }

    public static final class Result {
        private final boolean success;
        private final String message;
        @Nullable
        private final Plugin plugin;

        private Result(boolean success, String message, @Nullable Plugin plugin) {
            this.success = success;
            this.message = message;
            this.plugin = plugin;
        }

        public static Result ok(String message, @Nullable Plugin plugin) {
            return new Result(true, message, plugin);
        }

        public static Result fail(String message) {
            return new Result(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        @Nullable
        public Plugin getPlugin() {
            return plugin;
        }
    }

    public static boolean isBukkitOfUtils(Plugin plugin) {
        return plugin instanceof BukkitOfUtils
                || (plugin != null && plugin.getName().equalsIgnoreCase("BukkitOfUtils"));
    }

    public static Result enable(String name) {
        Plugin plugin = resolveLoaded(name);
        if (plugin == null) {
            return Result.fail("Plugin '" + name + "' is not loaded.");
        }
        if (plugin.isEnabled()) {
            return Result.ok("Plugin '" + plugin.getName() + "' is already enabled.", plugin);
        }
        Bukkit.getPluginManager().enablePlugin(plugin);
        return Result.ok("Enabled plugin '" + plugin.getName() + "'.", plugin);
    }

    public static Result disable(String name) {
        Plugin plugin = resolveLoaded(name);
        if (plugin == null) {
            return Result.fail("Plugin '" + name + "' is not loaded.");
        }
        if (isBukkitOfUtils(plugin)) {
            return Result.fail("Cannot disable BukkitOfUtils via /boup.");
        }
        if (!plugin.isEnabled()) {
            return Result.ok("Plugin '" + plugin.getName() + "' is already disabled.", plugin);
        }
        Bukkit.getPluginManager().disablePlugin(plugin);
        return Result.ok("Disabled plugin '" + plugin.getName() + "'.", plugin);
    }

    public static Result unload(String name) {
        Plugin plugin = resolveLoaded(name);
        if (plugin == null) {
            return Result.fail("Plugin '" + name + "' is not loaded.");
        }
        if (isBukkitOfUtils(plugin)) {
            return Result.fail("Cannot unload BukkitOfUtils via /boup.");
        }

        String pluginName = plugin.getName();
        PluginManager manager = Bukkit.getPluginManager();
        if (plugin.isEnabled()) {
            manager.disablePlugin(plugin);
        }

        try {
            Method unloadMethod = manager.getClass().getMethod("unloadPlugin", Plugin.class);
            Object result = unloadMethod.invoke(manager, plugin);
            boolean ok = !(result instanceof Boolean) || (Boolean) result;
            if (ok) {
                unregisterBOUPlugin(plugin);
                syncCommands();
                return Result.ok("Unloaded plugin '" + pluginName + "'.", null);
            }
        } catch (NoSuchMethodException ignored) {
            // Fall through to reflection unload for Spigot.
        } catch (Throwable t) {
            BukkitOfUtils.getInstance().logWarning("Paper unloadPlugin failed for " + pluginName + ": " + t.getMessage());
        }

        if (reflectiveUnload(plugin)) {
            unregisterBOUPlugin(plugin);
            syncCommands();
            return Result.ok("Unloaded plugin '" + pluginName + "' (reflective).", null);
        }
        return Result.fail("Disabled '" + pluginName + "' but could not fully unload it on this server.");
    }

    /**
     * Reloads a plugin in the same order used by PlugMan: disable it, remove it
     * from the server's loaded plugin state, then load and enable a fresh instance
     * from its jar. BOU plugins are also removed from BOU's plugin registry during
     * the unload phase.
     *
     * @param name the loaded plugin name or jar name
     * @return the result of the reload operation
     */
    public static Result reload(String name) {
        Plugin plugin = resolveLoaded(name);
        if (plugin == null) {
            return Result.fail("Plugin '" + name + "' is not loaded.");
        }
        if (isBukkitOfUtils(plugin)) {
            return Result.fail("Cannot reload BukkitOfUtils via /boup.");
        }

        String pluginName = plugin.getName();
        Result unloadResult = unload(pluginName);
        if (!unloadResult.isSuccess()) {
            return Result.fail("Could not reload plugin '" + pluginName + "': " + unloadResult.getMessage());
        }

        Result loadResult = load(pluginName);
        if (!loadResult.isSuccess()) {
            return Result.fail("Unloaded plugin '" + pluginName + "' but failed to reload it: "
                    + loadResult.getMessage());
        }
        return Result.ok("Reloaded plugin '" + pluginName + "'.", loadResult.getPlugin());
    }

    private static void unregisterBOUPlugin(Plugin plugin) {
        if (plugin instanceof BetterPlugin) {
            PluginUtils.unregisterPlugin((BetterPlugin) plugin);
        }
    }

    private static void registerBOUPlugin(Plugin plugin) {
        if (plugin instanceof BetterPlugin) {
            PluginUtils.registerPlugin((BetterPlugin) plugin);
        }
    }

    private static void syncCommands() {
        try {
            Method syncCommands = Bukkit.getServer().getClass().getMethod("syncCommands");
            syncCommands.invoke(Bukkit.getServer());
        } catch (Throwable ignored) {
            // Command synchronization is not available on every Bukkit version.
        }
    }

    public static Result load(String nameOrJar) {
        if (nameOrJar == null || nameOrJar.isBlank()) {
            return Result.fail("No plugin name or jar provided.");
        }

        Plugin already = resolveLoaded(stripJar(nameOrJar));
        if (already != null) {
            return Result.fail("Plugin '" + already.getName() + "' is already loaded.");
        }

        File jar = resolveJar(nameOrJar);
        if (jar == null || !jar.exists()) {
            return Result.fail("Could not find a jar for '" + nameOrJar + "' in the plugins folder.");
        }

        try {
            Plugin plugin = Bukkit.getPluginManager().loadPlugin(jar);
            if (plugin == null) {
                return Result.fail("Failed to load plugin from " + jar.getName() + ".");
            }
            plugin.onLoad();
            Bukkit.getPluginManager().enablePlugin(plugin);
            if (!plugin.isEnabled()) {
                return Result.fail("Loaded plugin '" + plugin.getName() + "' but could not enable it.");
            }
            registerBOUPlugin(plugin);
            syncCommands();
            return Result.ok("Loaded and enabled plugin '" + plugin.getName() + "' from " + jar.getName() + ".", plugin);
        } catch (InvalidPluginException | InvalidDescriptionException | UnknownDependencyException e) {
            return Result.fail("Failed to load " + jar.getName() + ": " + e.getMessage());
        } catch (Throwable t) {
            return Result.fail("Failed to load " + jar.getName() + ": " + t.getMessage());
        }
    }

    @Nullable
    public static Plugin resolveLoaded(String name) {
        if (name == null || name.isBlank()) return null;
        String clean = stripJar(name);
        Plugin plugin = Bukkit.getPluginManager().getPlugin(clean);
        if (plugin != null) return plugin;
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (p.getName().equalsIgnoreCase(clean)) return p;
        }
        Optional<BetterPlugin> better = PluginUtils.getPluginIgnoreCase(clean);
        return better.orElse(null);
    }

    public static File getPluginsDirectory() {
        if (BukkitOfUtils.getInstance() != null) {
            File parent = BukkitOfUtils.getInstance().getDataFolder().getParentFile();
            if (parent != null) return parent;
        }
        return new File("plugins");
    }

    public static List<String> listPluginJarNames() {
        List<String> names = new ArrayList<>();
        File pluginsDir = getPluginsDirectory();
        File[] jars = pluginsDir.listFiles((dir, fileName) -> fileName.toLowerCase().endsWith(".jar"));
        if (jars == null) return names;
        for (File jar : jars) {
            String base = jar.getName().substring(0, jar.getName().length() - 4);
            if (!names.contains(base)) {
                names.add(base);
            }
            String ymlName = readPluginYmlName(jar);
            if (ymlName != null && !containsIgnoreCase(names, ymlName)) {
                names.add(ymlName);
            }
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    private static boolean containsIgnoreCase(List<String> list, String value) {
        for (String s : list) {
            if (s.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    @Nullable
    private static File resolveJar(String nameOrJar) {
        File pluginsDir = getPluginsDirectory();
        String clean = nameOrJar.endsWith(".jar") ? nameOrJar : nameOrJar + ".jar";
        File direct = new File(pluginsDir, clean);
        if (direct.exists()) return direct;

        File[] jars = pluginsDir.listFiles((dir, fileName) -> fileName.toLowerCase().endsWith(".jar"));
        if (jars == null) return null;

        String needle = stripJar(nameOrJar);
        for (File jar : jars) {
            String base = jar.getName().substring(0, jar.getName().length() - 4);
            if (base.equalsIgnoreCase(needle)) return jar;
        }
        for (File jar : jars) {
            String ymlName = readPluginYmlName(jar);
            if (ymlName != null && ymlName.equalsIgnoreCase(needle)) return jar;
        }
        return null;
    }

    private static String stripJar(String name) {
        if (name == null) return "";
        return name.toLowerCase().endsWith(".jar") ? name.substring(0, name.length() - 4) : name;
    }

    @Nullable
    private static String readPluginYmlName(File jar) {
        try (JarFile jarFile = new JarFile(jar)) {
            JarEntry entry = jarFile.getJarEntry("plugin.yml");
            if (entry == null) return null;
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(jarFile.getInputStream(entry), StandardCharsets.UTF_8));
            return yaml.getString("name");
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean reflectiveUnload(Plugin plugin) {
        PluginManager manager = Bukkit.getPluginManager();
        String name = plugin.getName();
        try {
            Field pluginsField = findField(manager.getClass(), "plugins");
            pluginsField.setAccessible(true);
            List<Plugin> plugins = (List<Plugin>) pluginsField.get(manager);
            plugins.remove(plugin);

            Field lookupNamesField = findField(manager.getClass(), "lookupNames");
            lookupNamesField.setAccessible(true);
            Map<String, Plugin> lookupNames = (Map<String, Plugin>) lookupNamesField.get(manager);
            lookupNames.entrySet().removeIf(e -> e.getValue() == plugin
                    || (e.getKey() != null && e.getKey().equalsIgnoreCase(name)));

            try {
                Field commandsField = findField(manager.getClass(), "commandMap");
                commandsField.setAccessible(true);
                Object commandMap = commandsField.get(manager);
                Method getCommands = commandMap.getClass().getMethod("getKnownCommands");
                Map<String, Command> known = (Map<String, Command>) getCommands.invoke(commandMap);
                Iterator<Map.Entry<String, Command>> it = known.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Command> entry = it.next();
                    if (entry.getValue() instanceof PluginIdentifiableCommand) {
                        PluginIdentifiableCommand pic = (PluginIdentifiableCommand) entry.getValue();
                        if (pic.getPlugin() == plugin) {
                            entry.getValue().unregister((CommandMap) commandMap);
                            it.remove();
                        }
                    }
                }
            } catch (Throwable ignored) {
                // Command cleanup is best-effort.
            }

            ClassLoader classLoader = plugin.getClass().getClassLoader();
            if (classLoader instanceof URLClassLoader) {
                clearPluginClassLoader((URLClassLoader) classLoader);
                ((URLClassLoader) classLoader).close();
            }

            System.gc();
            return true;
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.WARNING, "Reflective unload failed for " + name, t);
            return false;
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static void clearPluginClassLoader(URLClassLoader classLoader) {
        for (String fieldName : new String[]{"plugin", "pluginInit"}) {
            try {
                Field field = findField(classLoader.getClass(), fieldName);
                field.setAccessible(true);
                field.set(classLoader, null);
            } catch (Throwable ignored) {
                // These fields vary between Bukkit implementations.
            }
        }
    }
}
