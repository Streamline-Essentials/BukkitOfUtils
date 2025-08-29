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

public class VersionTool {

    // GSON

    public static final Gson GSON = new GsonBuilder().create();

    // ITEMSTACK SERIALIZATION AND DESERIALIZATION

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

    public static TextComponent squashComponents(BaseComponent[] components) {
        TextComponent textComponent = new TextComponent();
        for (BaseComponent component : components) {
            textComponent.addExtra(component);
        }
        return textComponent;
    }

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

    public static void init() {
        Versioning.introspect(); // Initialize server version

        CommandHandler.init();

        initClasses();
        initMethods();
        initFields();
    }

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

    public static String getNMSPackage() {
        return getNMSPackage(false);
    }

    public static String getNMSPackage(boolean withTrailingDot) {
        return "net.minecraft.server" + getServerVersionDotted() + (withTrailingDot ? "." : "");
    }

    public static String getCraftPackage() {
        return getCraftPackage(false);
    }

    public static String getCraftPackage(boolean withTrailingDot) {
        return "org.bukkit.craftbukkit" + getServerVersionDotted() + (withTrailingDot ? "." : "");
    }

    private static Class<?> CLASS_BUKKIT_SERVER = null;

    public static Class<?> getBukkitServerClass() {
        if (CLASS_BUKKIT_SERVER == null) {
            CLASS_BUKKIT_SERVER = Bukkit.getServer().getClass();
        }
        return CLASS_BUKKIT_SERVER;
    }

    private static Class<?> CLASS_PLAYER_LIST = null;

    public static Class<?> getPlayerListClass() throws Throwable {
        if (CLASS_PLAYER_LIST == null) {
            boolean is17OrOver = Versioning.getServerVersion().isAfter(17 - 1); // 1.17+
            CLASS_PLAYER_LIST = Class.forName((is17OrOver ? getNMSPackage() + ".players" : getNMSPackage()) + ".PlayerList");
        }
        return CLASS_PLAYER_LIST;
    }

    private static Class<?> CLASS_CRAFT_PLAYER = null;

    public static Class<?> getCraftPlayerClass() throws Throwable {
        if (CLASS_CRAFT_PLAYER == null) {
            CLASS_CRAFT_PLAYER = Class.forName(getCraftPackage() + ".entity.CraftPlayer");
        }
        return CLASS_CRAFT_PLAYER;
    }

    private static Class<?> CLASS_CRAFT_ITEM_STACK = null;

    public static Class<?> getCraftItemStackClass() throws Throwable {
        if (CLASS_CRAFT_ITEM_STACK == null) {
            CLASS_CRAFT_ITEM_STACK = Class.forName(getCraftPackage() + ".inventory.CraftItemStack");
        }
        return CLASS_CRAFT_ITEM_STACK;
    }

    public static Object getNMSItemStack(ItemStack itemStack) throws Throwable {
        Class<?> craftItemStackClass = getCraftItemStackClass();
        Method asNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
        return asNMSCopy.invoke(null, itemStack);
    }

    private static Class<?> CLASS_NBT_TAG_COMPOUND = null;

    public static Class<?> getNBTTagCompoundClass() throws Throwable {
        if (CLASS_NBT_TAG_COMPOUND == null) {
            CLASS_NBT_TAG_COMPOUND = Class.forName("net.minecraft.nbt.NBTTagCompound");
        }
        return CLASS_NBT_TAG_COMPOUND;
    }

    private static Class<?> CLASS_MOJANGSON_PARSER = null;

    public static Class<?> getMojangsonParserClass() throws Throwable {
        if (CLASS_MOJANGSON_PARSER == null) {
            CLASS_MOJANGSON_PARSER = Class.forName("net.minecraft.nbt.MojangsonParser");
        }
        return CLASS_MOJANGSON_PARSER;
    }

    private static Class<?> CLASS_NMS_ITEM_STACK = null;

    public static Class<?> getNMSItemStackClass() throws Throwable {
        if (CLASS_NMS_ITEM_STACK == null) {
            CLASS_NMS_ITEM_STACK = Class.forName("net.minecraft.world.item.ItemStack");
        }
        return CLASS_NMS_ITEM_STACK;
    }

    private static Class<?> CLASS_FOLIA_ENTITY = null;

    public static Class<?> getFoliaEntityClass() throws Throwable {
        if (CLASS_FOLIA_ENTITY == null) {
            CLASS_FOLIA_ENTITY = Class.forName("org.bukkit.entity.Entity");
        }
        return CLASS_FOLIA_ENTITY;
    }

    private static Class<?> CLASS_FOLIA_LOCATION = null;

    public static Class<?> getFoliaLocationClass() throws Throwable {
        if (CLASS_FOLIA_LOCATION == null) {
            CLASS_FOLIA_LOCATION = Class.forName("org.bukkit.Location");
        }
        return CLASS_FOLIA_LOCATION;
    }

    private static Method METHOD_BUKKIT_SERVER_SYNC_COMMANDS = null;

    public static Method getBukkitServerSyncCommandsMethod() throws Throwable {
        if (METHOD_BUKKIT_SERVER_SYNC_COMMANDS == null) {
            METHOD_BUKKIT_SERVER_SYNC_COMMANDS = getBukkitServerClass().getDeclaredMethod("syncCommands");
            METHOD_BUKKIT_SERVER_SYNC_COMMANDS.setAccessible(true);
        }
        return METHOD_BUKKIT_SERVER_SYNC_COMMANDS;
    }

    private static Method METHOD_CRAFT_PLAYER_GET_GAME_PROFILE = null;

    public static Method getCraftPlayerGetGameProfileMethod() throws Throwable {
        if (METHOD_CRAFT_PLAYER_GET_GAME_PROFILE == null) {
            METHOD_CRAFT_PLAYER_GET_GAME_PROFILE = getCraftPlayerClass().getMethod("getProfile");
        }
        return METHOD_CRAFT_PLAYER_GET_GAME_PROFILE;
    }

    private static Method METHOD_CRAFT_PLAYER_GET_HANDLE = null;

    public static Method getCraftPlayerGetHandleMethod() throws Throwable {
        if (METHOD_CRAFT_PLAYER_GET_HANDLE == null) {
            METHOD_CRAFT_PLAYER_GET_HANDLE = getCraftPlayerClass().getMethod("getHandle");
        }
        return METHOD_CRAFT_PLAYER_GET_HANDLE;
    }

    private static Method MOJANGSON_PARSER_PARSE_METHOD = null;

    public static Method getMojangsonParserParseMethod() throws Throwable {
        if (MOJANGSON_PARSER_PARSE_METHOD == null) {
            MOJANGSON_PARSER_PARSE_METHOD = getMojangsonParserClass().getMethod("parse", String.class);
        }
        return MOJANGSON_PARSER_PARSE_METHOD;
    }

    private static Method NMS_ITEM_STACK_A_METHOD = null;

    public static Method getNMSItemStackAMethod() throws Throwable {
        if (NMS_ITEM_STACK_A_METHOD == null) {
            NMS_ITEM_STACK_A_METHOD = getNMSItemStackClass().getMethod("a", getNBTTagCompoundClass());
        }
        return NMS_ITEM_STACK_A_METHOD;
    }

    private static Method CRAFT_ITEM_STACK_AS_BUKKIT_COPY_METHOD = null;

    public static Method getCraftItemStackAsBukkitCopyMethod() throws Throwable {
        if (CRAFT_ITEM_STACK_AS_BUKKIT_COPY_METHOD == null) {
            CRAFT_ITEM_STACK_AS_BUKKIT_COPY_METHOD = getCraftItemStackClass().getMethod("asBukkitCopy", getNMSItemStackClass());
        }
        return CRAFT_ITEM_STACK_AS_BUKKIT_COPY_METHOD;
    }

    private static Method FOLIA_ENTITY_TELEPORT_ASYNC_METHOD = null;

    public static Method getFoliaEntityTeleportAsyncMethod() throws Throwable {
        if (FOLIA_ENTITY_TELEPORT_ASYNC_METHOD == null) {
            FOLIA_ENTITY_TELEPORT_ASYNC_METHOD = getFoliaEntityClass().getMethod("teleportAsync", getFoliaLocationClass());
        }
        return FOLIA_ENTITY_TELEPORT_ASYNC_METHOD;
    }

    private static Field FIELD_GAME_PROFILE_NAME = null;

    public static Field getGameProfileNameField() throws Throwable {
        if (FIELD_GAME_PROFILE_NAME == null) {
            FIELD_GAME_PROFILE_NAME = GameProfile.class.getDeclaredField("name");
            FIELD_GAME_PROFILE_NAME.setAccessible(true);
        }
        return FIELD_GAME_PROFILE_NAME;
    }

    private static Field FIELD_PLAYER_LIST = null;

    public static Field getPlayerListField() throws Throwable {
        if (FIELD_PLAYER_LIST == null) {
            FIELD_PLAYER_LIST = Bukkit.getServer().getClass().getDeclaredField("playerList");
            FIELD_PLAYER_LIST.setAccessible(true);
        }
        return FIELD_PLAYER_LIST;
    }

    private static Field FIELD_PLAYERS_BY_NAME = null;

    public static Field getPlayersByNameField() throws Throwable {
        if (FIELD_PLAYERS_BY_NAME == null) {
            final Object playerList = getPlayerListField().get(Bukkit.getServer());

            FIELD_PLAYERS_BY_NAME = getPlayerListClass().getDeclaredField("playersByName");
            FIELD_PLAYERS_BY_NAME.setAccessible(true);
        }
        return FIELD_PLAYERS_BY_NAME;
    }

    private static Map MAP_PLAYERS = null;

    public static Map getPlayersMap() throws Throwable {
        if (MAP_PLAYERS == null) {
            final Object playerList = getPlayerListField().get(Bukkit.getServer());

            MAP_PLAYERS = (Map) getPlayersByNameField().get(playerList);
        }
        return MAP_PLAYERS;
    }

    private static Field FIELD_COMMAND_MAP_KNOWN_COMMANDS = null;

    public static Field getCommandMapKnownCommandsField() throws Throwable {
        if (FIELD_COMMAND_MAP_KNOWN_COMMANDS == null) {
            FIELD_COMMAND_MAP_KNOWN_COMMANDS = CommandHandler.getCommandMap().getClass().getDeclaredField("knownCommands");
            FIELD_COMMAND_MAP_KNOWN_COMMANDS.setAccessible(true);
        }
        return FIELD_COMMAND_MAP_KNOWN_COMMANDS;
    }

    // WORKER METHODS

    public static void syncCommands() {
        try {
            getBukkitServerSyncCommandsMethod().invoke(Bukkit.getServer());
        } catch (NoSuchMethodException e) {
            BukkitOfUtils.getInstance().logWarningWithInfo("syncCommands method not found: ", e);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarningWithInfo("Failed to invoke syncCommands method: ", e);
        }
    }

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

    public static Object parseNBT(String nbtJson) throws Throwable {
        return getMojangsonParserParseMethod().invoke(null, nbtJson);
    }

    public static Object getNMSItemStackFromNBT(Object nbtTagCompound) throws Throwable {
        return getNMSItemStackAMethod().invoke(null, nbtTagCompound);
    }

    public static ItemStack getBukkitItemStack(Object nmsItemStack) throws Throwable {
        return (ItemStack) getCraftItemStackAsBukkitCopyMethod().invoke(null, nmsItemStack);
    }

    public static void teleportAsync(Entity entity, Location location) {
        try {
            getFoliaEntityTeleportAsyncMethod().invoke(entity, location);
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarning("Failed to teleport entity asynchronously: ", e);
        }
    }

    // SERVER VERSION

    public static String getServerVersionDotted() {
        return getServerVersionDotted(true);
    }

    private static String SERVER_VERSION = null;

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

    public static String getNewServerVersion() {
        return "";
    }

    public static Versioning getVersion() {
        String version = getServerVersion();
        if (version.isEmpty()) {
            return Versioning.getEmpty();
        }

        String[] parts = version.split("_");
        if (parts.length < 3) {
            return Versioning.getModern();
        }

        String first = parts[0];
        String second = parts[1];
        String third = parts[2];

        first = first.replaceAll("[^0-9]", "");
        second = second.replaceAll("[^0-9]", "");
        third = third.replaceAll("[^0-9]", "");

        int firstInt = 0;
        int secondInt = 0;
        int thirdInt = 0;

        try {
            firstInt = Integer.parseInt(first);
        } catch (Throwable e) {
            return Versioning.getEmpty();
        }
        try {
            secondInt = Integer.parseInt(second);
        } catch (Throwable e) {
            return Versioning.getEmpty();
        }
        try {
            thirdInt = Integer.parseInt(third);
//            thirdInt += 1; // Due to the "R" being 0-based.
        } catch (Throwable e) {
            return Versioning.getEmpty();
        }

        return new Versioning(firstInt, secondInt, thirdInt);
    }
}