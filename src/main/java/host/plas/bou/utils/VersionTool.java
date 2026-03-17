package host.plas.bou.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import host.plas.bou.BukkitOfUtils;
import host.plas.bou.commands.CommandHandler;
import host.plas.bou.utils.obj.Versioning;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.checkerframework.checker.units.qual.N;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Utility class providing version-dependent reflection methods, ItemStack serialization/deserialization,
 * command synchronization, and NMS/CraftBukkit class access.
 */
public class VersionTool {
    /** Private constructor to prevent instantiation of this utility class. */
    private VersionTool() {}

    // GSON

    /** A shared Gson instance used for JSON serialization and deserialization. */
    public static final Gson GSON = new GsonBuilder().create();

    // ITEMSTACK SERIALIZATION AND DESERIALIZATION

    /**
     * Serializes a Bukkit ItemStack to a JSON string.
     * Uses NMS reflection when a server version is available, otherwise falls back to Bukkit serialization.
     *
     * @param stack the ItemStack to serialize
     * @return a JSON string representation of the ItemStack
     */
    public static String getJsonStringFromBukkitItemStack(ItemStack stack) {
        if (getServerVersion().isEmpty()) {
            try {
                ConcurrentSkipListMap<String, Object> map = new ConcurrentSkipListMap<>(stack.serialize());
                map.entrySet().removeIf(entry -> entry.getValue() == null);  // Remove null entries

                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    map.put("meta", meta.serialize());
                }

                return GSON.toJson(map);
            } catch (Throwable e) {
                BukkitOfUtils.getInstance().logWarning("Failed to serialize ItemStack to JSON: ", e);
                return "{}";
            }
        }

        try {
            // Get NMS ItemStack
            Object nmsItemStack = VersionTool.getNMSItemStack(stack);

            if (nmsItemStack == null) {
                return "{}";  // Return empty JSON if the item is null or incompatible
            }

            // Get the NBT tag compound using reflection
            Class<?> nmsItemStackClass = nmsItemStack.getClass();
            Method saveMethod = nmsItemStackClass.getMethod("save", VersionTool.getNBTTagCompoundClass());
            Object nbtTagCompound = saveMethod.invoke(nmsItemStack, VersionTool.getNBTTagCompoundClass().newInstance());

            // Convert NBT to JSON using `toString()`
            return nbtTagCompound.toString();
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarning("Failed to serialize ItemStack to JSON: ", e);
            return "{}";
        }
    }

    /**
     * Deserializes a Bukkit ItemStack from a JSON/NBT string.
     * Uses NMS reflection when a server version is available, otherwise falls back to Bukkit deserialization.
     *
     * @param nbtJson the JSON or NBT string to deserialize
     * @return the deserialized ItemStack, or null if deserialization fails
     */
    public static ItemStack getBukkitItemStackFromJsonString(String nbtJson) {
        if (getServerVersion().isEmpty()) {
            try {
                ConcurrentSkipListMap<String, Object> map = new ConcurrentSkipListMap<>((Map<String, ?>) GSON.fromJson(nbtJson, Map.class));
                ItemStack stack = ItemStack.deserialize(map);

                try {
                    ConcurrentSkipListMap<String, Object> metaMap = new ConcurrentSkipListMap<>();
                    if (map.containsKey("meta")) {
                        metaMap = new ConcurrentSkipListMap<>((Map<String, ?>) map.get("meta"));
                        try {
                            ItemMeta meta = GSON.fromJson(GSON.toJson(metaMap), ItemMeta.class);
                            stack.setItemMeta(meta);
                        } catch (Throwable e) {
//                            BukkitOfUtils.getInstance().logWarning("Failed to deserialize ItemStack meta from JSON: ", e);
//                            BukkitOfUtils.getInstance().logWarning("Using fallback method to deserialize ItemStack meta from JSON...");

                            try {
                                ItemMeta meta = deserializeItemMetaFallback(stack, metaMap);
                                stack.setItemMeta(meta);
                            } catch (Throwable e2) {
                                BukkitOfUtils.getInstance().logWarning("Failed to deserialize ItemStack meta from JSON: ", e2);
                            }
                        }
                    }
                } catch (Throwable e) {
                    BukkitOfUtils.getInstance().logWarning("Failed to deserialize ItemStack meta from JSON: ", e);
                }

                return stack;
            } catch (Throwable e) {
                BukkitOfUtils.getInstance().logWarning("Failed to deserialize ItemStack from JSON: ", e);
                return null;
            }
        }

        try {
            // Get an instance of NBTTagCompound from the JSON string
            Object nbtTagCompound = VersionTool.parseNBT(nbtJson);
            if (nbtTagCompound == null) {
                return null;
            }

            // Create an NMS ItemStack from the NBT data
            Object nmsItemStack = VersionTool.getNMSItemStackFromNBT(nbtTagCompound);

            // Convert the NMS ItemStack back to Bukkit's ItemStack
            return VersionTool.getBukkitItemStack(nmsItemStack);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarning("Failed to deserialize ItemStack from JSON: ", e);
            return null;
        }
    }

    // ITEM META FALLBACK

    /**
     * Deserializes ItemMeta from a raw map using a fallback approach when standard deserialization fails.
     * Handles display names, lore, and skull owner data.
     *
     * @param stack   the ItemStack to create meta for
     * @param metaMap the raw meta data map
     * @return the deserialized ItemMeta, or null if the stack type has no meta
     */
    public static ItemMeta deserializeItemMetaFallback(ItemStack stack, Map<String, Object> metaMap) {
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(stack.getType());

        try {
            if (meta == null) return null;

            if (metaMap.containsKey("display-name")) {
                String displayName = (String) metaMap.get("display-name");
                // Replace '\' in display name with ''
                if (displayName != null) {
                    displayName = displayName.replace("\\", "");
                    meta.setDisplayName(squashComponents(deserializeJsonToText(displayName)).toLegacyText());
                }
                List<String> lore = (List<String>) metaMap.get("lore");
                if (lore != null) {
                    List<TextComponent> loreComponents = new ArrayList<>();
                    for (String s : lore) {
                        loreComponents.add(squashComponents(deserializeJsonToText(s)));
                    }

                    List<String> loreStrings = new ArrayList<>();
                    for (TextComponent loreComponent : loreComponents) {
                        loreStrings.add(loreComponent.toLegacyText());
                    }

                    meta.setLore(loreStrings);
                }
            }

            if (meta instanceof SkullMeta) {
                try {
                    SkullMeta skullMeta = (SkullMeta) meta;
                    Map<String, Object> skullOwnerMap = (Map<String, Object>) metaMap.get("skull-owner");
                    if (skullOwnerMap != null) {
                        Map<String, Object> profileMap = (Map<String, Object>) skullOwnerMap.get("profile");
                        if (profileMap != null) {
                            String ownerId = (String) profileMap.get("id");
                            String ownerName = (String) profileMap.get("name");

                            if (ownerId != null) {
                                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(ownerId)));
                            } else if (ownerName != null) {
                                skullMeta.setOwner(ownerName);
                            }
                        }
                    }
                } catch (Throwable e) {
                    BukkitOfUtils.getInstance().logWarning("Failed to deserialize SkullMeta from JSON: ", e);
                }
            }
        } catch (Throwable e2) {
            BukkitOfUtils.getInstance().logWarning("Failed to deserialize ItemStack meta from JSON: ", e2);
        }

        return meta;
    }

    /**
     * Combines an array of BaseComponents into a single TextComponent.
     *
     * @param components the components to combine
     * @return a TextComponent containing all the given components as extras
     */
    public static TextComponent squashComponents(BaseComponent[] components) {
        TextComponent textComponent = new TextComponent();
        for (BaseComponent component : components) {
            textComponent.addExtra(component);
        }
        return textComponent;
    }

    /**
     * Deserializes a JSON string into an array of BaseComponents, handling text, color, and extra fields.
     *
     * @param json the JSON string to deserialize
     * @return an array of BaseComponents representing the deserialized text
     */
    public static BaseComponent[] deserializeJsonToText(String json) {
        Map<String, Object> metaMap2 = (Map<String, Object>) GSON.fromJson(json, Map.class);
        String text = (String) metaMap2.get("text");
        String color = (String) metaMap2.get("color");
        String[] extra = (String[]) metaMap2.get("extra");
        ComponentBuilder builder = new ComponentBuilder(text);
        if (color != null) {
            if (color.startsWith("#")) {
                // color is in hex format
                int colorRed = Integer.parseInt(color.substring(1, 3), 16);
                int colorGreen = Integer.parseInt(color.substring(3, 5), 16);
                int colorBlue = Integer.parseInt(color.substring(5, 7), 16);
                builder.color(ChatColor.of(new Color(colorRed, colorGreen, colorBlue)));
            } else {
                try {
                    builder.color(ChatColor.valueOf(color));
                } catch (Throwable throwable) {
                    // do nothing
                }
            }
        }
        if (extra != null) {
            for (String s : extra) {
                builder.append(deserializeJsonToText(s));
            }
        }

        return builder.create();
    }

    // FAST REFLECTION METHODS

    /**
     * Initializes the VersionTool by introspecting the server version and pre-loading
     * all reflection classes, methods, and fields.
     */
    public static void init() {
        Versioning.introspect(); // Initialize server version

        CommandHandler.init();

        initClasses();
        initMethods();
        initFields();
    }

    /**
     * Pre-loads all NMS and CraftBukkit classes used by reflection.
     */
    public static void initClasses() {
        try {
            getBukkitServerClass();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getPlayerListClass();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getCraftPlayerClass();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getCraftItemStackClass();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getNBTTagCompoundClass();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getMojangsonParserClass();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getNMSItemStackClass();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getFoliaEntityClass();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getFoliaLocationClass();
        } catch (Throwable e) {
            // do nothing
        }
    }

    /**
     * Pre-loads all reflection methods used by this utility.
     */
    public static void initMethods() {
        try {
            getBukkitServerSyncCommandsMethod();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getCraftPlayerGetGameProfileMethod();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getCraftPlayerGetHandleMethod();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getMojangsonParserParseMethod();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getNMSItemStackAMethod();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getCraftItemStackAsBukkitCopyMethod();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getFoliaEntityTeleportAsyncMethod();
        } catch (Throwable e) {
            // do nothing
        }
    }

    /**
     * Pre-loads all reflection fields used by this utility.
     */
    public static void initFields() {
        try {
            getGameProfileNameField();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getPlayerListField();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getPlayersByNameField();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getPlayersMap();
        } catch (Throwable e) {
            // do nothing
        }
        try {
            getCommandMapKnownCommandsField();
        } catch (Throwable e) {
            // do nothing
        }
    }

    /**
     * Returns the NMS (net.minecraft.server) package path.
     *
     * @return the NMS package path without a trailing dot
     */
    public static String getNMSPackage() {
        return getNMSPackage(false);
    }

    /**
     * Returns the NMS (net.minecraft.server) package path with an optional trailing dot.
     *
     * @param withTrailingDot whether to append a trailing dot
     * @return the NMS package path
     */
    public static String getNMSPackage(boolean withTrailingDot) {
        return "net.minecraft.server" + getServerVersionDotted() + (withTrailingDot ? "." : "");
    }

    /**
     * Returns the CraftBukkit package path.
     *
     * @return the CraftBukkit package path without a trailing dot
     */
    public static String getCraftPackage() {
        return getCraftPackage(false);
    }

    /**
     * Returns the CraftBukkit package path with an optional trailing dot.
     *
     * @param withTrailingDot whether to append a trailing dot
     * @return the CraftBukkit package path
     */
    public static String getCraftPackage(boolean withTrailingDot) {
        return "org.bukkit.craftbukkit" + getServerVersionDotted() + (withTrailingDot ? "." : "");
    }

    private static Class<?> CLASS_BUKKIT_SERVER = null;

    /**
     * Returns the Bukkit server implementation class.
     *
     * @return the server class
     */
    public static Class<?> getBukkitServerClass() {
        if (CLASS_BUKKIT_SERVER == null) {
            CLASS_BUKKIT_SERVER = Bukkit.getServer().getClass();
        }
        return CLASS_BUKKIT_SERVER;
    }

    private static Class<?> CLASS_PLAYER_LIST = null;

    /**
     * Returns the NMS PlayerList class.
     *
     * @return the PlayerList class
     * @throws Throwable if the class cannot be found
     */
    public static Class<?> getPlayerListClass() throws Throwable {
        if (CLASS_PLAYER_LIST == null) {
            boolean is17OrOver = Versioning.getServerVersion().isAfter(17 - 1); // 1.17+
            CLASS_PLAYER_LIST = Class.forName((is17OrOver ? getNMSPackage() + ".players" : getNMSPackage()) + ".PlayerList");
        }
        return CLASS_PLAYER_LIST;
    }

    private static Class<?> CLASS_CRAFT_PLAYER = null;

    /**
     * Returns the CraftPlayer class.
     *
     * @return the CraftPlayer class
     * @throws Throwable if the class cannot be found
     */
    public static Class<?> getCraftPlayerClass() throws Throwable {
        if (CLASS_CRAFT_PLAYER == null) {
            CLASS_CRAFT_PLAYER = Class.forName(getCraftPackage() + ".entity.CraftPlayer");
        }
        return CLASS_CRAFT_PLAYER;
    }

    private static Class<?> CLASS_CRAFT_ITEM_STACK = null;

    /**
     * Returns the CraftItemStack class.
     *
     * @return the CraftItemStack class
     * @throws Throwable if the class cannot be found
     */
    public static Class<?> getCraftItemStackClass() throws Throwable {
        if (CLASS_CRAFT_ITEM_STACK == null) {
            CLASS_CRAFT_ITEM_STACK = Class.forName(getCraftPackage() + ".inventory.CraftItemStack");
        }
        return CLASS_CRAFT_ITEM_STACK;
    }

    /**
     * Converts a Bukkit ItemStack to an NMS ItemStack using reflection.
     *
     * @param itemStack the Bukkit ItemStack to convert
     * @return the NMS ItemStack object
     * @throws Throwable if the conversion fails
     */
    public static Object getNMSItemStack(ItemStack itemStack) throws Throwable {
        Class<?> craftItemStackClass = getCraftItemStackClass();
        Method asNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
        return asNMSCopy.invoke(null, itemStack);
    }

    private static Class<?> CLASS_NBT_TAG_COMPOUND = null;

    /**
     * Returns the NMS NBTTagCompound class.
     *
     * @return the NBTTagCompound class
     * @throws Throwable if the class cannot be found
     */
    public static Class<?> getNBTTagCompoundClass() throws Throwable {
        if (CLASS_NBT_TAG_COMPOUND == null) {
            CLASS_NBT_TAG_COMPOUND = Class.forName("net.minecraft.nbt.NBTTagCompound");
        }
        return CLASS_NBT_TAG_COMPOUND;
    }

    private static Class<?> CLASS_MOJANGSON_PARSER = null;

    /**
     * Returns the NMS MojangsonParser class.
     *
     * @return the MojangsonParser class
     * @throws Throwable if the class cannot be found
     */
    public static Class<?> getMojangsonParserClass() throws Throwable {
        if (CLASS_MOJANGSON_PARSER == null) {
            CLASS_MOJANGSON_PARSER = Class.forName("net.minecraft.nbt.MojangsonParser");
        }
        return CLASS_MOJANGSON_PARSER;
    }

    private static Class<?> CLASS_NMS_ITEM_STACK = null;

    /**
     * Returns the NMS ItemStack class.
     *
     * @return the NMS ItemStack class
     * @throws Throwable if the class cannot be found
     */
    public static Class<?> getNMSItemStackClass() throws Throwable {
        if (CLASS_NMS_ITEM_STACK == null) {
            CLASS_NMS_ITEM_STACK = Class.forName("net.minecraft.world.item.ItemStack");
        }
        return CLASS_NMS_ITEM_STACK;
    }

    private static Class<?> CLASS_FOLIA_ENTITY = null;

    /**
     * Returns the Bukkit Entity class (used for Folia teleport compatibility).
     *
     * @return the Entity class
     * @throws Throwable if the class cannot be found
     */
    public static Class<?> getFoliaEntityClass() throws Throwable {
        if (CLASS_FOLIA_ENTITY == null) {
            CLASS_FOLIA_ENTITY = Class.forName("org.bukkit.entity.Entity");
        }
        return CLASS_FOLIA_ENTITY;
    }

    private static Class<?> CLASS_FOLIA_LOCATION = null;

    /**
     * Returns the Bukkit Location class (used for Folia teleport compatibility).
     *
     * @return the Location class
     * @throws Throwable if the class cannot be found
     */
    public static Class<?> getFoliaLocationClass() throws Throwable {
        if (CLASS_FOLIA_LOCATION == null) {
            CLASS_FOLIA_LOCATION = Class.forName("org.bukkit.Location");
        }
        return CLASS_FOLIA_LOCATION;
    }

    private static Method METHOD_BUKKIT_SERVER_SYNC_COMMANDS = null;

    /**
     * Returns the CraftServer syncCommands method.
     *
     * @return the syncCommands Method
     * @throws Throwable if the method cannot be found
     */
    public static Method getBukkitServerSyncCommandsMethod() throws Throwable {
        if (METHOD_BUKKIT_SERVER_SYNC_COMMANDS == null) {
            METHOD_BUKKIT_SERVER_SYNC_COMMANDS = getBukkitServerClass().getDeclaredMethod("syncCommands");
            METHOD_BUKKIT_SERVER_SYNC_COMMANDS.setAccessible(true);
        }
        return METHOD_BUKKIT_SERVER_SYNC_COMMANDS;
    }

    private static Method METHOD_CRAFT_PLAYER_GET_GAME_PROFILE = null;

    /**
     * Returns the CraftPlayer getProfile method for retrieving the GameProfile.
     *
     * @return the getProfile Method
     * @throws Throwable if the method cannot be found
     */
    public static Method getCraftPlayerGetGameProfileMethod() throws Throwable {
        if (METHOD_CRAFT_PLAYER_GET_GAME_PROFILE == null) {
            METHOD_CRAFT_PLAYER_GET_GAME_PROFILE = getCraftPlayerClass().getMethod("getProfile");
        }
        return METHOD_CRAFT_PLAYER_GET_GAME_PROFILE;
    }

    private static Method METHOD_CRAFT_PLAYER_GET_HANDLE = null;

    /**
     * Returns the CraftPlayer getHandle method for retrieving the NMS EntityPlayer.
     *
     * @return the getHandle Method
     * @throws Throwable if the method cannot be found
     */
    public static Method getCraftPlayerGetHandleMethod() throws Throwable {
        if (METHOD_CRAFT_PLAYER_GET_HANDLE == null) {
            METHOD_CRAFT_PLAYER_GET_HANDLE = getCraftPlayerClass().getMethod("getHandle");
        }
        return METHOD_CRAFT_PLAYER_GET_HANDLE;
    }

    private static Method MOJANGSON_PARSER_PARSE_METHOD = null;

    /**
     * Returns the MojangsonParser parse method for parsing NBT JSON strings.
     *
     * @return the parse Method
     * @throws Throwable if the method cannot be found
     */
    public static Method getMojangsonParserParseMethod() throws Throwable {
        if (MOJANGSON_PARSER_PARSE_METHOD == null) {
            MOJANGSON_PARSER_PARSE_METHOD = getMojangsonParserClass().getMethod("parse", String.class);
        }
        return MOJANGSON_PARSER_PARSE_METHOD;
    }

    private static Method NMS_ITEM_STACK_A_METHOD = null;

    /**
     * Returns the NMS ItemStack factory method for creating ItemStacks from NBT data.
     *
     * @return the factory Method
     * @throws Throwable if the method cannot be found
     */
    public static Method getNMSItemStackAMethod() throws Throwable {
        if (NMS_ITEM_STACK_A_METHOD == null) {
            NMS_ITEM_STACK_A_METHOD = getNMSItemStackClass().getMethod("a", getNBTTagCompoundClass());
        }
        return NMS_ITEM_STACK_A_METHOD;
    }

    private static Method CRAFT_ITEM_STACK_AS_BUKKIT_COPY_METHOD = null;

    /**
     * Returns the CraftItemStack asBukkitCopy method for converting NMS ItemStacks to Bukkit ItemStacks.
     *
     * @return the asBukkitCopy Method
     * @throws Throwable if the method cannot be found
     */
    public static Method getCraftItemStackAsBukkitCopyMethod() throws Throwable {
        if (CRAFT_ITEM_STACK_AS_BUKKIT_COPY_METHOD == null) {
            CRAFT_ITEM_STACK_AS_BUKKIT_COPY_METHOD = getCraftItemStackClass().getMethod("asBukkitCopy", getNMSItemStackClass());
        }
        return CRAFT_ITEM_STACK_AS_BUKKIT_COPY_METHOD;
    }

    private static Method FOLIA_ENTITY_TELEPORT_ASYNC_METHOD = null;

    /**
     * Returns the Entity teleportAsync method used for Folia-compatible teleportation.
     *
     * @return the teleportAsync Method
     * @throws Throwable if the method cannot be found
     */
    public static Method getFoliaEntityTeleportAsyncMethod() throws Throwable {
        if (FOLIA_ENTITY_TELEPORT_ASYNC_METHOD == null) {
            FOLIA_ENTITY_TELEPORT_ASYNC_METHOD = getFoliaEntityClass().getMethod("teleportAsync", getFoliaLocationClass());
        }
        return FOLIA_ENTITY_TELEPORT_ASYNC_METHOD;
    }

    private static Field FIELD_GAME_PROFILE_NAME = null;

    /**
     * Returns the GameProfile name field for modifying player profile names via reflection.
     *
     * @return the name Field
     * @throws Throwable if the field cannot be found
     */
    public static Field getGameProfileNameField() throws Throwable {
        if (FIELD_GAME_PROFILE_NAME == null) {
            FIELD_GAME_PROFILE_NAME = GameProfile.class.getDeclaredField("name");
            FIELD_GAME_PROFILE_NAME.setAccessible(true);
        }
        return FIELD_GAME_PROFILE_NAME;
    }

    private static Field FIELD_PLAYER_LIST = null;

    /**
     * Returns the CraftServer playerList field.
     *
     * @return the playerList Field
     * @throws Throwable if the field cannot be found
     */
    public static Field getPlayerListField() throws Throwable {
        if (FIELD_PLAYER_LIST == null) {
            FIELD_PLAYER_LIST = Bukkit.getServer().getClass().getDeclaredField("playerList");
            FIELD_PLAYER_LIST.setAccessible(true);
        }
        return FIELD_PLAYER_LIST;
    }

    private static Field FIELD_PLAYERS_BY_NAME = null;

    /**
     * Returns the PlayerList playersByName field for accessing the player-by-name lookup map.
     *
     * @return the playersByName Field
     * @throws Throwable if the field cannot be found
     */
    public static Field getPlayersByNameField() throws Throwable {
        if (FIELD_PLAYERS_BY_NAME == null) {
            final Object playerList = getPlayerListField().get(Bukkit.getServer());

            FIELD_PLAYERS_BY_NAME = getPlayerListClass().getDeclaredField("playersByName");
            FIELD_PLAYERS_BY_NAME.setAccessible(true);
        }
        return FIELD_PLAYERS_BY_NAME;
    }

    private static Map MAP_PLAYERS = null;

    /**
     * Returns the internal players-by-name map from the server's PlayerList.
     *
     * @return the players map
     * @throws Throwable if the map cannot be accessed
     */
    public static Map getPlayersMap() throws Throwable {
        if (MAP_PLAYERS == null) {
            final Object playerList = getPlayerListField().get(Bukkit.getServer());

            MAP_PLAYERS = (Map) getPlayersByNameField().get(playerList);
        }
        return MAP_PLAYERS;
    }

    private static Field FIELD_COMMAND_MAP_KNOWN_COMMANDS = null;

    /**
     * Returns the knownCommands field from the server's command map.
     *
     * @return the knownCommands Field
     * @throws Throwable if the field cannot be found
     */
    public static Field getCommandMapKnownCommandsField() throws Throwable {
        if (FIELD_COMMAND_MAP_KNOWN_COMMANDS == null) {
            FIELD_COMMAND_MAP_KNOWN_COMMANDS = CommandHandler.getCommandMap().getClass().getDeclaredField("knownCommands");
            FIELD_COMMAND_MAP_KNOWN_COMMANDS.setAccessible(true);
        }
        return FIELD_COMMAND_MAP_KNOWN_COMMANDS;
    }

    // WORKER METHODS

    /**
     * Invokes the server's syncCommands method to synchronize registered commands with clients.
     */
    public static void syncCommands() {
        try {
            getBukkitServerSyncCommandsMethod().invoke(Bukkit.getServer());
        } catch (NoSuchMethodException e) {
            BukkitOfUtils.getInstance().logWarningWithInfo("syncCommands method not found: ", e);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarningWithInfo("Failed to invoke syncCommands method: ", e);
        }
    }

    /**
     * Unregisters a command and its aliases from the server's known commands map.
     *
     * @param command the command to unregister
     */
    public static void unregisterKnownCommand(Command command) {
        try {
            @SuppressWarnings("unchecked") final Map<String, Command> knownCommands = (Map<String, Command>) getCommandMapKnownCommandsField().get(CommandHandler.getCommandMap());

            knownCommands.remove(command.getName());
            for (String alias : command.getAliases()) {
                knownCommands.remove(alias);
            }
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarningWithInfo("Failed to unregister command: ", e);
        }
    }

    /**
     * Parses an NBT JSON string into an NMS NBTTagCompound object.
     *
     * @param nbtJson the NBT JSON string to parse
     * @return the parsed NBTTagCompound object
     * @throws Throwable if parsing fails
     */
    public static Object parseNBT(String nbtJson) throws Throwable {
        return getMojangsonParserParseMethod().invoke(null, nbtJson);
    }

    /**
     * Creates an NMS ItemStack from an NBTTagCompound object.
     *
     * @param nbtTagCompound the NBT data to create the ItemStack from
     * @return the NMS ItemStack object
     * @throws Throwable if creation fails
     */
    public static Object getNMSItemStackFromNBT(Object nbtTagCompound) throws Throwable {
        return getNMSItemStackAMethod().invoke(null, nbtTagCompound);
    }

    /**
     * Converts an NMS ItemStack to a Bukkit ItemStack.
     *
     * @param nmsItemStack the NMS ItemStack to convert
     * @return the Bukkit ItemStack
     * @throws Throwable if conversion fails
     */
    public static ItemStack getBukkitItemStack(Object nmsItemStack) throws Throwable {
        return (ItemStack) getCraftItemStackAsBukkitCopyMethod().invoke(null, nmsItemStack);
    }

    /**
     * Teleports an entity asynchronously using Folia's teleportAsync method.
     *
     * @param entity   the entity to teleport
     * @param location the destination location
     */
    public static void teleportAsync(Entity entity, Location location) {
        try {
            getFoliaEntityTeleportAsyncMethod().invoke(entity, location);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarning("Failed to teleport entity asynchronously: ", e);
        }
    }

    // SERVER VERSION

    /**
     * Returns the server version string with a leading dot.
     *
     * @return the dotted server version string
     */
    public static String getServerVersionDotted() {
        return getServerVersionDotted(true);
    }

    private static String SERVER_VERSION = null;

    /**
     * Returns the server version string with a dot on the specified side.
     *
     * @param before if true, the dot is placed before the version; if false, after
     * @return the dotted server version string, or empty if no version is available
     */
    public static String getServerVersionDotted(boolean before) {
        if (SERVER_VERSION == null) {
            String r;
            if (before) {
                r = "." + getServerVersion();
            } else {
                r = getServerVersion() + ".";
            }

            if (r.equals(".")) {
                r = "";
            }

            SERVER_VERSION = r;
        }

        return SERVER_VERSION;
    }

    /**
     * Extracts the server version string from the CraftBukkit package name.
     *
     * @return the server version string (e.g., "v1_20_R1"), or empty if not available
     */
    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();

        String r;

        try {
            r = packageName.substring("org.bukkit.craftbukkit.".length());
            if (r.contains(".")) {
                r = r.substring(0, r.indexOf("."));
            }
            if (r.isBlank() || r.isEmpty()) {
                r = getNewServerVersion();
            }
        } catch (Throwable e) {
            r = getNewServerVersion();
        }

        return r;
    }

    /**
     * Returns an empty string as a fallback for modern servers that do not include
     * a version string in the CraftBukkit package name.
     *
     * @return an empty string
     */
    public static String getNewServerVersion() {
        return "";
    }

//    public static Versioning getVersion() {
//        String version = getServerVersion();
//        if (version.isEmpty()) {
//            return Versioning.getEmpty();
//        }
//
//        String[] parts = version.split("_");
//        if (parts.length < 3) {
//            return Versioning.getModern();
//        }
//
//        String first = parts[0];
//        String second = parts[1];
//        String third = parts[2];
//
//        first = first.replaceAll("[^0-9]", "");
//        second = second.replaceAll("[^0-9]", "");
//        third = third.replaceAll("[^0-9]", "");
//
//        int firstInt = 0;
//        int secondInt = 0;
//        int thirdInt = 0;
//
//        try {
//            firstInt = Integer.parseInt(first);
//        } catch (Throwable e) {
//            return Versioning.getEmpty();
//        }
//        try {
//            secondInt = Integer.parseInt(second);
//        } catch (Throwable e) {
//            return Versioning.getEmpty();
//        }
//        try {
//            thirdInt = Integer.parseInt(third);
////            thirdInt += 1; // Due to the "R" being 0-based.
//        } catch (Throwable e) {
//            return Versioning.getEmpty();
//        }
//
//        return new Versioning(firstInt, secondInt, thirdInt);
//    }
}