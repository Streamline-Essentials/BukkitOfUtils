package host.plas.bou.utils;

import com.mojang.authlib.GameProfile;
import host.plas.bou.utils.obj.Versioning;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;

public class PlayerUtils {
    public static void updatePlayerName(Player player, String name, boolean format, boolean tabAsWell) {
        String toSet = format ? ColorUtils.colorizeHard(name) : name;
        String noColor = ColorUtils.stripFormatting(name);

        player.setDisplayName(toSet);
        player.setCustomName(noColor);
        player.setCustomNameVisible(true);

        patchProfile(player, noColor);

        if (tabAsWell) player.setPlayerListName(toSet);
    }

    public static Optional<GameProfile> getProfile(Player player) {
        try {
            return Optional.ofNullable((GameProfile) VersionTool.getCraftPlayerGetGameProfileMethod().invoke(player));
        } catch (Throwable e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

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
}
