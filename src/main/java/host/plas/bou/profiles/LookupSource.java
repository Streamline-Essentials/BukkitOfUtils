package host.plas.bou.profiles;

/**
 * Where a piece of {@link ProfileData} came from.
 */
public enum LookupSource {
    /** The player is online on this server; data is read from their live game profile. */
    ONLINE_PLAYER,
    /** The server's own player data or user cache. */
    SERVER_CACHE,
    /** The Floodgate or Geyser API on this server. */
    FLOODGATE,
    /** Derived without any lookup (e.g. an offline-mode UUID computed from a name). */
    DERIVED,
    /** The Geyser global API (api.geysermc.org), for Bedrock players. */
    GEYSER_API,
    /** PlayerDB (playerdb.co). */
    PLAYERDB,
    /** MineTools (api.minetools.eu). */
    MINETOOLS,
    ;

    /**
     * Whether this source required a network request.
     *
     * @return true for web APIs
     */
    public boolean isWeb() {
        return this == GEYSER_API || this == PLAYERDB || this == MINETOOLS;
    }
}
