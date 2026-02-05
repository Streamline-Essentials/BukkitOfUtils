package host.plas.bou.drakapi;

import lombok.Getter;

@Getter
public enum ApiEndpoint {
    PLUGINS_HEIGHTS("plugins/heights"),
    ;

    private final String path;

    ApiEndpoint(String path) {
        this.path = path;
    }
}
