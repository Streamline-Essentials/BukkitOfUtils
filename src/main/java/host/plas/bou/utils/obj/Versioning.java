package host.plas.bou.utils.obj;

import host.plas.bou.utils.VersionTool;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
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

    public boolean isModern() {
        return first == -2 && second == 0 && third == 0;
    }

    public static Versioning fromString(String version) {
        String[] parts = version.split("\\.");
        long first = parts.length > 0 ? Long.parseLong(parts[0]) : 0;
        long second = parts.length > 1 ? Long.parseLong(parts[1]) : 0;
        long third = parts.length > 2 ? Long.parseLong(parts[2]) : 0;
        return new Versioning(first, second, third);
    }

    public static Versioning getEmpty() {
        return new Versioning(0, 0, 0);
    }

    public static Versioning getModern() {
        return new Versioning(-2, 0, 0);
    }

    public static Versioning introspect() {
        SERVER_VERSION = VersionTool.getVersion();

        return SERVER_VERSION;
    }

    public static Versioning getServerVersion() {
        if (SERVER_VERSION == null) {
            return introspect();
        }

        return SERVER_VERSION;
    }
}
