package host.plas.bou.utils;

import host.plas.bou.BukkitOfUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import tv.quaint.thebase.lib.re2j.Matcher;
import tv.quaint.utils.MatcherUtils;

import java.util.List;
import java.util.Optional;

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
            top = searchForTopBlock(location, BlockFace.DOWN);
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
        Location top = location.clone();
        Block block = top.getBlock();
        World world = top.getWorld();
        if (world == null) return null;
        while (! checkForTopableBlock(block)) {
            if (block.getY() < world.getMinHeight() && block.getY() > world.getMaxHeight()) return null;

            block = top.getBlock().getRelative(direction);
            top = block.getLocation();
        }

        if (block.getY() < world.getMinHeight() && block.getY() > world.getMaxHeight()) return null;

        return getCenteredLocation(top);
    }

    public static boolean checkForTopableBlock(Location location) {
        return checkForTopableBlock(location.getBlock());
    }

    // Ensure to check for highest block after using this.
    public static boolean checkForTopableBlock(Block block) {
        return (block.getType() != Material.AIR && block.getRelative(BlockFace.UP).getType() == Material.AIR &&
                block.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType() == Material.AIR);
    }

    public static Location getHighestNonAirBlock(Location location) {
        if (location == null) return null;
        if (location.getWorld() == null) return null;
        return new Location(location.getWorld(), location.getBlockX(), location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()), location.getBlockZ());
    }
}
