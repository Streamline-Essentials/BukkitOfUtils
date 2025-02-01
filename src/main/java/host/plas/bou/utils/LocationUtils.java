package host.plas.bou.utils;

import com.google.re2j.Matcher;
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
import tv.quaint.utils.MatcherUtils;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;

public class LocationUtils {
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

    public static Optional<Location> fromString(String location) {
        return fromString(location, false);
    }

    public static String toString(Location location, boolean withYawPitch) {
        if (location == null) return "world,0,0,0";
        if (location.getWorld() == null) return "world,0,0,0";
        if (withYawPitch) {
            return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
        }
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
    }

    public static String toString(Location location) {
        return toString(location, false);
    }

    public static Location getCenteredLocation(Location location) {
        return new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5);
    }

    public static Location getTopLocation(Location location) {
        return getTopLocation(location, true);
    }

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

    public static Location searchForTopBlock(Location location, BlockFace direction) {
        return searchForTopBlock(location, direction, false, false);
    }

    public static Location searchForTopBlock(Location location, BlockFace direction, boolean centered) {
        return searchForTopBlock(location, direction, false, centered);
    }

    private static boolean isAirLike(Location location) {
        return isAirLike(location.getBlock());
    }

    private static boolean isAirLike(Block block) {
        return block.getType() == Material.AIR ||
                block.isPassable() && ! block.getType().isSolid(); // This covers flowers, tall grass, etc.
    }

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

    public static Location getTopMostTopLocation(Location location) {
        return getTopMostTopLocation(location, true);
    }

    public static Location getTopMostTopLocation(Location location, boolean centered) {
        return getCenteredLocation(getTopMostTopBlock(location).getLocation());
    }

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

    public static boolean checkForTopableBlock(Location location) {
        return checkForTopableBlock(location.getBlock());
    }

    // Ensure to check for highest block after using this.
    public static boolean checkForTopableBlock(Block block) {
        return (isAirLike(block) && isAirLike(block.getRelative(BlockFace.UP)));
    }

    public static Location getHighestNonAirBlock(Location location) {
        if (location == null) return null;
        if (location.getWorld() == null) return null;
        return new Location(location.getWorld(), location.getBlockX(), location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()), location.getBlockZ());
    }

    public static void teleport(Entity entity, Location location) {
        TaskManager.teleport(entity, location);
    }

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

    public static Vector getRandomDirection(double multiplier) {
        return getRandomDirection(false, multiplier);
    }

    public static Vector getRandomDirection(boolean normalize) {
        return getRandomDirection(normalize, 1);
    }

    public static Vector getRandomDirection() {
        return getRandomDirection(1);
    }

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

    public static ConcurrentSkipListMap<Integer, Item> dropItems(Location location, ConcurrentSkipListMap<Integer, ItemStack> items, boolean normalize, double multiplier) {
        return dropItemsWithDirection(location, items, getRandomDirection(normalize, multiplier));
    }

    public static ConcurrentSkipListMap<Integer, Item> dropItems(Location location, ConcurrentSkipListMap<Integer, ItemStack> items, double multiplier) {
        return dropItemsWithDirection(location, items, getRandomDirection(multiplier));
    }

    public static ConcurrentSkipListMap<Integer, Item> dropItems(Location location, ConcurrentSkipListMap<Integer, ItemStack> items, boolean normalize) {
        return dropItemsWithDirection(location, items, getRandomDirection(normalize));
    }

    public static ConcurrentSkipListMap<Integer, Item> dropItems(Location location, ConcurrentSkipListMap<Integer, ItemStack> items) {
        return dropItemsWithDirection(location, items, getRandomDirection());
    }
}
