package host.plas.bou.drakapi;

public enum ApiEndpoint {
    PLUGINS_HEIGHTS("plugins/heights");
    private final String path;

    ApiEndpoint(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }
}
