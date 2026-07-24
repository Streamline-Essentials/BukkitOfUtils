package host.plas.bou.libs;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Fallback library loader for servers that do not support Spigot's native
 * {@code plugin.yml} {@code libraries:} downloader (or when a declared library
 * failed to load). Modern Spigot/Paper download Maven Central libraries before
 * the plugin constructs; this class only downloads what is still missing.
 *
 * <p>Works from legacy 1.8-classloaders through modern Java 17+ plugin loaders.</p>
 */
public final class RuntimeLibraryLoader {
    public static final String MAVEN_CENTRAL = "https://repo1.maven.org/maven2/";

    private static final List<Library> LIBRARIES;

    static {
        List<Library> libs = new ArrayList<>();
        // Keep in sync with plugin.yml `libraries:` (plus required transitives).
        libs.add(Library.mavenCentral("com.zaxxer", "HikariCP", "5.1.0", "com.zaxxer.hikari.HikariDataSource"));
        libs.add(Library.mavenCentral("org.slf4j", "slf4j-api", "2.0.13", "org.slf4j.Logger"));
        libs.add(Library.mavenCentral("org.xerial", "sqlite-jdbc", "3.46.1.0", "org.sqlite.JDBC"));
        libs.add(Library.mavenCentral("com.mysql", "mysql-connector-j", "8.0.33", "com.mysql.cj.jdbc.Driver"));
        libs.add(Library.mavenCentral("com.google.code.gson", "gson", "2.11.0", "com.google.gson.Gson"));
        libs.add(Library.mavenCentral("com.github.ben-manes.caffeine", "caffeine", "3.1.8", "com.github.benmanes.caffeine.cache.Cache"));
        libs.add(Library.mavenCentral("com.konghq", "unirest-java", "3.14.5", "kong.unirest.Unirest"));
        libs.add(Library.mavenCentral("org.apache.httpcomponents", "httpclient", "4.5.13", "org.apache.http.client.HttpClient"));
        libs.add(Library.mavenCentral("org.apache.httpcomponents", "httpcore", "4.4.16", "org.apache.http.HttpHost"));
        libs.add(Library.mavenCentral("org.apache.httpcomponents", "httpmime", "4.5.13", "org.apache.http.entity.mime.MultipartEntityBuilder"));
        libs.add(Library.mavenCentral("org.apache.httpcomponents", "httpasyncclient", "4.1.5", "org.apache.http.nio.client.HttpAsyncClient"));
        libs.add(Library.mavenCentral("org.apache.httpcomponents", "httpcore-nio", "4.4.16", "org.apache.http.nio.reactor.IOSession"));
        libs.add(Library.mavenCentral("commons-codec", "commons-codec", "1.15", "org.apache.commons.codec.binary.Base64"));
        libs.add(Library.mavenCentral("commons-logging", "commons-logging", "1.2", "org.apache.commons.logging.Log"));
        LIBRARIES = Collections.unmodifiableList(libs);
    }

    private RuntimeLibraryLoader() {
    }

    /**
     * Ensures all runtime libraries are present on the plugin classloader.
     * No-ops for artifacts already provided by Spigot's native library loader.
     *
     * @param plugin owning plugin (used for logger + libraries folder)
     */
    public static void ensureLoaded(JavaPlugin plugin) {
        if (plugin == null) return;

        Path libDir = plugin.getDataFolder().toPath().resolve("libraries");
        try {
            Files.createDirectories(libDir);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create libraries directory: " + libDir, e);
            return;
        }

        ClassLoader classLoader = plugin.getClass().getClassLoader();
        int downloaded = 0;
        int skipped = 0;

        for (Library library : LIBRARIES) {
            if (isPresent(library.testClass, classLoader)) {
                skipped++;
                continue;
            }

            try {
                Path jar = resolveJar(libDir, library);
                inject(classLoader, jar);
                if (!isPresent(library.testClass, classLoader)) {
                    plugin.getLogger().severe("Loaded " + library.fileName()
                            + " but could not find test class " + library.testClass);
                } else {
                    downloaded++;
                    plugin.getLogger().info("Loaded runtime library: " + library.coords());
                }
            } catch (Throwable t) {
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to load runtime library " + library.coords() + ": " + t.getMessage(), t);
            }
        }

        if (downloaded > 0) {
            plugin.getLogger().info("Runtime library fallback loaded " + downloaded
                    + " jar(s) (" + skipped + " already present via Spigot libraries).");
        }
    }

    private static Path resolveJar(Path libDir, Library library) throws Exception {
        Path target = libDir.resolve(library.fileName());
        if (Files.exists(target) && Files.size(target) > 0L) {
            return target;
        }

        URL url = new URL(library.repoUrl + library.mavenPath());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(15_000);
        connection.setReadTimeout(60_000);
        connection.setRequestProperty("User-Agent", "BukkitOfUtils-RuntimeLibraryLoader");

        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("HTTP " + code + " for " + url);
        }

        Path temp = Files.createTempFile(libDir, library.artifact + "-", ".jar.part");
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            connection.disconnect();
        }

        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    private static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void inject(ClassLoader classLoader, Path jar) throws Exception {
        URL url = jar.toUri().toURL();

        // Spigot/Paper PluginClassLoader usually extends URLClassLoader.
        if (classLoader instanceof URLClassLoader) {
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            try {
                addURL.setAccessible(true);
                addURL.invoke(classLoader, url);
                return;
            } catch (Exception reflectionFailure) {
                // Java 16+ may block setAccessible — try lookup / declared helper below.
            }
        }

        // Paper / newer loaders sometimes expose addURL on the concrete class.
        try {
            Method addURL = classLoader.getClass().getMethod("addURL", URL.class);
            addURL.setAccessible(true);
            addURL.invoke(classLoader, url);
            return;
        } catch (NoSuchMethodException ignored) {
            // continue
        }

        try {
            Method addURL = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            addURL.invoke(classLoader, url);
            return;
        } catch (NoSuchMethodException ignored) {
            // continue
        }

        throw new UnsupportedOperationException(
                "Cannot inject libraries into classloader type: " + classLoader.getClass().getName());
    }

    /**
     * Maven library descriptor.
     */
    public static final class Library {
        private final String group;
        private final String artifact;
        private final String version;
        private final String repoUrl;
        private final String testClass;

        private Library(String group, String artifact, String version, String repoUrl, String testClass) {
            this.group = group;
            this.artifact = artifact;
            this.version = version;
            this.repoUrl = repoUrl.endsWith("/") ? repoUrl : repoUrl + "/";
            this.testClass = testClass;
        }

        public static Library mavenCentral(String group, String artifact, String version, String testClass) {
            return new Library(group, artifact, version, MAVEN_CENTRAL, testClass);
        }

        public String coords() {
            return group + ":" + artifact + ":" + version;
        }

        public String fileName() {
            return artifact + "-" + version + ".jar";
        }

        public String mavenPath() {
            return group.replace('.', '/') + "/" + artifact + "/" + version + "/" + fileName();
        }
    }
}
