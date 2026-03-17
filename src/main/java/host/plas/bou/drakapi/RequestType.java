package host.plas.bou.drakapi;

/**
 * Enumeration of supported HTTP request types for API operations.
 */
public enum RequestType {
    /** HTTP GET request, used for retrieving resources. */
    GET,
    /** HTTP POST request, used for creating resources. */
    POST,
    /** HTTP PUT request, used for replacing resources. */
    PUT,
    /** HTTP DELETE request, used for removing resources. */
    DELETE,
    /** HTTP PATCH request, used for partially updating resources. */
    PATCH,
    ;
}
