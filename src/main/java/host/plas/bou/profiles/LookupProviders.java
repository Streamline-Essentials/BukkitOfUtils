package host.plas.bou.profiles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * HTTP access to the non-Mojang profile services.
 * <p>
 * Each provider method returns the data, returns {@code null} when the service answered that the
 * player does not exist, or throws {@link IOException} when the service could not answer (offline,
 * timeout, rate limit, outage). Every provider sits behind its own {@link Gate}, so a failing
 * service is skipped for a backoff period instead of being hammered.
 */
final class LookupProviders {
    private LookupProviders() {}

    static final Gate GEYSER = new Gate("api.geysermc.org");
    static final Gate PLAYERDB = new Gate("playerdb.co");
    static final Gate MINETOOLS = new Gate("api.minetools.eu");

    /**
     * Circuit breaker for one provider. After a failure the provider is skipped for 30 seconds,
     * doubling per consecutive failure up to the configured maximum.
     */
    static final class Gate {
        private static final long BASE_BACKOFF_MILLIS = TimeUnit.SECONDS.toMillis(30);

        final String host;
        private int failures;
        private long blockedUntil;

        Gate(String host) {
            this.host = host;
        }

        synchronized boolean isOpen() {
            return System.currentTimeMillis() >= blockedUntil;
        }

        synchronized void success() {
            failures = 0;
            blockedUntil = 0;
        }

        synchronized void failure(long maxBackoffMillis) {
            failures = Math.min(failures + 1, 20);
            long backoff = Math.min(BASE_BACKOFF_MILLIS << (failures - 1), maxBackoffMillis);
            blockedUntil = System.currentTimeMillis() + Math.max(backoff, 0);
        }

        synchronized void reset() {
            success();
        }
    }

    static final class Response {
        final int status;
        final String body;

        Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

    static Response get(String url, int timeoutMillis, String userAgent) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        // Some endpoints redirect unknown inputs to api.mojang.com; following that would hit the
        // very rate limit these providers exist to avoid.
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);
        connection.setUseCaches(false);
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Accept", "application/json");

        try {
            int status = connection.getResponseCode();
            InputStream in = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String body = "";
            if (in != null) {
                try (InputStream stream = in) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = stream.read(buffer)) != -1) out.write(buffer, 0, read);
                    body = out.toString(StandardCharsets.UTF_8.name());
                }
            }
            return new Response(status, body);
        } finally {
            connection.disconnect();
        }
    }

    /** Throws for statuses that mean "could not answer" rather than "no such player". */
    private static void requireAnswer(Gate gate, Response response) throws IOException {
        if (response.status == 429 || response.status >= 500 || (response.status >= 300 && response.status < 400)) {
            throw new IOException(gate.host + " returned HTTP " + response.status);
        }
    }

    private static JsonObject parseObject(Gate gate, String body) throws IOException {
        try {
            JsonElement element = new JsonParser().parse(body);
            if (element != null && element.isJsonObject()) return element.getAsJsonObject();
        } catch (Throwable ignored) {
            // Fall through: an HTML error page or truncated body is a failed answer.
        }
        throw new IOException(gate.host + " returned a non-JSON body");
    }

    private static String string(JsonObject object, String key) {
        JsonElement e = object == null ? null : object.get(key);
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }

    private static String encode(String s) throws IOException {
        return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
    }

    static UUID parseUuid(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.length() == 32) {
            s = s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16) + "-" + s.substring(16, 20) + "-" + s.substring(20);
        }
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static ProfileData fromProperties(UUID uuid, String name, JsonArray properties, LookupSource source) {
        String value = null;
        String signature = null;
        if (properties != null) {
            for (JsonElement element : properties) {
                if (!element.isJsonObject()) continue;
                JsonObject property = element.getAsJsonObject();
                if ("textures".equals(string(property, "name"))) {
                    value = string(property, "value");
                    signature = string(property, "signature");
                    break;
                }
            }
        }
        return new ProfileData(uuid, name, value, signature, source);
    }

    // ---- Geyser global API (Bedrock) ----

    /** The Xbox endpoints answer an unknown player with HTTP 503 and this message, not a 404. */
    private static boolean isGeyserUnknownUser(Response response) {
        return response.body != null && response.body.contains("Unable to find user");
    }

    /**
     * Looks up the skin Floodgate uploaded for a Bedrock player.
     *
     * @return the skin, or null if the API has no skin cached for this XUID
     */
    static ProfileData geyserSkin(long xuid, int timeout, String ua) throws IOException {
        Response response = get("https://api.geysermc.org/v2/skin/" + xuid, timeout, ua);
        requireAnswer(GEYSER, response);
        if (response.status >= 400) return null;

        JsonObject object = parseObject(GEYSER, response.body);
        String value = string(object, "value");
        if (value == null) return null;
        return new ProfileData(UuidType.fromXuid(xuid), null, value, string(object, "signature"), LookupSource.GEYSER_API);
    }

    /**
     * Looks up a Bedrock gamertag by XUID.
     *
     * @return the gamertag, or null if unknown to the API
     */
    static String geyserGamertag(long xuid, int timeout, String ua) throws IOException {
        Response response = get("https://api.geysermc.org/v2/xbox/gamertag/" + xuid, timeout, ua);
        if (isGeyserUnknownUser(response)) return null;
        requireAnswer(GEYSER, response);
        if (response.status >= 400) return null;
        return string(parseObject(GEYSER, response.body), "gamertag");
    }

    /**
     * Looks up a Bedrock XUID by gamertag.
     *
     * @return the XUID, or null if unknown to the API
     */
    static Long geyserXuid(String gamertag, int timeout, String ua) throws IOException {
        Response response = get("https://api.geysermc.org/v2/xbox/xuid/" + encode(gamertag), timeout, ua);
        if (isGeyserUnknownUser(response)) return null;
        requireAnswer(GEYSER, response);
        if (response.status >= 400) return null;

        JsonObject object = parseObject(GEYSER, response.body);
        JsonElement xuid = object.get("xuid");
        if (xuid == null || xuid.isJsonNull()) return null;
        try {
            return xuid.getAsLong();
        } catch (Throwable e) {
            return null;
        }
    }

    // ---- PlayerDB (Java) ----

    /**
     * Looks up a Java player by UUID or name.
     *
     * @return the profile, or null if the player does not exist
     */
    static ProfileData playerDb(String uuidOrName, int timeout, String ua) throws IOException {
        Response response = get("https://playerdb.co/api/player/minecraft/" + encode(uuidOrName), timeout, ua);
        requireAnswer(PLAYERDB, response);

        JsonObject object = parseObject(PLAYERDB, response.body);
        JsonElement success = object.get("success");
        if (success == null || !success.getAsBoolean()) {
            String code = string(object, "code");
            // Only an explicit "invalid/unknown player" code means the player is missing; any
            // other failure (e.g. PlayerDB's own upstream errors) is an unanswered lookup.
            if (code != null && (code.contains("invalid") || code.contains("not_found"))) return null;
            throw new IOException("playerdb.co failed: " + code);
        }

        JsonObject player = object.getAsJsonObject("data").getAsJsonObject("player");
        UUID uuid = parseUuid(string(player, "id"));
        if (uuid == null) return null;
        JsonElement properties = player.get("properties");
        return fromProperties(uuid, string(player, "username"),
                properties != null && properties.isJsonArray() ? properties.getAsJsonArray() : null,
                LookupSource.PLAYERDB);
    }

    // ---- MineTools (Java) ----

    /**
     * Looks up a Java player's UUID and name by name.
     *
     * @return the profile (without textures), or null if the player does not exist
     */
    static ProfileData mineToolsUuid(String name, int timeout, String ua) throws IOException {
        Response response = get("https://api.minetools.eu/uuid/" + encode(name), timeout, ua);
        requireAnswer(MINETOOLS, response);

        JsonObject object = parseObject(MINETOOLS, response.body);
        if (!"OK".equals(string(object, "status"))) {
            String error = string(object, "error");
            if (error != null && error.toLowerCase().contains("invalid")) return null;
            throw new IOException("api.minetools.eu failed: " + error);
        }
        UUID uuid = parseUuid(string(object, "id"));
        return uuid == null ? null : new ProfileData(uuid, string(object, "name"), null, null, LookupSource.MINETOOLS);
    }

    /**
     * Looks up a Java player's full profile (with textures) by UUID.
     *
     * @return the profile, or null if the player does not exist
     */
    static ProfileData mineToolsProfile(UUID uuid, int timeout, String ua) throws IOException {
        Response response = get("https://api.minetools.eu/profile/" + uuid.toString().replace("-", ""), timeout, ua);
        requireAnswer(MINETOOLS, response);

        JsonObject object = parseObject(MINETOOLS, response.body);
        JsonElement rawElement = object.get("raw");
        if (rawElement == null || !rawElement.isJsonObject()) throw new IOException("api.minetools.eu returned no profile");
        JsonObject raw = rawElement.getAsJsonObject();

        if (!"OK".equals(string(raw, "status"))) {
            String error = string(raw, "error");
            if (error != null && error.toLowerCase().contains("invalid")) return null;
            throw new IOException("api.minetools.eu failed: " + error);
        }
        UUID id = parseUuid(string(raw, "id"));
        JsonElement properties = raw.get("properties");
        return fromProperties(id != null ? id : uuid, string(raw, "name"),
                properties != null && properties.isJsonArray() ? properties.getAsJsonArray() : null,
                LookupSource.MINETOOLS);
    }
}
