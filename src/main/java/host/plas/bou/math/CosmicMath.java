package host.plas.bou.math;

import java.util.Random;

/**
 * Utility class providing common mathematical operations including distance calculations,
 * random number generation, angle computations, and ceiling division.
 */
public class CosmicMath {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CosmicMath() {
        // Utility class
    }

    /**
     * Calculates the Euclidean distance between two points in 3D space.
     *
     * @param x1 the x-coordinate of the first point
     * @param y1 the y-coordinate of the first point
     * @param z1 the z-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param y2 the y-coordinate of the second point
     * @param z2 the z-coordinate of the second point
     * @return the distance between the two points
     */
    public static double getDistance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(getDistance3DSquared(x1, y1, z1, x2, y2, z2));
    }

    /**
     * Calculates the squared Euclidean distance between two points in 3D space.
     * This avoids the square root computation and is useful for distance comparisons.
     *
     * @param x1 the x-coordinate of the first point
     * @param y1 the y-coordinate of the first point
     * @param z1 the z-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param y2 the y-coordinate of the second point
     * @param z2 the z-coordinate of the second point
     * @return the squared distance between the two points
     */
    public static double getDistance3DSquared(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Calculates the Euclidean distance between two points in 2D space (ignoring the y-axis).
     *
     * @param x1 the x-coordinate of the first point
     * @param z1 the z-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param z2 the z-coordinate of the second point
     * @return the distance between the two points
     */
    public static double getDistance2D(double x1, double z1, double x2, double z2) {
        return Math.sqrt(getDistance2DSquared(x1, z1, x2, z2));
    }

    /**
     * Calculates the squared Euclidean distance between two points in 2D space.
     * This avoids the square root computation and is useful for distance comparisons.
     *
     * @param x1 the x-coordinate of the first point
     * @param z1 the z-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param z2 the z-coordinate of the second point
     * @return the squared distance between the two points
     */
    public static double getDistance2DSquared(double x1, double z1, double x2, double z2) {
        double dx = x1 - x2;
        double dz = z1 - z2;
        return dx * dx + dz * dz;
    }

    /**
     * Creates a new Random instance.
     *
     * @return a new {@link Random} object
     */
    public static Random getNewRandom() {
        return new Random();
    }

    /**
     * Generates a random integer within the specified inclusive range.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random integer between min and max, inclusive
     */
    public static int getRandomInt(int min, int max) {
        return min + getNewRandom().nextInt(max - min + 1);
    }

    /**
     * Generates a random double within the specified range.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (exclusive)
     * @return a random double between min (inclusive) and max (exclusive)
     */
    public static double getRandomDouble(double min, double max) {
        return min + getNewRandom().nextDouble() * (max - min);
    }

    /**
     * Generates a random value from a Gaussian (normal) distribution with the specified mean and standard deviation.
     *
     * @param mean the mean of the distribution
     * @param stdDev the standard deviation of the distribution
     * @return a random value sampled from the specified Gaussian distribution
     */
    public static double getRandomGaussian(double mean, double stdDev) {
        return mean + getNewRandom().nextGaussian() * stdDev;
    }

    /**
     * Generates a random float within the specified range.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (exclusive)
     * @return a random float between min (inclusive) and max (exclusive)
     */
    public static float getRandomFloat(float min, float max) {
        return min + getNewRandom().nextFloat() * (max - min);
    }

    /**
     * Generates a random boolean value.
     *
     * @return a random boolean, either true or false
     */
    public static boolean getRandomBoolean() {
        return getNewRandom().nextBoolean();
    }

    /**
     * Generates a random long within the specified range.
     *
     * @param min the minimum value (inclusive)
     * @param max the maximum value (exclusive)
     * @return a random long between min and max
     */
    public static long getRandomLong(long min, long max) {
        return min + (getNewRandom().nextLong() * (max - min));
    }

    /**
     * Calculates the angle in radians between two points in 2D space using atan2.
     *
     * @param x1 the x-coordinate of the first point
     * @param z1 the z-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param z2 the z-coordinate of the second point
     * @return the angle in radians from the first point to the second point
     */
    public static double getAngle2D(double x1, double z1, double x2, double z2) {
        return Math.atan2(z2 - z1, x2 - x1);
    }

    /**
     * Calculates the polar angle in radians between two points in 3D space.
     * The angle is measured from the y-axis to the line connecting the two points.
     *
     * @param x1 the x-coordinate of the first point
     * @param y1 the y-coordinate of the first point
     * @param z1 the z-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param y2 the y-coordinate of the second point
     * @param z2 the z-coordinate of the second point
     * @return the polar angle in radians
     */
    public static double getAngle3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.atan2(Math.sqrt(dx * dx + dz * dz), dy);
    }

    /**
     * Computes the ceiling division of two integers, rounding towards positive infinity.
     *
     * @param x the dividend
     * @param y the divisor
     * @return the smallest integer greater than or equal to the algebraic quotient
     */
    public static int ceilDiv(int x, int y) {
        return - Math.floorDiv(- x, y); // ceil(x / y) = - floor(- x / y)
    }

    /**
     * Computes the ceiling division of two longs, rounding towards positive infinity.
     *
     * @param x the dividend
     * @param y the divisor
     * @return the smallest long greater than or equal to the algebraic quotient
     */
    public static long ceilDiv(long x, long y) {
        return - Math.floorDiv(- x, y); // ceil(x / y) = - floor(- x / y)
    }
}
