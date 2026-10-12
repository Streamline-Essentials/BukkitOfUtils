![Discord](https://github.com/Streamline-Essentials/StreamlineWiki/blob/main/website/images/JoinTheDiscord.png?raw=true)
Please join the Streamline Hub Discord to get
updates and for me to fully assist you with bugs,
questions, or suggestions.

Discord: [**click here**](https://dsc.gg/streamline)

![Summary](https://github.com/Streamline-Essentials/StreamlineWiki/blob/main/website/images/Summary.png?raw=true)
## BukkitOfUtils (BOU for short)
_Easily develop plugins that support the majority of server software!_

## Supported Platforms
- Bukkit
- Spigot
- Paper/Purpur
- Leaf/uSpigot
- Folia
- Mohist/Banner/Youer
- And more!
    - Most forks of Spigot and Paper are supported natively.

## What is it?
BOU is both a plugin and plugin-making framework / library.
It is designed to ease the development process of making Bukkit plugins.
It also provides a central framework for plugins that use it,
making the plugins that use it more organized and easier to manage
as well as making them slimmer and faster to download.

![Why This?](https://github.com/Streamline-Essentials/StreamlineWiki/blob/main/website/images/WhyThis.png?raw=true)
BOU is one of the few plugin-building-frameworks out there that supports
Bukkit, Folia, **_and_** Mohist/Banner/Youer!
Plus, its easy-to-understand classes and framework help beginners
start developing plugins that they too can understand!

The plugin is also optimized with its own async methods so that
server owners don't have to worry about lag (specifically with method calls like Database operations).

All of this and the fact that it has a built-in database framework that uses HikariCP
(the fastest database framework at the moment),
means that developers will have an easy time adding database support for their plugins.

## What does this do?
As stated before,
the main purpose of this plugin is
to provide a central framework for plugins that use it.

On top of that, it also has a few abilities it does by itself:
- Allow server admins to change how many milliseconds there are in between the async ticks the plugin has.
    - This is used for increasing or decreasing the tickrate of the plugin's runnable tasks.
- Item serialization.
    - Serialize and deserialize ItemStacks to and from strings.
- Built-in database framework.
    - Easily add database support to your plugins with HikariCP.
- Enhanced logging.
    - Allows plugin authors to handle logging messages in a more intuitive way.
- Focus on asynchronous, thread-safe, optimized methods.
    - The plugin is designed to be as lag-free as possible.
- Enhanced color support.
    - Easily colorize messages with the plugin's built-in color support. Supports RGB colors with Hex!
- Added utility methods.
    - The plugin has a few utility methods that can be used in your plugin's project.
- Localized PlaceholderAPI support.
    - Easily add PlaceholderAPI support to your plugin with the plugin's built-in PlaceholderAPI support.
- Player lookups without Mojang.
    - Player names, UUIDs, and skins come from your server's data and cache first.
    - Falls back to Geyser, PlayerDB, then MineTools, pausing any service that is down.
    - Offline-mode and no-internet servers are supported (set `web.enabled` to `false`).
- Bedrock support through Floodgate and Geyser.
    - Bedrock players are fully supported, and their player heads show their real skins.
- Slim download.
    - Large libraries are downloaded by the server at startup instead of being bundled in the jar.

![Commands and Permissions](https://github.com/Streamline-Essentials/StreamlineWiki/blob/main/website/images/CommandsAndPermissions.png?raw=true)
## Commands
_`<...>` is required, `(...)` is optional._

* `/entity-count (world)` — aliases: `entityc`, `counte`, `countentities`
    * Get the amount of entities currently loaded on your server, or in one world.
    * Permission: `bou.command.entity-count`
* `/boudebug <action> (args)` — alias: `boud`
    * Permission: `bou.command.boudebug`
    * `item-nbt` / `item-nbt-strict` — Serialize the item in your hand to a string.
    * `make-item <nbt>` / `make-item-strict <nbt>` — Create an item from a serialized string.
    * `store-item` — Store the item in your hand in memory.
    * `get-item <id>` — Get a stored item back from memory.
    * `list-bou-plugins` — List loaded BOU plugins.
    * `dump` — Dump debug information.
    * `worlds` — List all worlds.
    * `uuid <player>` — Show a copy-able message of a player's UUID.
    * `tasks <list|cancel <id>|pause <id>|resume <id>|menu>` — View and manage running BOU tasks.
    * Teleport to:
        * `top` — The top-most open space above you.
        * `up` — The next open space above you.
        * `down` — The next open space below you.
        * `center` — The center of your block.
        * `center3d` — The exact middle of your block.
* `/boumessage <player> <message>` — alias: `boumsg`
    * Send a message to a player with the plugin's color support.
    * Permission: `bou.command.boumessage`
* `/boutitle <player> <title>\n<subtitle> (-fadeIn=<fade in ticks>) (-stay=<stay ticks>) (-fadeOut=<fade out ticks>)` — alias: `bout`
    * Send a title to a player with the plugin's color support.
    * Permission: `bou.command.boutitle`
* `/bouversion` — alias: `bouv`
    * Shows `BukkitOfUtils` versioning information, including the server's version.
    * Checks Modrinth for updates and links the download if you are behind.
* `/bouplugins <list|enable|disable|info|reload|unload|load|menu> (plugin)` — alias: `boup`
    * List and manage loaded BOU plugins, or open the plugins menu.
    * Permission: `bou.plugins`
* `/onemenu` — alias: `menu` (configurable)
    * Opens the configurable OneMenu GUI.
    * Off by default; enable it with `one-menu.enabled` in the config.
    * Permission: `bou.onemenu`
* `/runfirestring <firestring>` — aliases: `rfs`, `firestring`, `fstring`
    * Runs a `FireString`.
    * Permission: `bou.command.runfirestring`
* `/bouhelp <bou-plugin>` — alias: `bouh`
    * Shows `Helpful` information about a `BOU` plugin.
    * Permission: `bou.command.bouhelp`
* `/item-factory <bou-plugin> <key> (count)` — aliases: `itemfactory`, `itf`
    * Gets an item (or several) from the `ItemFactory`.
    * Permission: `bou.command.item-factory`

![More Info](https://github.com/Streamline-Essentials/StreamlineWiki/blob/main/website/images/MoreInfo.png?raw=true)
## How do I get started using this?
Head on over to the plugin's wiki to see how you can use it in your own plugin's project.
Wiki: [https://wiki.plas.host/bukkitofutils](https://wiki.plas.host/bukkitofutils)

# BStats
![BStats Profile](https://bstats.org/signatures/bukkit/BukkitOfUtils.svg)