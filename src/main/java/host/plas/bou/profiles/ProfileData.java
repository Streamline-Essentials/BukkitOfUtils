package host.plas.bou.profiles;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * An immutable snapshot of what is known about a player: identity and (optionally) skin.
 * <p>
 * The texture value/signature pair is the {@code textures} property of a game profile,
 * exactly as Mojang (or the Geyser global API, for Bedrock players) issues it, so it can be
 * placed on skulls or NPC profiles without the server ever contacting Mojang.
 */
public final class ProfileData {
    private final UUID uuid;
    private final String name;
    private final String textureValue;
    private final String textureSignature;
    private final String skinUrl;
    private final boolean slim;
    private final LookupSource source;

    /**
     * Creates a profile snapshot. The skin URL and model are read from the texture value.
     *
     * @param uuid             the player's UUID, may be null when only the name is known
     * @param name             the player's name, may be null when only the UUID is known
     * @param textureValue     the base64 {@code textures} property value, may be null
     * @param textureSignature the property's signature, may be null
     * @param source           where the data came from
     */
    public ProfileData(UUID uuid, String name, String textureValue, String textureSignature, LookupSource source) {
        this.uuid = uuid;
        this.name = name;
        this.textureValue = blankToNull(textureValue);
        this.textureSignature = this.textureValue == null ? null : blankToNull(textureSignature);
        this.source = source;

        String url = null;
        boolean isSlim = false;
        if (this.textureValue != null) {
            try {
                String json = new String(Base64.getDecoder().decode(this.textureValue), StandardCharsets.UTF_8);
                // The instance parse() form exists on the old Gson that 1.8-1.17 servers bundle; parseString() does not.
                JsonElement root = new JsonParser().parse(json);
                JsonObject skin = root.getAsJsonObject().getAsJsonObject("textures").getAsJsonObject("SKIN");
                if (skin != null) {
                    url = skin.get("url").getAsString();
                    JsonObject metadata = skin.getAsJsonObject("metadata");
                    isSlim = metadata != null && metadata.has("model") && "slim".equals(metadata.get("model").getAsString());
                }
            } catch (Throwable ignored) {
                // A malformed value still gets passed through; only the convenience fields stay empty.
            }
        }
        this.skinUrl = url;
        this.slim = isSlim;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    /**
     * Returns the player's UUID.
     *
     * @return the UUID, or null if unknown
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Returns the player's name. For Bedrock players this is the name the server uses,
     * which normally carries the Floodgate prefix.
     *
     * @return the name, or null if unknown
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the base64 {@code textures} property value.
     *
     * @return the value, or null when no skin is known
     */
    public String getTextureValue() {
        return textureValue;
    }

    /**
     * Returns the {@code textures} property signature.
     *
     * @return the signature, or null when unsigned or unknown
     */
    public String getTextureSignature() {
        return textureSignature;
    }

    /**
     * Returns the skin's texture URL (on textures.minecraft.net).
     *
     * @return the URL, or null when no skin is known
     */
    public String getSkinUrl() {
        return skinUrl;
    }

    /**
     * Whether the skin uses the slim (Alex) arm model.
     *
     * @return true for slim skins
     */
    public boolean isSlim() {
        return slim;
    }

    /**
     * Returns where this data came from.
     *
     * @return the lookup source
     */
    public LookupSource getSource() {
        return source;
    }

    /**
     * Whether a skin texture is known.
     *
     * @return true if a texture value is present
     */
    public boolean hasTextures() {
        return textureValue != null;
    }

    /**
     * Returns the UUID's classification.
     *
     * @return the UUID type
     */
    public UuidType getUuidType() {
        return UuidType.of(uuid);
    }

    /**
     * Whether this is a Bedrock (Floodgate) player.
     *
     * @return true when the UUID is a Floodgate UUID
     */
    public boolean isBedrock() {
        return getUuidType() == UuidType.BEDROCK;
    }

    /**
     * Returns the Bedrock XUID for Floodgate players.
     *
     * @return the XUID, or empty for non-Bedrock players
     */
    public Optional<Long> getXuid() {
        return isBedrock() ? Optional.of(UuidType.toXuid(uuid)) : Optional.empty();
    }

    /**
     * Returns a copy whose missing fields are filled from another snapshot.
     * Fields already set on this snapshot win.
     *
     * @param other the snapshot to fill from, may be null
     * @return the merged snapshot
     */
    public ProfileData mergeFrom(ProfileData other) {
        if (other == null) return this;

        UUID u = uuid != null ? uuid : other.uuid;
        String n = name != null ? name : other.name;
        if (textureValue != null) return new ProfileData(u, n, textureValue, textureSignature, source);
        return new ProfileData(u, n, other.textureValue, other.textureSignature, other.textureValue != null ? other.source : source);
    }

    @Override
    public String toString() {
        return "ProfileData{uuid=" + uuid + ", name=" + name + ", textures=" + hasTextures() + ", source=" + source + "}";
    }
}
