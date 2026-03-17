package host.plas.bou.drakapi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kong.unirest.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a configurable API request instance for the Drak API.
 * Supports fluent builder-style configuration of endpoint, body, and query parameters.
 */
@Getter @Setter
public class ApiInstance {
    /** The base URL for all Drak API requests. */
    public static final String BASE_URL = "https://api.drak.gg/api/v1/";

    /**
     * The API endpoint path, or {@code null} for no endpoint.
     * @param endpoint the API endpoint path to set
     * @return the API endpoint path
     */
    @Nullable
    private String endpoint;
    /**
     * The JSON request body, or {@code null} for no body.
     * @param body the JSON request body to set
     * @return the JSON request body
     */
    @Nullable
    private JsonObject body;
    /**
     * The query parameters for this API request.
     * @param parameters the query parameters map to set
     * @return the query parameters map
     */
    private Map<String, String> parameters;

    /**
     * Constructs an ApiInstance with the specified endpoint, body, and parameters.
     *
     * @param endpoint   the API endpoint path, or {@code null} for no endpoint
     * @param body       the JSON request body, or {@code null} for no body
     * @param parameters the query parameters map
     */
    public ApiInstance(@Nullable String endpoint, @Nullable JsonObject body, Map<String, String> parameters) {
        this.endpoint = endpoint;
        this.body = body;
        this.parameters = parameters;
    }

    /**
     * Constructs an empty ApiInstance with no endpoint, no body, and an empty parameter map.
     */
    public ApiInstance() {
        this(null, null, new HashMap<>());
    }

    /**
     * Sets the endpoint path for this API instance.
     *
     * @param endpoint the endpoint path string, or {@code null}
     * @return this instance for method chaining
     */
    public ApiInstance withEndpoint(@Nullable String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the endpoint using an {@link ApiEndpoint} enum value.
     *
     * @param endpoint the API endpoint enum constant
     * @return this instance for method chaining
     */
    public ApiInstance withEndpoint(ApiEndpoint endpoint) {
        return withEndpoint(endpoint.getPath());
    }

    /**
     * Builds and returns the full URL by combining the base URL with the endpoint.
     *
     * @return the complete API URL string
     */
    public String getMainUrl() {
        return BASE_URL + (getEndpoint() == null ? "" : getEndpoint());
    }

    /**
     * Sets the JSON body for this API instance.
     *
     * @param body the JSON object to use as the request body, or {@code null}
     * @return this instance for method chaining
     */
    public ApiInstance withBody(@Nullable JsonObject body) {
        this.body = body;
        return this;
    }

    /**
     * Sets the JSON body by parsing the given string.
     *
     * @param body the JSON string to parse into the request body, or {@code null}
     * @return this instance for method chaining
     */
    public ApiInstance withBody(@Nullable String body) {
        this.body = body == null ? null : new Gson().fromJson(body, JsonObject.class);
        return this;
    }

    /**
     * Replaces the entire parameter map with the given map.
     *
     * @param parameters the new query parameters map, or {@code null}
     * @return this instance for method chaining
     */
    public ApiInstance withParameters(@Nullable Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * Adds or updates a single query parameter.
     *
     * @param key   the parameter key
     * @param value the parameter value
     * @return this instance for method chaining
     */
    public ApiInstance withParameter(@Nullable String key, @Nullable String value) {
        ensureParameters();

        this.parameters.put(key, value);
        return this;
    }

    /**
     * Retrieves the value of a query parameter by its key.
     *
     * @param key the parameter key to look up
     * @return the parameter value, or {@code null} if not found
     */
    public String getParameter(@Nullable String key) {
        ensureParameters();

        return this.parameters.get(key);
    }

    /**
     * Ensures the parameters map is initialized, creating an empty map if it is {@code null}.
     */
    public void ensureParameters() {
        if (this.getParameters() == null) {
            this.parameters = new HashMap<>();
        }
    }

    /**
     * Converts the current parameters into a URL-encoded query string.
     *
     * @return the URL-encoded parameter string
     */
    public String toParameterString() {
        ensureParameters();

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : getParameters().entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return toParameterableString(sb.toString());
    }

    /**
     * Executes a GET request using this instance's configuration.
     *
     * @return the JSON response object
     */
    public JsonObject get() {
        return request(RequestType.GET);
    }

    /**
     * Executes a POST request using this instance's configuration.
     *
     * @return the JSON response object
     */
    public JsonObject post() {
        return request(RequestType.POST);
    }

    /**
     * Executes an HTTP request of the specified type using this instance's configuration.
     *
     * @param type the HTTP request type (GET, POST, etc.)
     * @return the JSON response object, or an empty JSON object for unsupported types
     */
    public JsonObject request(RequestType type) {
        switch (type) {
            case GET:
                return getResponse(getMainUrl(), getParameters());
            case POST:
                return postResponse(getMainUrl(), getParameters());
            default:
                return new JsonObject();
        }
    }

    /**
     * Sends a GET request to the specified URL with query parameters and returns the JSON response.
     *
     * @param url    the target URL
     * @param params the query parameters to include
     * @return the parsed JSON response object
     */
    public static JsonObject getResponse(String url, Map<String, String> params) {
        GetRequest request = Unirest.get(url);

        return formatResponse(request, params);
    }

    /**
     * Sends a POST request to the specified URL with query parameters and returns the JSON response.
     *
     * @param url    the target URL
     * @param params the query parameters to include
     * @return the parsed JSON response object
     */
    public static JsonObject postResponse(String url, Map<String, String> params) {
        HttpRequestWithBody request = Unirest.post(url);

        return formatResponse(request, params);
    }

    /**
     * Formats and executes an HTTP request by adding headers and query parameters,
     * then parses the response into a JSON object.
     *
     * @param request the HTTP request to format and execute
     * @param params  the query parameters to add to the request
     * @return the parsed JSON response object
     */
    public static JsonObject formatResponse(HttpRequest<?> request, Map<String, String> params) {
        request = request.header("accept", "application/json");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            request = request.queryString(entry.getKey(), entry.getValue());
        }

        HttpResponse<JsonNode> response = request.asJson();
        return new JsonParser().parse(response.getBody().toString()).getAsJsonObject();
    }

    /**
     * Converts a string into a parameterable string by replacing invalid characters with correct ones.
     * Uses UTF-8 encoding for spaces and special characters. (Similar to URL encoding.)
     * @param string The string to convert.
     * @return The parameterable string.
     */
    public static String toParameterableString(String string) {
        try {
            return URLEncoder.encode(string, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return string;
        }
    }

    /**
     * Creates a new empty ApiInstance using the default constructor.
     *
     * @return a new ApiInstance with no endpoint, body, or parameters configured
     */
    public static ApiInstance create() {
        return new ApiInstance();
    }
}
