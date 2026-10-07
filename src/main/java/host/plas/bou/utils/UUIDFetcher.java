package host.plas.bou.utils;

import host.plas.bou.profiles.LookupResult;
import host.plas.bou.profiles.PlayerLookup;
import host.plas.bou.profiles.ProfileData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Blocking player name/UUID lookups, backed by {@link PlayerLookup}.
 * <p>
 * Results come from the server's own data first, then the Geyser global API (Bedrock players),
 * PlayerDB, and MineTools; Mojang is never queried. Calls are cached and bounded by
 * {@link #TIMEOUT_MILLIS}, so offline-mode and no-internet servers get a fast answer. Prefer the
 * asynchronous {@link PlayerLookup} methods in new code.
 */
public class UUIDFetcher {
    /** Private constructor to prevent instantiation of this utility class. */
    private UUIDFetcher() {}

    /** The longest a blocking lookup waits before returning null. */
    public static final long TIMEOUT_MILLIS = 10_000L;

    /**
     * Fetches the UUID of a player by name. On offline-mode servers (not behind a proxy) this is
     * the offline-mode UUID the server assigns to that name.
     *
     * @param name the player's username
     * @return the player's UUID, or null if the player is not found or no source could be reached
     */
    @Nullable
    public static UUID getUUID(@NotNull String name) {
        // Identity answers never wait on the network when the server already knows (or can
        // derive) them; only skins need the web.
        Optional<UUID> known = PlayerLookup.getUuidWithoutLookup(name);
        if (known.isPresent()) return known.get();

        LookupResult result = PlayerLookup.resolveBlocking(name, TIMEOUT_MILLIS);
        return result.getProfile().map(ProfileData::getUuid).orElse(null);
    }

    /**
     * Fetches the username of a player by their UUID string.
     *
     * @param uuid the player's UUID as a string
     * @return the player's username, or null if the player is not found or an error occurs
     */
    @Nullable
    public static String getName(@NotNull String uuid) {
        try {
            return getName(UUID.fromString(uuid.trim()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Fetches the username of a player by their UUID.
     *
     * @param uuid the player's UUID
     * @return the player's username, or null if the player is not found or an error occurs
     */
    public static String getName(UUID uuid) {
        Optional<String> known = PlayerLookup.getNameWithoutLookup(uuid);
        if (known.isPresent()) return known.get();

        LookupResult result = PlayerLookup.resolveBlocking(uuid, TIMEOUT_MILLIS);
        return result.getProfile().map(ProfileData::getName).orElse(null);
    }
}
