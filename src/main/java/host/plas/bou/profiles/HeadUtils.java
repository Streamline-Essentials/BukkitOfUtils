package host.plas.bou.profiles;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Builds player heads from {@link ProfileData} without the server ever contacting Mojang.
 * <p>
 * A skull whose profile lacks textures (or a name, on 1.20.5+) is "incomplete", and the server
 * resolves it through Mojang's session server: one request per head, which fails for Bedrock and
 * offline-mode UUIDs and gets rate limited. These helpers always write a complete profile (UUID,
 * name, and textures), or leave the skull untouched when no skin is known.
 * <p>
 * Implementations, tried in order: Paper's profile API, Spigot's 1.18.1+ profile API, and the
 * legacy GameProfile field on older servers.
 */
public final class HeadUtils {
    private HeadUtils() {}

    /**
     * Creates a blank player head item for the running server version.
     *
     * @return a player head
     */
    @SuppressWarnings("deprecation")
    public static ItemStack createBlankHead() {
        Material modern = Material.matchMaterial("PLAYER_HEAD");
        if (modern != null) return new ItemStack(modern);
        Material legacy = Material.matchMaterial("SKULL_ITEM");
        // Data value 3 is the player variant of the pre-1.13 skull item.
        return new ItemStack(legacy != null ? legacy : Material.STONE, 1, (short) 3);
    }

    /**
     * Creates a player head showing the given profile's skin.
     *
     * @param profile the profile
     * @return the head; a plain head when the profile has no skin
     */
    public static ItemStack createHead(ProfileData profile) {
        ItemStack item = createBlankHead();
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof SkullMeta && applyProfile((SkullMeta) meta, profile)) {
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates a player head from a base64 {@code textures} value (as used by custom head databases).
     *
     * @param textureValue the base64 texture value
     * @return the head; a plain head when the value is blank
     */
    public static ItemStack createTexturedHead(String textureValue) {
        return createHead(textureProfile(textureValue));
    }

    /**
     * Looks up a player by UUID and creates their head. Completes on a lookup thread; switch to the
     * main (or region) thread before putting the item into an inventory.
     *
     * @param uuid the player's UUID
     * @return a future with the head; a plain head when no skin could be found
     */
    public static CompletableFuture<ItemStack> createHead(UUID uuid) {
        return PlayerLookup.lookup(uuid).thenApply(profile -> profile.map(HeadUtils::createHead).orElseGet(HeadUtils::createBlankHead));
    }

    /**
     * Looks up a player by name and creates their head. Completes on a lookup thread; switch to the
     * main (or region) thread before putting the item into an inventory.
     *
     * @param name the player's name (Bedrock names with the Floodgate prefix are supported)
     * @return a future with the head; a plain head when no skin could be found
     */
    public static CompletableFuture<ItemStack> createHeadByName(String name) {
        return PlayerLookup.lookup(name).thenApply(profile -> profile.map(HeadUtils::createHead).orElseGet(HeadUtils::createBlankHead));
    }

    /**
     * Applies a base64 {@code textures} value to a skull.
     *
     * @param meta         the skull meta to modify
     * @param textureValue the base64 texture value
     * @return true if the texture was applied
     */
    public static boolean applyTexture(SkullMeta meta, String textureValue) {
        return applyProfile(meta, textureProfile(textureValue));
    }

    private static ProfileData textureProfile(String textureValue) {
        // A stable UUID per texture keeps identical custom heads stackable.
        UUID uuid = textureValue == null ? new UUID(0L, 0L) : UUID.nameUUIDFromBytes(textureValue.getBytes(StandardCharsets.UTF_8));
        return new ProfileData(uuid, null, textureValue, null, LookupSource.DERIVED);
    }

    /**
     * Writes a complete profile (UUID, name, and textures) onto a skull. Skulls are left unchanged
     * when the profile has no skin, because a skin-less profile is exactly what makes the server
     * query Mojang.
     *
     * @param meta    the skull meta to modify
     * @param profile the profile to apply
     * @return true if the profile was applied
     */
    public static boolean applyProfile(SkullMeta meta, ProfileData profile) {
        if (meta == null || profile == null || !profile.hasTextures()) return false;

        UUID uuid = profile.getUuid() != null ? profile.getUuid() : UUID.nameUUIDFromBytes(profile.getTextureValue().getBytes(StandardCharsets.UTF_8));
        String name = profileName(profile, uuid);

        return applyPaper(meta, uuid, name, profile)
                || applySpigot(meta, uuid, name, profile)
                || applyLegacy(meta, uuid, name, profile);
    }

    /**
     * A name valid on every version: at most 16 printable ASCII characters. Without one, 1.20.5+
     * treats the profile as unresolved and looks it up anyway.
     */
    private static String profileName(ProfileData profile, UUID uuid) {
        String name = profile.getName();
        if (name != null && name.length() <= 16 && name.chars().allMatch(c -> c > 32 && c < 127)) return name;
        if (name != null) {
            String cleaned = name.replaceAll("[^\\x21-\\x7E]", "");
            if (!cleaned.isEmpty()) return cleaned.length() > 16 ? cleaned.substring(0, 16) : cleaned;
        }
        return uuid.toString().replace("-", "").substring(0, 16);
    }

    private static boolean applyPaper(SkullMeta meta, UUID uuid, String name, ProfileData profile) {
        try {
            Class<?> profileClass = Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
            Class<?> propertyClass = Class.forName("com.destroystokyo.paper.profile.ProfileProperty");

            Object paperProfile = Bukkit.class.getMethod("createProfile", UUID.class, String.class).invoke(null, uuid, name);
            Constructor<?> propertyConstructor = propertyClass.getConstructor(String.class, String.class, String.class);
            Object property = propertyConstructor.newInstance("textures", profile.getTextureValue(), profile.getTextureSignature());
            profileClass.getMethod("setProperty", propertyClass).invoke(paperProfile, property);

            meta.getClass().getMethod("setPlayerProfile", profileClass).invoke(meta, paperProfile);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean applySpigot(SkullMeta meta, UUID uuid, String name, ProfileData profile) {
        if (profile.getSkinUrl() == null) return false;
        try {
            Class<?> profileClass = Class.forName("org.bukkit.profile.PlayerProfile");
            Class<?> texturesClass = Class.forName("org.bukkit.profile.PlayerTextures");
            Class<?> modelClass = Class.forName("org.bukkit.profile.PlayerTextures$SkinModel");

            Object spigotProfile = Bukkit.class.getMethod("createPlayerProfile", UUID.class, String.class).invoke(null, uuid, name);
            Object textures = profileClass.getMethod("getTextures").invoke(spigotProfile);

            Object model = modelClass.getField(profile.isSlim() ? "SLIM" : "CLASSIC").get(null);
            texturesClass.getMethod("setSkin", URL.class, modelClass).invoke(textures, new URL(profile.getSkinUrl()), model);
            profileClass.getMethod("setTextures", texturesClass).invoke(spigotProfile, textures);

            SkullMeta.class.getMethod("setOwnerProfile", profileClass).invoke(meta, spigotProfile);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean applyLegacy(SkullMeta meta, UUID uuid, String name, ProfileData profile) {
        try {
            GameProfile gameProfile = new GameProfile(uuid, name);
            gameProfile.getProperties().put("textures", new Property("textures", profile.getTextureValue(), profile.getTextureSignature()));

            Optional<Method> setter = findMethod(meta.getClass(), "setProfile", GameProfile.class);
            if (setter.isPresent()) {
                setter.get().invoke(meta, gameProfile);
                return true;
            }

            Field field = findField(meta.getClass(), "profile");
            if (field == null) return false;
            field.set(meta, gameProfile);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static Optional<Method> findMethod(Class<?> clazz, String name, Class<?>... types) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                Method method = c.getDeclaredMethod(name, types);
                method.setAccessible(true);
                return Optional.of(method);
            } catch (NoSuchMethodException ignored) {
                // Check the superclass.
            }
        }
        return Optional.empty();
    }

    private static Field findField(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // Check the superclass.
            }
        }
        return null;
    }
}
