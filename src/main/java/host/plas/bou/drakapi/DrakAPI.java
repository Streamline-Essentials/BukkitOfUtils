package host.plas.bou.drakapi;

import com.google.gson.JsonObject;
import gg.drak.thebase.objects.SingleSet;
import kong.unirest.*;

public class DrakAPI {
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
