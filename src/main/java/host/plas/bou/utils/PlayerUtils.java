package host.plas.bou.utils;

import com.mojang.authlib.GameProfile;
import host.plas.bou.utils.obj.Versioning;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

/**
 * Utility class for player-related operations such as name updates,
 * profile patching, and player name/UUID lookups.
 */
public class PlayerUtils {
    /** Private constructor to prevent instantiation of this utility class. */
    private PlayerUtils() {}

    /**
     * Updates a player's display name, custom name, and optionally their tab list name.
     * Also patches the player's GameProfile with the new name (stripped of formatting).
     *
     * @param player    the player to update
     * @param name      the new name to set
     * @param format    whether to apply color formatting to the name
     * @param tabAsWell whether to also update the player list (tab) name
     */
    public static void updatePlayerName(Player player, String name, boolean format, boolean tabAsWell) {
        String toSet = format ? ColorUtils.colorizeHard(name) : name;
        String noColor = ColorUtils.stripFormatting(name);

        player.setDisplayName(toSet);
        player.setCustomName(noColor);
        player.setCustomNameVisible(true);

        patchProfile(player, noColor);

        if (tabAsWell) player.setPlayerListName(toSet);
    }

    /**
     * Retrieves the GameProfile associated with the given player via reflection.
     *
     * @param player the player to get the profile for
     * @return an Optional containing the GameProfile, or empty if retrieval fails
     */
    public static Optional<GameProfile> getProfile(Player player) {
        try {
            return Optional.ofNullable((GameProfile) VersionTool.getCraftPlayerGetGameProfileMethod().invoke(player));
        } catch (Throwable e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Patches the player's GameProfile name field and registers the new name via reflection.
     *
     * @param player  the player whose profile to patch
     * @param newName the new name to set in the profile (formatting will be stripped)
     */
    public static void patchProfile(Player player, String newName) {
        getProfile(player).ifPresent(profile -> {
            try {
                String cleansedName = ColorUtils.stripFormatting(newName);

                VersionTool.getGameProfileNameField().set(profile, cleansedName);
                registerName(player, cleansedName);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Registers a player's name in the server's internal player-by-name map via reflection.
     *
     * @param player the player to register
     * @param name   the name to register (formatting will be stripped)
     */
    public static void registerName(Player player, String name) {
        try {
            String cleansedName = ColorUtils.stripFormatting(name);

            final Object entityPlayer = VersionTool.getCraftPlayerGetHandleMethod().invoke(player);
            boolean is13R2Plus = Versioning.getServerVersion().isAfter(13, 2 - 1); // 1.13.2+

            String toPut = (is13R2Plus ? cleansedName.toLowerCase(Locale.ENGLISH) : cleansedName);

            VersionTool.getPlayersMap().put(toPut, entityPlayer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a set of all online player names.
     *
     * @return a sorted set of online player names
     */
    public static ConcurrentSkipListSet<String> getOnlinePlayerNames() {
        return EntityUtils.getOnlinePlayerNames();
    }

    /**
     * Returns a set of all online player UUIDs as strings.
     *
     * @return a sorted set of online player UUID strings
     */
    public static ConcurrentSkipListSet<String> getOnlinePlayerUUIDs() {
        return EntityUtils.getOnlinePlayerUuids();
    }

    /**
     * Returns a stream of all offline players known to the server.
     *
     * @return a stream of OfflinePlayer instances
     */
    public static Stream<OfflinePlayer> getOfflinePlayersStream() {
        return EntityUtils.getOfflinePlayersStream();
    }

    /**
     * Returns a set of all offline player names.
     *
     * @return a sorted set of offline player names
     */
    public static ConcurrentSkipListSet<String> getOfflinePlayerNames() {
        return EntityUtils.getOfflinePlayerNames();
    }

    /**
     * Returns a set of all offline player UUIDs as strings.
     *
     * @return a sorted set of offline player UUID strings
     */
    public static ConcurrentSkipListSet<String> getOfflinePlayerUUIDs() {
        return EntityUtils.getOfflinePlayerUuids();
    }
}
