package host.plas.bou.drakapi;

import lombok.Getter;

/**
 * Enumeration of available API endpoints for the Drak API.
 */
@Getter
public enum ApiEndpoint {
    /** Endpoint for retrieving plugin height data. */
    PLUGINS_HEIGHTS("plugins/heights"),
    ;

    /**
     * The relative URL path for this endpoint.
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
