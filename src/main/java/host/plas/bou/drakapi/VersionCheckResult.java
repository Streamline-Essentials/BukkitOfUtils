package host.plas.bou.drakapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Parsed response from {@code POST /api/v1/modrinth/projects/{idOrSlug}/check}.
 */
@Getter
public class VersionCheckResult {
    private final boolean success;
    private final VersionCheckStatus status;
    private final boolean needsUpdate;
    private final String currentVersion;
    @Nullable
    private final String latestVersion;
    @Nullable
    private final String message;
    @Nullable
    private final String downloadUrl;
    @Nullable
    private final String projectSlug;
    private final JsonObject raw;

    public VersionCheckResult(boolean success,
                              VersionCheckStatus status,
                              boolean needsUpdate,
                              String currentVersion,
                              @Nullable String latestVersion,
                              @Nullable String message,
                              @Nullable String downloadUrl,
                              @Nullable String projectSlug,
                              JsonObject raw) {
        this.success = success;
        this.status = status == null ? VersionCheckStatus.UNKNOWN : status;
        this.needsUpdate = needsUpdate;
        this.currentVersion = currentVersion;
        this.latestVersion = latestVersion;
        this.message = message;
        this.downloadUrl = downloadUrl;
        this.projectSlug = projectSlug;
        this.raw = raw == null ? new JsonObject() : raw;
    }

    public static VersionCheckResult failure(String message) {
        return failure(message, new JsonObject());
    }

    public static VersionCheckResult failure(String message, JsonObject raw) {
        return new VersionCheckResult(
                false,
                VersionCheckStatus.UNKNOWN,
                false,
                null,
                null,
                message,
                null,
                null,
                raw
        );
    }

    public static VersionCheckResult fromJson(JsonObject json, @Nullable String projectSlug) {
        if (json == null || json.entrySet().isEmpty()) {
            return failure("Empty response from Drak API.", json == null ? new JsonObject() : json);
        }
        if (json.has("error") && json.get("error").getAsBoolean()) {
            String msg = json.has("message") ? json.get("message").getAsString() : "Drak API returned an error.";
            return failure(msg, json);
        }
        if (!json.has("status") && !json.has("needsUpdate")) {
            String msg = json.has("message") ? json.get("message").getAsString() : "Unexpected Drak API response.";
            return failure(msg, json);
        }

        VersionCheckStatus status = VersionCheckStatus.fromApi(getAsString(json, "status"));
        boolean needsUpdate = json.has("needsUpdate") && json.get("needsUpdate").getAsBoolean();
        String currentVersion = getAsString(json, "currentVersion");
        String latestVersion = getAsString(json, "latestVersion");
        String message = getAsString(json, "message");
        String downloadUrl = extractDownloadUrl(json);

        return new VersionCheckResult(
                true,
                status,
                needsUpdate,
                currentVersion,
                latestVersion,
                message,
                downloadUrl,
                projectSlug,
                json
        );
    }

    @Nullable
    private static String extractDownloadUrl(JsonObject json) {
        if (json.has("downloadUrl") && json.get("downloadUrl").isJsonPrimitive()) {
            return json.get("downloadUrl").getAsString();
        }
        if (json.has("latest") && json.get("latest").isJsonObject()) {
            JsonObject latest = json.getAsJsonObject("latest");
            if (latest.has("primaryDownloadUrl") && latest.get("primaryDownloadUrl").isJsonPrimitive()) {
                return latest.get("primaryDownloadUrl").getAsString();
            }
        }
        return null;
    }

    @Nullable
    private static String getAsString(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return null;
        }
        JsonElement element = json.get(key);
        return element.isJsonPrimitive() ? element.getAsString() : null;
    }

    public boolean isUpToDate() {
        return success && status == VersionCheckStatus.UP_TO_DATE;
    }

    public boolean isBehind() {
        return success && status == VersionCheckStatus.BEHIND;
    }

    public boolean isAhead() {
        return success && status == VersionCheckStatus.AHEAD;
    }
}
