package host.plas.bou.profiles;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Classifies a player UUID by where it came from, using only the UUID's bits.
 * <p>
 * Works without Floodgate installed on this server, which matters when Floodgate
 * only runs on the proxy: the backend still sees Floodgate-shaped UUIDs.
 */
public enum UuidType {
    /** A Mojang/Microsoft account UUID (version 4). Resolvable through Java profile APIs. */
    ONLINE,
    /** An offline-mode UUID derived from {@code "OfflinePlayer:" + name} (version 3). No Java profile exists. */
    OFFLINE,
    /** A Floodgate UUID: zero high bits, with the Bedrock XUID in the low bits. No Java profile exists. */
    BEDROCK,
    /** Anything else (null UUID, NPC UUIDs of other versions, etc.). */
    OTHER,
    ;

    /**
     * Classifies the given UUID.
     *
     * @param uuid the UUID to classify, may be null
     * @return the UUID's type; {@link #OTHER} for null or the all-zero UUID
     */
    public static UuidType of(UUID uuid) {
        if (uuid == null) return OTHER;
        if (uuid.getMostSignificantBits() == 0L) {
            return uuid.getLeastSignificantBits() == 0L ? OTHER : BEDROCK;
        }

        switch (uuid.version()) {
            case 4:
                return ONLINE;
            case 3:
                return OFFLINE;
            default:
                return OTHER;
        }
    }

    /**
     * Whether Java profile services (Mojang, PlayerDB, MineTools) can know this UUID.
     * Sending any other kind of UUID to them only produces errors and rate limits.
     *
     * @return true only for {@link #ONLINE}
     */
    public boolean isJavaResolvable() {
        return this == ONLINE;
    }

    /**
     * Returns the Bedrock XUID encoded in a Floodgate UUID.
     *
     * @param uuid a UUID of type {@link #BEDROCK}
     * @return the XUID
     */
    public static long toXuid(UUID uuid) {
        return uuid.getLeastSignificantBits();
    }

    /**
     * Builds the Floodgate UUID for a Bedrock XUID.
     *
     * @param xuid the Bedrock XUID
     * @return the Floodgate UUID
     */
    public static UUID fromXuid(long xuid) {
        return new UUID(0L, xuid);
    }

    /**
     * Builds the offline-mode UUID the server assigns to a name when it is not authenticating.
     *
     * @param name the player name
     * @return the offline-mode UUID
     */
    public static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }
}
