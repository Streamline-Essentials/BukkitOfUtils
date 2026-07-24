package host.plas.bou.drakapi;

import com.google.gson.JsonObject;
import gg.drak.thebase.objects.SingleSet;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * Provides high-level methods for interacting with the Drak API,
 * including player height/scale operations and Modrinth version checks.
 *
 * @see <a href="https://api.drak.gg/api-docs">Drak API docs</a>
 */
public class DrakAPI {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DrakAPI() {
        // Utility class
    }

    /**
     * Retrieves the scale value for a player by their UUID from the Drak API.
     *
     * @param uuid the UUID of the player to look up
     * @return a pair where the first element indicates whether the scale was found,
     *         and the second element is the scale value (or -1.0 if not found)
     */
    public static SingleSet<Boolean, Double> getScale(String uuid) {
        JsonObject response = ApiInstance.create()
                .withEndpoint(ApiEndpoint.PLUGINS_HEIGHTS)
                .withParameter("uuid", uuid)
                .request(RequestType.GET);
        boolean found = response.has("found") && response.get("found").getAsBoolean();
        double scale = found && response.has("scale") ? response.get("scale").getAsDouble() : -1.0;
        if (scale == -1.0) {
            found = false;
        }

        return new SingleSet<>(found, scale);
    }

    /**
     * Sets the scale value for a player by their UUID via the Drak API.
     *
     * @param uuid  the UUID of the player to update
     * @param scale the new scale value to set
     * @return a pair where the first element indicates whether the operation succeeded,
     *         and the second element is the returned scale value (or -1.0 if failed)
     */
    public static SingleSet<Boolean, Double> setScale(String uuid, double scale) {
        JsonObject response = ApiInstance.create()
                .withEndpoint(ApiEndpoint.PLUGINS_HEIGHTS)
                .withParameter("uuid", uuid).withParameter("scale", String.valueOf(scale))
                .request(RequestType.POST);
        boolean found = response.has("success") && response.get("success").getAsBoolean();
        double scaleReturned = found && response.has("scale") ? response.get("scale").getAsDouble() : -1.0;
        if (scaleReturned == -1.0) {
            found = false;
        }

        return new SingleSet<>(found, scaleReturned);
    }

    /**
     * Compares an installed version against the latest Modrinth release for a project.
     *
     * @param idOrSlug       Modrinth project ID or slug
     * @param currentVersion installed version number
     * @return parsed version comparison result
     */
    public static VersionCheckResult checkVersion(String idOrSlug, String currentVersion) {
        if (idOrSlug == null || idOrSlug.isBlank()) {
            return VersionCheckResult.failure("Modrinth project id/slug is required.");
        }
        if (currentVersion == null || currentVersion.isBlank()) {
            return VersionCheckResult.failure("Current version is required.");
        }

        JsonObject body = new JsonObject();
        body.addProperty("version", currentVersion);

        JsonObject response = ApiInstance.create()
                .withEndpoint(ApiEndpoint.MODRINTH_CHECK)
                .withIdOrSlug(idOrSlug)
                .withBody(body)
                .request(RequestType.POST);

        return VersionCheckResult.fromJson(response, idOrSlug);
    }

    /**
     * Compares a plugin's installed version against Modrinth.
     *
     * @param plugin   plugin providing {@link Plugin#getDescription()} version
     * @param idOrSlug Modrinth project ID or slug
     * @return parsed version comparison result
     */
    public static VersionCheckResult checkVersion(Plugin plugin, String idOrSlug) {
        if (plugin == null) {
            return VersionCheckResult.failure("Plugin is null.");
        }
        return checkVersion(idOrSlug, plugin.getDescription().getVersion());
    }

    /**
     * Returns latest-version status JSON for a Modrinth project.
     *
     * @param idOrSlug Modrinth project ID or slug
     * @return raw JSON response
     */
    public static JsonObject getVersionStatus(String idOrSlug) {
        return ApiInstance.create()
                .withEndpoint(ApiEndpoint.MODRINTH_STATUS)
                .withIdOrSlug(idOrSlug)
                .request(RequestType.GET);
    }

    /**
     * Returns project details JSON for a Modrinth project.
     *
     * @param idOrSlug Modrinth project ID or slug
     * @return raw JSON response
     */
    public static JsonObject getProject(String idOrSlug) {
        return ApiInstance.create()
                .withEndpoint(ApiEndpoint.MODRINTH_PROJECT)
                .withIdOrSlug(idOrSlug)
                .request(RequestType.GET);
    }

    /**
     * Returns the latest download metadata for a Modrinth project.
     *
     * @param idOrSlug Modrinth project ID or slug
     * @return raw JSON response (includes {@code downloadUrl} when successful)
     */
    public static JsonObject getLatestDownload(String idOrSlug) {
        return ApiInstance.create()
                .withEndpoint(ApiEndpoint.MODRINTH_DOWNLOAD)
                .withIdOrSlug(idOrSlug)
                .request(RequestType.GET);
    }

    /**
     * Returns the download metadata for an exact Modrinth version.
     *
     * @param idOrSlug Modrinth project ID or slug
     * @param version  version number or Modrinth version ID
     * @return raw JSON response
     */
    public static JsonObject getExactDownload(String idOrSlug, String version) {
        return ApiInstance.create()
                .withEndpoint(ApiEndpoint.MODRINTH_DOWNLOAD_VERSION)
                .withIdOrSlug(idOrSlug)
                .withPathParameter("version", version)
                .request(RequestType.GET);
    }

    /**
     * Convenience extractor for {@code downloadUrl} from a download endpoint response.
     *
     * @param downloadResponse response from {@link #getLatestDownload(String)} or {@link #getExactDownload(String, String)}
     * @return download URL, or {@code null} if missing
     */
    @Nullable
    public static String extractDownloadUrl(JsonObject downloadResponse) {
        if (downloadResponse == null || !downloadResponse.has("downloadUrl")
                || downloadResponse.get("downloadUrl").isJsonNull()) {
            return null;
        }
        return downloadResponse.get("downloadUrl").getAsString();
    }
}
