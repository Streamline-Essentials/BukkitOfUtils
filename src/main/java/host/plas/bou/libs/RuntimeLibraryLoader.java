package host.plas.bou.libs;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
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

    private static final String PART_SUFFIX = ".jar.part";

    private static final List<Library> LIBRARIES;

    static {
        List<Library> libs = new ArrayList<>();
        // Keep in sync with plugin.yml `libraries:` (plus required transitives).
        libs.add(Library.mavenCentral("com.zaxxer", "HikariCP", "5.1.0", "com.zaxxer.hikari.HikariDataSource",
                "a47a6ee62379694ee52c30036f0931b72f9aee2a801d590341ed82bd839e2134"));
        libs.add(Library.mavenCentral("org.slf4j", "slf4j-api", "2.0.13", "org.slf4j.Logger",
                "e7c2a48e8515ba1f49fa637d57b4e2f590b3f5bd97407ac699c3aa5efb1204a9"));
        libs.add(Library.mavenCentral("org.xerial", "sqlite-jdbc", "3.46.1.0", "org.sqlite.JDBC",
                "6dc7464e3803648d3ff18a7359bab6adf079fcd8495b18991f6f5edcb8ac6e3b"));
        libs.add(Library.mavenCentral("com.mysql", "mysql-connector-j", "8.0.33", "com.mysql.cj.jdbc.Driver",
                "e2a3b2fc726a1ac64e998585db86b30fa8bf3f706195b78bb77c5f99bf877bd9"));
        libs.add(Library.mavenCentral("com.google.code.gson", "gson", "2.11.0", "com.google.gson.Gson",
                "57928d6e5a6edeb2abd3770a8f95ba44dce45f3b23b7a9dc2b309c581552a78b"));
        libs.add(Library.mavenCentral("com.github.ben-manes.caffeine", "caffeine", "3.1.8", "com.github.benmanes.caffeine.cache.Cache",
                "7dd15f9df1be238ffaa367ce6f556737a88031de4294dad18eef57c474ddf1d3"));
        libs.add(Library.mavenCentral("com.konghq", "unirest-java", "3.14.5", "kong.unirest.Unirest",
                "f0cc339221e9e759681bd1435f5918f460beadec4745c27a8719af6da26d45e5"));
        libs.add(Library.mavenCentral("org.apache.httpcomponents", "httpclient", "4.5.13", "org.apache.http.client.HttpClient",
                "6fe9026a566c6a5001608cf3fc32196641f6c1e5e1986d1037ccdbd5f31ef743"));
        libs.add(Library.mavenCentral("org.apache.httpcomponents", "httpcore", "4.4.16", "org.apache.http.HttpHost",
                "6c9b3dd142a09dc468e23ad39aad6f75a0f2b85125104469f026e52a474e464f"));
        libs.add(Library.mavenCentral("org.apache.httpcomponents", "httpmime", "4.5.13", "org.apache.http.entity.mime.MultipartEntityBuilder",
                "06e754d99245b98dcc2860dcb43d20e737d650da2bf2077a105f68accbd5c5cc"));
        libs.add(Library.mavenCentral("org.apache.httpcomponents", "httpasyncclient", "4.1.5", "org.apache.http.nio.client.HttpAsyncClient",
                "0c1877489a9d1ba4fa50f6cfcab11d1123618858cb31d56afaab5afdd5064d99"));
        libs.add(Library.mavenCentral("org.apache.httpcomponents", "httpcore-nio", "4.4.16", "org.apache.http.nio.reactor.IOSession",
                "4018736ede2d321034e8517ea90baefb31831a8608afccc446d8a699fb1d00d4"));
        libs.add(Library.mavenCentral("commons-codec", "commons-codec", "1.15", "org.apache.commons.codec.binary.Base64",
                "b3e9f6d63a790109bf0d056611fbed1cf69055826defeb9894a71369d246ed63"));
        libs.add(Library.mavenCentral("commons-logging", "commons-logging", "1.2", "org.apache.commons.logging.Log",
                "daddea1ea0be0f56978ab3006b8ac92834afeefbd9b7e4e6316fca57df0fa636"));
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

        deleteStalePartFiles(libDir, plugin);

        ClassLoader classLoader = plugin.getClass().getClassLoader();
        int downloaded = 0;
        int skipped = 0;
        List<String> unreachable = new ArrayList<>();
        boolean offline = false;

        for (Library library : LIBRARIES) {
            if (isPresent(library.testClass, classLoader)) {
                skipped++;
                continue;
            }

            try {
                Path jar = findVerifiedJar(libDir, library, plugin);
                if (jar == null) {
                    // Once the repository is unreachable, every further download would wait out
                    // the same timeouts, so the rest are skipped and reported together.
                    if (offline) {
                        unreachable.add(library.coords());
                        continue;
                    }
                    try {
                        jar = download(libDir, library);
                    } catch (UnknownHostException | ConnectException | SocketTimeoutException | NoRouteToHostException e) {
                        offline = true;
                        unreachable.add(library.coords());
                        plugin.getLogger().severe("Could not reach " + library.repoUrl + " (" + e + ").");
                        continue;
                    }
                }
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

        if (! unreachable.isEmpty()) {
            plugin.getLogger().severe("Runtime libraries not loaded because the repository is unreachable: "
                    + String.join(", ", unreachable) + ". Place these jars in " + libDir
                    + " or allow the server to reach Maven Central.");
        }

        if (downloaded > 0) {
            plugin.getLogger().info("Runtime library fallback loaded " + downloaded
                    + " jar(s) (" + skipped + " already present via Spigot libraries).");
        }
    }

    /**
     * Returns the library jar already in the folder when its SHA-256 matches the pinned hash.
     * A jar that does not match is deleted so it is downloaded again rather than loaded.
     */
    private static Path findVerifiedJar(Path libDir, Library library, JavaPlugin plugin) throws Exception {
        Path target = libDir.resolve(library.fileName());
        if (! Files.exists(target)) return null;

        if (library.sha256.equalsIgnoreCase(sha256(target))) return target;

        plugin.getLogger().warning("Checksum mismatch for " + target + "; deleting it and downloading it again.");
        Files.deleteIfExists(target);
        return null;
    }

    /**
     * Downloads the library to a temporary file, checks its SHA-256 against the pinned hash and
     * only then moves it into place. The temporary file is always removed.
     */
    private static Path download(Path libDir, Library library) throws Exception {
        Path target = libDir.resolve(library.fileName());

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

        Path temp = Files.createTempFile(libDir, library.artifact + "-", PART_SUFFIX);
        try {
            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                connection.disconnect();
            }

            String actual = sha256(temp);
            if (! library.sha256.equalsIgnoreCase(actual)) {
                throw new SecurityException("Checksum mismatch for " + url + ": expected " + library.sha256 + ", got " + actual);
            }

            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static void deleteStalePartFiles(Path libDir, JavaPlugin plugin) {
        try (DirectoryStream<Path> parts = Files.newDirectoryStream(libDir, "*" + PART_SUFFIX)) {
            for (Path part : parts) {
                Files.deleteIfExists(part);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to clean up partial downloads in " + libDir, e);
        }
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        StringBuilder hex = new StringBuilder();
        for (byte value : digest.digest()) {
            hex.append(String.format("%02x", value));
        }
        return hex.toString();
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
        private final String sha256;

        private Library(String group, String artifact, String version, String repoUrl, String testClass, String sha256) {
            this.group = group;
            this.artifact = artifact;
            this.version = version;
            this.repoUrl = repoUrl.endsWith("/") ? repoUrl : repoUrl + "/";
            this.testClass = testClass;
            this.sha256 = sha256;
        }

        /**
         * @param group     Maven group id
         * @param artifact  Maven artifact id
         * @param version   exact version
         * @param testClass a class the jar provides, used to detect whether it is already loaded
         * @param sha256    SHA-256 of the published jar; a jar with any other hash is never loaded
         * @return the library descriptor
         */
        public static Library mavenCentral(String group, String artifact, String version, String testClass, String sha256) {
            return new Library(group, artifact, version, MAVEN_CENTRAL, testClass, sha256);
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
