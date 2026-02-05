package host.plas.bou.math;

import java.util.Random;

public class CosmicMath {
    public static double getDistance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(getDistance3DSquared(x1, y1, z1, x2, y2, z2));
    }

    public static double getDistance3DSquared(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return dx * dx + dy * dy + dz * dz;
    }

    public static double getDistance2D(double x1, double z1, double x2, double z2) {
        return Math.sqrt(getDistance2DSquared(x1, z1, x2, z2));
    }

    public static double getDistance2DSquared(double x1, double z1, double x2, double z2) {
        double dx = x1 - x2;
        double dz = z1 - z2;
        return dx * dx + dz * dz;
    }

    public static Random getNewRandom() {
        return new Random();
    }

    public static int getRandomInt(int min, int max) {
        return min + getNewRandom().nextInt(max - min + 1);
    }

    public static double getRandomDouble(double min, double max) {
        return min + getNewRandom().nextDouble() * (max - min);
    }

    public static double getRandomGaussian(double mean, double stdDev) {
        return mean + getNewRandom().nextGaussian() * stdDev;
    }

    public static float getRandomFloat(float min, float max) {
        return min + getNewRandom().nextFloat() * (max - min);
    }

    public static boolean getRandomBoolean() {
        return getNewRandom().nextBoolean();
    }

    public static long getRandomLong(long min, long max) {
        return min + (getNewRandom().nextLong() * (max - min));
    }

    public static double getAngle2D(double x1, double z1, double x2, double z2) {
        return Math.atan2(z2 - z1, x2 - x1);
    }

    public static double getAngle3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.atan2(Math.sqrt(dx * dx + dz * dz), dy);
    }

    public static int ceilDiv(int x, int y) {
        return - Math.floorDiv(- x, y); // ceil(x / y) = - floor(- x / y)
    }

    public static long ceilDiv(long x, long y) {
        return - Math.floorDiv(- x, y); // ceil(x / y) = - floor(- x / y)
    }
}
