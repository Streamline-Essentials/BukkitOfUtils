package host.plas.bou.utils;

import host.plas.bou.BukkitOfUtils;
import host.plas.bou.scheduling.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import gg.drak.thebase.lib.re2j.Matcher;
import gg.drak.thebase.utils.MatcherUtils;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Utility class for location parsing, serialization, top-block searching,
 * teleportation, and item dropping.
 */
public class LocationUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private LocationUtils() {
        // utility class
    }

    /**
     * Parses a Location from a comma-separated string representation.
     *
     * @param location     the string to parse (format: "world,x,y,z" or "world,x,y,z,yaw,pitch")
     * @param withYawPitch whether to expect and parse yaw and pitch values
     * @return an Optional containing the parsed Location, or empty if parsing fails
     */
    public static Optional<Location> fromString(String location, boolean withYawPitch) {
        try {
            String regex;
            int groupCount;
            if (withYawPitch) {
                // world,x,y,z,yaw,pitch
                regex = "([a-zA-Z0-9_]+),(-?\\d+\\.?\\d*),(-?\\d+\\.?\\d*),(-?\\d+\\.?\\d*),(-?\\d+\\.?\\d*),(-?\\d+\\.?\\d*)";
                groupCount = 6;
            } else {
                // world,x,y,z
                regex = "([a-zA-Z0-9_]+),(-?\\d+\\.?\\d*),(-?\\d+\\.?\\d*),(-?\\d+\\.?\\d*)";
                groupCount = 4;
            }
            Matcher matcher = MatcherUtils.matcherBuilder(regex, location);
            List<String[]> groups = MatcherUtils.getGroups(matcher, groupCount);
            if (groups.isEmpty()) {
                return Optional.empty();
            }
            for (String[] group : groups) {
                String world = group[0];
                double x = Double.parseDouble(group[1]);
                double y = Double.parseDouble(group[2]);
                double z = Double.parseDouble(group[3]);

                World bukkitWorld = Bukkit.getWorld(world);
                if (bukkitWorld == null) {
                    return Optional.empty();
                }

                if (withYawPitch && group.length == 6) {
                    float yaw = Float.parseFloat(group[4]);
                    float pitch = Float.parseFloat(group[5]);
                    return Optional.of(new Location(bukkitWorld, x, y, z, yaw, pitch));
                }
                return Optional.of(new Location(bukkitWorld, x, y, z));
            }

            return Optional.empty();
        } catch (Throwable e) {
            BukkitOfUtils.getInstance().logWarning("Failed to parse location: " + location, e);
            return Optional.empty();
        }
    }

    /**
     * Parses a Location from a comma-separated string without yaw and pitch.
     *
     * @param location the string to parse (format: "world,x,y,z")
     * @return an Optional containing the parsed Location, or empty if parsing fails
     */
    public static Optional<Location> fromString(String location) {
        return fromString(location, false);
    }

    /**
     * Converts a Location to a comma-separated string representation.
     *
     * @param location     the location to serialize
     * @param withYawPitch whether to include yaw and pitch in the output
     * @return the string representation of the location
     */
    public static String toString(Location location, boolean withYawPitch) {
        if (location == null) return "world,0,0,0";
        if (location.getWorld() == null) return "world,0,0,0";
        if (withYawPitch) {
            return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
        }
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
    }

    /**
     * Converts a Location to a comma-separated string representation without yaw and pitch.
     *
     * @param location the location to serialize
     * @return the string representation of the location
     */
    public static String toString(Location location) {
        return toString(location, false);
    }

    /**
     * Returns a new location centered on the block (adds 0.5 to X and Z coordinates).
     *
     * @param location the location to center
     * @return a new centered location
     */
    public static Location getCenteredLocation(Location location) {
        return new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5);
    }

    /**
     * Finds a safe top location at the given coordinates, searching down first then up.
     *
     * @param location the starting location
     * @return a centered safe top location
     */
    public static Location getTopLocation(Location location) {
        return getTopLocation(location, true);
    }

    /**
     * Finds a safe top location at the given coordinates.
     *
     * @param location   the starting location
     * @param downThenUp if true, searches downward first then upward; otherwise searches upward only
     * @return a centered safe top location
     */
    public static Location getTopLocation(Location location, boolean downThenUp) {
        Location top = location.clone();
        if (downThenUp) {
            top = searchForTopBlock(location, BlockFace.DOWN, true, false);
            if (top == null) {
                top = searchForTopBlock(location, BlockFace.UP);
            }
        } else {
            top = searchForTopBlock(location, BlockFace.UP);
        }
        if (top == null) {
            top = getHighestNonAirBlock(location);
            top.add(0, 1, 0); // Add one to get the top of the block (world). This is to prevent the player from being stuck in the block.
        }

        return getCenteredLocation(top);
    }

    /**
     * Searches for a top block in the specified direction from the given location.
     *
     * @param location  the starting location
     * @param direction the direction to search (UP or DOWN)
     * @return the location of the found top block, or null if not found
     */
    public static Location searchForTopBlock(Location location, BlockFace direction) {
        return searchForTopBlock(location, direction, false, false);
    }

    /**
     * Searches for a top block in the specified direction with an optional centered result.
     *
     * @param location  the starting location
     * @param direction the direction to search (UP or DOWN)
     * @param centered  whether to return a centered location
     * @return the location of the found top block, or null if not found
     */
    public static Location searchForTopBlock(Location location, BlockFace direction, boolean centered) {
        return searchForTopBlock(location, direction, false, centered);
    }

    /**
     * Checks whether the block at the given location is air-like (air or passable non-solid).
     *
     * @param location the location to check
     * @return true if the block is air-like
     */
    private static boolean isAirLike(Location location) {
        return isAirLike(location.getBlock());
    }

    /**
     * Checks whether the given block is air-like (air or passable non-solid).
     *
     * @param block the block to check
     * @return true if the block is air-like
     */
    private static boolean isAirLike(Block block) {
        return block.getType() == Material.AIR ||
                block.isPassable() && ! block.getType().isSolid(); // This covers flowers, tall grass, etc.
    }

    /**
     * Searches for a safe top block in the specified direction with configurable behavior.
     *
     * @param location     the starting location
     * @param direction    the direction to search (UP or DOWN)
     * @param stopAtNonAir whether to stop when a non-air block is encountered
     * @param centered     whether to return a centered location
     * @return the location of the found top block, or null if outside world bounds
     */
    public static Location searchForTopBlock(Location location, BlockFace direction, boolean stopAtNonAir, boolean centered) {
        Location top = location.clone();
        Block block = top.getBlock();
        World world = top.getWorld();
        if (world == null) return null; // not going to be null, but just in case
        while (! checkForTopableBlock(block)) {
            if (block.getY() < world.getMinHeight() && block.getY() > world.getMaxHeight()) break;

            if (stopAtNonAir) {
                if (block.getType() != Material.AIR) {
                    block = block.getRelative(direction.getOppositeFace());
                    break;
                }
            }

            block = top.getBlock().getRelative(direction);
            top = block.getLocation();
        }

        if (block.getY() < world.getMinHeight()) {
            return null;
        }
        if (block.getY() > world.getMaxHeight()) {
            return null;
        }

        if (centered) return getCenteredLocation(top);
        return top;
    }

    /**
     * Finds the topmost safe location starting from the highest block at the given coordinates.
     *
     * @param location the location to search at (X and Z are used)
     * @return a centered location at the topmost safe position
     */
    public static Location getTopMostTopLocation(Location location) {
        return getTopMostTopLocation(location, true);
    }

    /**
     * Finds the topmost safe location starting from the highest block at the given coordinates.
     *
     * @param location the location to search at (X and Z are used)
     * @param centered whether to center the result on the block
     * @return a centered location at the topmost safe position
     */
    public static Location getTopMostTopLocation(Location location, boolean centered) {
        return getCenteredLocation(getTopMostTopBlock(location).getLocation());
    }

    /**
     * Gets the topmost safe block at the given location's X and Z coordinates.
     *
     * @param location the location to search at
     * @return the topmost safe block, or null if the location or world is null
     */
    public static Block getTopMostTopBlock(Location location) {
        if (location == null) return null;
        if (location.getWorld() == null) return null;

        World world = location.getWorld();
        Block block = world.getHighestBlockAt(location.getBlockX(), location.getBlockZ());

        Location top = block.getLocation();
        Location loc = searchForTopBlock(top, BlockFace.DOWN, true, false);
        if (loc != null) {
            return loc.getBlock();
        }

        return block;
    }

    /**
     * Checks whether the block at the given location is safe for an entity to stand on
     * (the block and the block above it are both air-like).
     *
     * @param location the location to check
     * @return true if the location is safe for standing
     */
    public static boolean checkForTopableBlock(Location location) {
        return checkForTopableBlock(location.getBlock());
    }

    /**
     * Checks whether the given block is safe for an entity to stand on
     * (the block and the block above it are both air-like).
     *
     * @param block the block to check
     * @return true if the block is safe for standing
     */
    // Ensure to check for highest block after using this.
    public static boolean checkForTopableBlock(Block block) {
        return (isAirLike(block) && isAirLike(block.getRelative(BlockFace.UP)));
    }

    /**
     * Gets the highest non-air block location at the given X and Z coordinates.
     *
     * @param location the location providing the X, Z, and world
     * @return the location of the highest non-air block, or null if the location or world is null
     */
    public static Location getHighestNonAirBlock(Location location) {
        if (location == null) return null;
        if (location.getWorld() == null) return null;
        return new Location(location.getWorld(), location.getBlockX(), location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()), location.getBlockZ());
    }

    /**
     * Teleports an entity to the specified location using the task manager.
     *
     * @param entity   the entity to teleport
     * @param location the destination location
     */
    public static void teleport(Entity entity, Location location) {
        TaskManager.teleport(entity, location);
    }

    /**
     * Generates a random direction vector with optional normalization and a multiplier.
     *
     * @param normalize  whether to normalize the vector to unit length before applying the multiplier
     * @param multiplier the scalar multiplier to apply to the direction
     * @return a random direction vector
     */
    public static Vector getRandomDirection(boolean normalize, double multiplier) {
        Random RNG = new Random();
        double x = RNG.nextDouble();
        double y = RNG.nextDouble();
        double z = RNG.nextDouble();

        Vector direction = new Vector(x, y, z);
        if (normalize) direction = direction.normalize();
        if (multiplier != 1) direction = direction.multiply(multiplier);
        return direction;
    }

    /**
     * Generates a random direction vector with the specified multiplier.
     *
     * @param multiplier the scalar multiplier to apply to the direction
     * @return a random direction vector
     */
    public static Vector getRandomDirection(double multiplier) {
        return getRandomDirection(false, multiplier);
    }

    /**
     * Generates a random direction vector with optional normalization.
     *
     * @param normalize whether to normalize the vector to unit length
     * @return a random direction vector
     */
    public static Vector getRandomDirection(boolean normalize) {
        return getRandomDirection(normalize, 1);
    }

    /**
     * Generates a random direction vector with default settings.
     *
     * @return a random direction vector
     */
    public static Vector getRandomDirection() {
        return getRandomDirection(1);
    }

    /**
     * Drops items at a location with a specified velocity direction.
     *
     * @param location  the location to drop items at
     * @param items     a map of item stacks to drop
     * @param direction the velocity to apply to the dropped items
     * @return a map of the dropped item entities
     */
    public static ConcurrentSkipListMap<Integer, Item> dropItemsWithDirection(Location location, ConcurrentSkipListMap<Integer, ItemStack> items, Vector direction) {
        ConcurrentSkipListMap<Integer, Item> dropped = new ConcurrentSkipListMap<>();

        if (items == null || items.isEmpty()) return dropped;
        if (location == null) return dropped;
        if (location.getWorld() == null) return dropped;

        items.forEach((index, item) -> {
            Item i = location.getWorld().dropItem(location, item);
            i.setVelocity(direction);
            dropped.put(dropped.size(), i);
        });

        return dropped;
    }

    /**
     * Drops items at a location with a random direction using the specified normalization and multiplier.
     *
     * @param location   the location to drop items at
     * @param items      a map of item stacks to drop
     * @param normalize  whether to normalize the random direction
     * @param multiplier the scalar multiplier for the random direction
     * @return a map of the dropped item entities
     */
    public static ConcurrentSkipListMap<Integer, Item> dropItems(Location location, ConcurrentSkipListMap<Integer, ItemStack> items, boolean normalize, double multiplier) {
        return dropItemsWithDirection(location, items, getRandomDirection(normalize, multiplier));
    }

    /**
     * Drops items at a location with a random direction using the specified multiplier.
     *
     * @param location   the location to drop items at
     * @param items      a map of item stacks to drop
     * @param multiplier the scalar multiplier for the random direction
     * @return a map of the dropped item entities
     */
    public static ConcurrentSkipListMap<Integer, Item> dropItems(Location location, ConcurrentSkipListMap<Integer, ItemStack> items, double multiplier) {
        return dropItemsWithDirection(location, items, getRandomDirection(multiplier));
    }

    /**
     * Drops items at a location with a random direction using optional normalization.
     *
     * @param location  the location to drop items at
     * @param items     a map of item stacks to drop
     * @param normalize whether to normalize the random direction
     * @return a map of the dropped item entities
     */
    public static ConcurrentSkipListMap<Integer, Item> dropItems(Location location, ConcurrentSkipListMap<Integer, ItemStack> items, boolean normalize) {
        return dropItemsWithDirection(location, items, getRandomDirection(normalize));
    }

    /**
     * Drops items at a location with a default random direction.
     *
     * @param location the location to drop items at
     * @param items    a map of item stacks to drop
     * @return a map of the dropped item entities
     */
    public static ConcurrentSkipListMap<Integer, Item> dropItems(Location location, ConcurrentSkipListMap<Integer, ItemStack> items) {
        return dropItemsWithDirection(location, items, getRandomDirection());
    }
}
