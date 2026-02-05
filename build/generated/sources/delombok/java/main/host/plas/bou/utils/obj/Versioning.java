package host.plas.bou.utils.obj;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.utils.VersionTool;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class Versioning implements Comparable<Versioning> {
    private static Versioning SERVER_VERSION = null;
    private long first;
    private long second;
    private long third;

    public Versioning(long first, long second, long third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public String toString() {
        return first + "." + second + "." + third;
    }

    public boolean isBefore(long minorVersion) {
        return this.second < minorVersion;
    }

    public boolean isAfter(long minorVersion) {
        return this.second > minorVersion;
    }

    public boolean isSame(long minorVersion) {
        return this.second == minorVersion;
    }

    public boolean isBefore(long minorVersion, long patchVersion) {
        if (this.second < minorVersion) return true;
        if (this.second > minorVersion) return false;
        return this.third < patchVersion;
    }

    public boolean isAfter(long minorVersion, long patchVersion) {
        if (this.second > minorVersion) return true;
        if (this.second < minorVersion) return false;
        return this.third > patchVersion;
    }

    public boolean isSame(long minorVersion, long patchVersion) {
        return this.second == minorVersion && this.third == patchVersion;
    }

    public boolean isBefore(Versioning other) {
        if (this.first < other.first) return true;
        if (this.first > other.first) return false;
        if (this.second < other.second) return true;
        if (this.second > other.second) return false;
        return this.third < other.third;
    }

    public boolean isAfter(Versioning other) {
        if (this.first > other.first) return true;
        if (this.first < other.first) return false;
        if (this.second > other.second) return true;
        if (this.second < other.second) return false;
        return this.third > other.third;
    }

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

    public boolean isEmpty() {
        return first == 0 && second == 0 && third == 0;
    }

    public boolean isModernErrored() {
        return first == -2 && second == 0 && third == 0;
    }

    public boolean isModern() {
        if (isModernErrored()) return true;
        return isAfter(new Versioning(1, 20, 4));
    }

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

    public static String grabOnlyNumbers(String from) {
        return from.replaceAll("[^0-9.]", "");
    }

    public static Versioning getEmpty() {
        return new Versioning(0, 0, 0);
    }

    public static Versioning getModern() {
        return new Versioning(-2, 0, 0);
    }

    public static Versioning introspect() {
        SERVER_VERSION = parseFromBukkit();
        return SERVER_VERSION;
    }

    public static Versioning getServerVersion() {
        if (SERVER_VERSION == null) {
            return introspect();
        }
        return SERVER_VERSION;
    }

    public static String getBukkitVersion() {
        return Bukkit.getBukkitVersion();
    }

    public static Versioning parseFromBukkit() {
        return fromString(getBukkitVersion());
    }

    public long getFirst() {
        return this.first;
    }

    public long getSecond() {
        return this.second;
    }

    public long getThird() {
        return this.third;
    }

    public void setFirst(final long first) {
        this.first = first;
    }

    public void setSecond(final long second) {
        this.second = second;
    }

    public void setThird(final long third) {
        this.third = third;
    }
}
