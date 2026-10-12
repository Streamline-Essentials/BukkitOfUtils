# Updates since 1.19.4

Covers `1.19.4` → `1.19.5`.

## Highlights

- New player lookup service (`host.plas.bou.profiles`) that resolves names, UUIDs, and skins
  **without ever contacting Mojang**.
- Bedrock / Floodgate players are fully supported, including their skins on player heads.
- Offline-mode and no-internet servers get immediate answers instead of waiting on web timeouts.

## New: player lookups (`host.plas.bou.profiles`)

### `PlayerLookup`

Resolves players by UUID or name. Sources are tried in this order, and the first complete answer
wins:

1. Online players
2. The server's own user cache
3. Floodgate / Geyser (if installed)
4. Values derived locally (offline-mode UUIDs, Floodgate UUIDs)
5. Geyser global API (Bedrock players only)
6. PlayerDB
7. MineTools

Floodgate and offline-mode UUIDs are never sent to Java profile services. Each web provider
gets exponential backoff after failures (from 30 seconds, doubling, up to a configurable cap).
Results are cached, with separate lifetimes for found, not-found, and failed lookups.

Main API:

| Method | Purpose |
| --- | --- |
| `resolve(UUID)` / `resolve(String)` | Async `LookupResult` (status plus profile) |
| `resolveAccount(String)` | Async lookup of the real account behind a name, even on offline-mode servers |
| `resolveBlocking(..., timeoutMillis)` / `resolveAccountBlocking(...)` | Blocking variants with a bounded timeout |
| `lookup(UUID)` / `lookup(String)` | Async `Optional<ProfileData>` |
| `getCached(...)`, `getLocal(...)` | Cache-only and server-data-only reads, no network |
| `getUuidWithoutLookup(String)` / `getNameWithoutLookup(UUID)` | Instant answers from local data, no network |
| `invalidate(...)`, `clearCache()` | Cache control |
| `isOnlineMode()`, `isProxied()`, `usesAccountUuids()` | Server identity mode checks |
| `isBedrock(UUID)`, `hasBedrockPrefix(String)`, `getBedrockPrefix()` | Bedrock helpers |
| `isMojangResolvable(UUID)` | Whether a UUID belongs to a real Java account |

### Supporting types

- `LookupResult`: `Status` is `FOUND`, `NOT_FOUND` (a provider said definitively that no such player
  exists), or `UNAVAILABLE` (nothing known locally and no provider reachable).
- `ProfileData`: UUID, name, texture value/signature, skin URL, slim-arm flag, and source.
- `LookupSource`: `ONLINE_PLAYER`, `SERVER_CACHE`, `FLOODGATE`, `DERIVED`, `GEYSER_API`,
  `PLAYERDB`, `MINETOOLS`.
- `UuidType`: classifies a UUID as `ONLINE`, `OFFLINE`, `BEDROCK`, or `OTHER`.

### `HeadUtils`

Builds player heads with complete, textured profiles, so the server never has to resolve them
through Mojang:

- `createHead(UUID)` / `createHeadByName(String)` (async)
- `createHead(ProfileData)`, `createTexturedHead(String textureValue)`, `createBlankHead()`
- `applyTexture(SkullMeta, String)`, `applyProfile(SkullMeta, ProfileData)`

### `FloodgateCompat` (`host.plas.bou.compat.floodgate`)

Optional Floodgate integration, accessed reflectively: `isAvailable()`, `isBedrockPlayer(UUID)`,
`isLinked(UUID)`, `getXuid(UUID)`, `getGamertag(UUID)`, `getJavaUsername(UUID)`,
`getPlayerPrefix()`.

## Changed

- **`UUIDFetcher`** keeps its signatures but now delegates to `PlayerLookup` with a 10-second
  timeout (`UUIDFetcher.TIMEOUT_MILLIS`), replacing the old PlayerDB-only client.
  - Known names and UUIDs are answered locally with no request.
  - `getUUID(name)` returns the **real account UUID** (Mojang UUID for Java names, Floodgate UUID
    for prefixed Bedrock names), even on offline-mode servers. Use `PlayerLookup.resolve(String)`
    if you need the server-side (offline-mode) UUID.
- **`UuidUtils`**
  - Floodgate UUIDs count as valid players.
  - Name/UUID validation answers from local data first.
  - **Behaviour change:** validation now fails only on a definite `NOT_FOUND`. If lookups are
    disabled or every provider is unreachable, the player is accepted, since they cannot be
    disproven. Previously any lookup error returned `false`.
  - `isOfflineMode()` still means `online-mode=false` (and is also true behind a UUID-forwarding
    proxy). Use `PlayerLookup.usesAccountUuids()` to check whether players have real account UUIDs.
- **`SenderUtils.getUuidByName`** checks local data before falling back to Bukkit's blocking
  `getOfflinePlayer(String)`.
- **Skull deserialization (`VersionTool`)** applies serialized textures directly. Bedrock and
  offline-mode owners use cached skins instead of an owner the server cannot resolve.
- **`plugin.yml`** adds `softdepend: [ floodgate, Geyser-Spigot ]` so their APIs are ready when
  BOU loads.
- The lookup service is shut down cleanly on plugin disable.

## New config options (`BaseConfig`)

```yaml
player-lookups:
  web:
    enabled: true          # false = only use data already on the server (offline / no-internet setups)
    timeout-millis: 4000   # connect + read timeout per web request
  cache:
    found-minutes: 360     # successful lookups
    missing-minutes: 30    # "no such player" answers
    failed-minutes: 5      # incomplete/failed lookups before retry
  backoff:
    max-minutes: 30        # longest a failing provider is skipped
  bedrock:
    prefix: "."            # Bedrock name prefix when Floodgate isn't on this server (e.g. proxy-only)
```

Mojang is never contacted, whatever these settings are.
