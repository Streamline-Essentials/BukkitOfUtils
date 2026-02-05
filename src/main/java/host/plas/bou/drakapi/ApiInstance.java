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

@Getter @Setter
public class ApiInstance {
    public static final String BASE_URL = "https://api.drak.gg/api/v1/";

    @Nullable
    private String endpoint;
    @Nullable
    private JsonObject body;
    private Map<String, String> parameters;

    public ApiInstance(@Nullable String endpoint, @Nullable JsonObject body, Map<String, String> parameters) {
        this.endpoint = endpoint;
        this.body = body;
        this.parameters = parameters;
    }

    public ApiInstance() {
        this(null, null, new HashMap<>());
    }

    public ApiInstance withEndpoint(@Nullable String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public ApiInstance withEndpoint(ApiEndpoint endpoint) {
        return withEndpoint(endpoint.getPath());
    }

    public String getMainUrl() {
        return BASE_URL + (getEndpoint() == null ? "" : getEndpoint());
    }

    public ApiInstance withBody(@Nullable JsonObject body) {
        this.body = body;
        return this;
    }

    public ApiInstance withBody(@Nullable String body) {
        this.body = body == null ? null : new Gson().fromJson(body, JsonObject.class);
        return this;
    }

    public ApiInstance withParameters(@Nullable Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public ApiInstance withParameter(@Nullable String key, @Nullable String value) {
        ensureParameters();

        this.parameters.put(key, value);
        return this;
    }

    public String getParameter(@Nullable String key) {
        ensureParameters();

        return this.parameters.get(key);
    }

    public void ensureParameters() {
        if (this.getParameters() == null) {
            this.parameters = new HashMap<>();
        }
    }

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

    public JsonObject get() {
        return request(RequestType.GET);
    }

    public JsonObject post() {
        return request(RequestType.POST);
    }

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

    public static JsonObject getResponse(String url, Map<String, String> params) {
        GetRequest request = Unirest.get(url);

        return formatResponse(request, params);
    }

    public static JsonObject postResponse(String url, Map<String, String> params) {
        HttpRequestWithBody request = Unirest.post(url);

        return formatResponse(request, params);
    }

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

    public static ApiInstance create() {
        return new ApiInstance();
    }
}
