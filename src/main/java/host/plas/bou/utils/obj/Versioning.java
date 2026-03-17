package host.plas.bou.utils.obj;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.utils.VersionTool;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a three-part version number (major.minor.patch) with comparison utilities.
 * Also provides static methods for server version introspection.
 */
@Getter @Setter
public class Versioning implements Comparable<Versioning> {
    /** Cached server version, populated by {@link #introspect()}. */
    private static Versioning SERVER_VERSION = null;

    /**
     * The major version number.
     *
     * @param first the major version number to set
     * @return the major version number
     */
    private long first;
    /**
     * The minor version number.
     *
     * @param second the minor version number to set
     * @return the minor version number
     */
    private long second;
    /**
     * The patch version number.
     *
     * @param third the patch version number to set
     * @return the patch version number
     */
    private long third;

    /**
     * Constructs a Versioning with the specified major, minor, and patch components.
     *
     * @param first  the major version number
     * @param second the minor version number
     * @param third  the patch version number
     */
    public Versioning(long first, long second, long third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public String toString() {
        return first + "." + second + "." + third;
    }

    /**
     * Checks whether this version's minor component is before the given minor version.
     *
     * @param minorVersion the minor version to compare against
     * @return true if this version's minor component is less than the given value
     */
    public boolean isBefore(long minorVersion) {
        return this.second < minorVersion;
    }

    /**
     * Checks whether this version's minor component is after the given minor version.
     *
     * @param minorVersion the minor version to compare against
     * @return true if this version's minor component is greater than the given value
     */
    public boolean isAfter(long minorVersion) {
        return this.second > minorVersion;
    }

    /**
     * Checks whether this version's minor component equals the given minor version.
     *
     * @param minorVersion the minor version to compare against
     * @return true if this version's minor component equals the given value
     */
    public boolean isSame(long minorVersion) {
        return this.second == minorVersion;
    }

    /**
     * Checks whether this version is before the given minor and patch version.
     *
     * @param minorVersion the minor version to compare against
     * @param patchVersion the patch version to compare against
     * @return true if this version is before the given minor.patch
     */
    public boolean isBefore(long minorVersion, long patchVersion) {
        if (this.second < minorVersion) return true;
        if (this.second > minorVersion) return false;
        return this.third < patchVersion;
    }

    /**
     * Checks whether this version is after the given minor and patch version.
     *
     * @param minorVersion the minor version to compare against
     * @param patchVersion the patch version to compare against
     * @return true if this version is after the given minor.patch
     */
    public boolean isAfter(long minorVersion, long patchVersion) {
        if (this.second > minorVersion) return true;
        if (this.second < minorVersion) return false;
        return this.third > patchVersion;
    }

    /**
     * Checks whether this version equals the given minor and patch version.
     *
     * @param minorVersion the minor version to compare against
     * @param patchVersion the patch version to compare against
     * @return true if this version's minor and patch match the given values
     */
    public boolean isSame(long minorVersion, long patchVersion) {
        return this.second == minorVersion && this.third == patchVersion;
    }

    /**
     * Checks whether this version is before another Versioning instance.
     *
     * @param other the version to compare against
     * @return true if this version is before the other version
     */
    public boolean isBefore(Versioning other) {
        if (this.first < other.first) return true;
        if (this.first > other.first) return false;
        if (this.second < other.second) return true;
        if (this.second > other.second) return false;
        return this.third < other.third;
    }

    /**
     * Checks whether this version is after another Versioning instance.
     *
     * @param other the version to compare against
     * @return true if this version is after the other version
     */
    public boolean isAfter(Versioning other) {
        if (this.first > other.first) return true;
        if (this.first < other.first) return false;
        if (this.second > other.second) return true;
        if (this.second < other.second) return false;
        return this.third > other.third;
    }

    /**
     * Checks whether this version is the same as another Versioning instance.
     *
     * @param other the version to compare against
     * @return true if all three components match
     */
    public boolean isSame(Versioning other) {
        return this.first == other.first && this.second == other.second && this.third == other.third;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Versioning)) return false;
        Versioning other = (Versioning) obj;

        return isSame(other);
    }

    @Override
    public int compareTo(@NotNull Versioning o) {
        if (isBefore(o)) return -1;
        if (isAfter(o)) return 1;
        return 0;
    }

    /**
     * Checks whether this version represents an empty (0.0.0) version.
     *
     * @return true if all components are zero
     */
    public boolean isEmpty() {
        return first == 0 && second == 0 && third == 0;
    }

    /**
     * Checks whether this version represents a modern server that errored during version detection.
     *
     * @return true if the first component is -2 and the rest are zero
     */
    public boolean isModernErrored() {
        return first == -2 && second == 0 && third == 0;
    }

    /**
     * Checks whether this version represents a modern server (after 1.20.4 or a modern error).
     *
     * @return true if this version is considered modern
     */
    public boolean isModern() {
        if (isModernErrored()) return true;

        return isAfter(new Versioning(1, 20, 4));
    }

    /**
     * Parses a version string (e.g., "1.20.4" or "1.20.4-SNAPSHOT") into a Versioning instance.
     *
     * @param version the version string to parse
     * @return the parsed Versioning, or an empty version if parsing fails
     */
    public static Versioning fromString(String version) {
        try {
            if (version.contains("-")) {
                version = version.split("-")[0];
            }

            String[] parts = version.split("\\.");
            long first = parts.length > 0 ? Long.parseLong(grabOnlyNumbers(parts[0])) : 0;
            long second = parts.length > 1 ? Long.parseLong(grabOnlyNumbers(parts[1])) : 0;
            long third = parts.length > 2 ? Long.parseLong(grabOnlyNumbers(parts[2])) : 0;
            return new Versioning(first, second, third);
        } catch (Exception e) {
            BukkitOfUtils.getInstance().logWarningWithInfo("Failed to parse version string: " + version, e);
            return getEmpty();
        }
    }

    /**
     * Strips all non-numeric characters (except dots) from a string.
     *
     * @param from the string to process
     * @return a string containing only digits and dots
     */
    public static String grabOnlyNumbers(String from) {
        return from.replaceAll("[^0-9.]", "");
    }

    /**
     * Returns an empty (0.0.0) version.
     *
     * @return a Versioning representing version 0.0.0
     */
    public static Versioning getEmpty() {
        return new Versioning(0, 0, 0);
    }

    /**
     * Returns a special version representing a modern server with unknown exact version.
     *
     * @return a Versioning with first=-2, representing a modern server
     */
    public static Versioning getModern() {
        return new Versioning(-2, 0, 0);
    }

    /**
     * Introspects the current Bukkit server version and caches the result.
     *
     * @return the detected server Versioning
     */
    public static Versioning introspect() {
        SERVER_VERSION = parseFromBukkit();

        return SERVER_VERSION;
    }

    /**
     * Returns the cached server version, introspecting if not yet cached.
     *
     * @return the server Versioning
     */
    public static Versioning getServerVersion() {
        if (SERVER_VERSION == null) {
            return introspect();
        }

        return SERVER_VERSION;
    }

    /**
     * Returns the raw Bukkit version string (e.g., "1.20.4-R0.1-SNAPSHOT").
     *
     * @return the Bukkit version string
     */
    public static String getBukkitVersion() {
        return Bukkit.getBukkitVersion();
    }

    /**
     * Parses the Bukkit version string into a Versioning instance.
     *
     * @return the parsed Versioning from Bukkit
     */
    public static Versioning parseFromBukkit() {
        return fromString(getBukkitVersion());
    }
}
