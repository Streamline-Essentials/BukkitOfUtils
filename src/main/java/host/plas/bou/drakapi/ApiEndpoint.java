package host.plas.bou.drakapi;

import lombok.Getter;

/**
 * Enumeration of available API endpoints for the Drak API.
 *
 * @see <a href="https://api.drak.gg/api-docs">Drak API docs</a>
 */
@Getter
public enum ApiEndpoint {
    /** Endpoint for retrieving plugin height data. */
    PLUGINS_HEIGHTS("plugins/heights"),

    /** List Modrinth projects for a user. */
    MODRINTH_PROJECTS("modrinth/projects"),
    /** Modrinth project details. */
    MODRINTH_PROJECT("modrinth/projects/{idOrSlug}"),
    /** List versions for a Modrinth project. */
    MODRINTH_VERSIONS("modrinth/projects/{idOrSlug}/versions"),
    /** Latest version status for a Modrinth project. */
    MODRINTH_STATUS("modrinth/projects/{idOrSlug}/status"),
    /** Compare an installed version against Modrinth. */
    MODRINTH_CHECK("modrinth/projects/{idOrSlug}/check"),
    /** Latest version download link. */
    MODRINTH_DOWNLOAD("modrinth/projects/{idOrSlug}/download"),
    /** Exact version download link. */
    MODRINTH_DOWNLOAD_VERSION("modrinth/projects/{idOrSlug}/download/{version}"),
    ;

    /**
     * The relative URL path for this endpoint.
     * Path placeholders use {@code {name}} syntax (e.g. {@code {idOrSlug}}).
     *
     * @return the relative URL path
     */
    private final String path;

    /**
     * Constructs an ApiEndpoint with the given path.
     *
     * @param path the relative URL path for this endpoint
     */
    ApiEndpoint(String path) {
        this.path = path;
    }
}
