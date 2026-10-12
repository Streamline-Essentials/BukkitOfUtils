+ Player names, UUIDs, and skins no longer use Mojang.
  + Server data and cache are checked first.
  + Falls back to Geyser, PlayerDB, then MineTools.
  + Failing web services are paused, then retried later.
+ Bedrock players are fully supported through Floodgate and Geyser.
  + Bedrock player heads now show their real skins.
  + Saved heads keep their skins after reloading.
+ Offline-mode servers get instant player lookups, no waiting.
+ Unreachable lookup services no longer reject valid player names.
+ Added a player-lookups config section.
  + Set web.enabled to false for no-internet servers.
  + Cache times, timeouts, and backoff are configurable.
  + Set bedrock.prefix when Floodgate runs only on proxy.
