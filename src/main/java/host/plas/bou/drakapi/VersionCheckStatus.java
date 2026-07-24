package host.plas.bou.drakapi;

/**
 * Result status from a Modrinth version comparison against the Drak API.
 */
public enum VersionCheckStatus {
    /** Installed version matches the latest Modrinth release. */
    UP_TO_DATE,
    /** Installed version is newer than the latest Modrinth release. */
    AHEAD,
    /** Installed version is older than the latest Modrinth release. */
    BEHIND,
    /** Status could not be determined (API error, missing data, etc.). */
    UNKNOWN,
    ;

    /**
     * Parses an API status string into a {@link VersionCheckStatus}.
     *
     * @param apiStatus status string from the API (e.g. {@code up_to_date})
     * @return matching status, or {@link #UNKNOWN} if unrecognized
     */
    public static VersionCheckStatus fromApi(String apiStatus) {
        if (apiStatus == null || apiStatus.isBlank()) {
            return UNKNOWN;
        }
        switch (apiStatus.trim().toLowerCase()) {
            case "up_to_date":
                return UP_TO_DATE;
            case "ahead":
                return AHEAD;
            case "behind":
                return BEHIND;
            default:
                return UNKNOWN;
        }
    }
}
