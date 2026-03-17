package host.plas.bou.drakapi;

import com.google.gson.JsonObject;
import gg.drak.thebase.objects.SingleSet;
import kong.unirest.*;

/**
 * Provides high-level methods for interacting with the Drak API,
 * specifically for player height/scale operations.
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
}
