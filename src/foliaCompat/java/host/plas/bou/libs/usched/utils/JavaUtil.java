package host.plas.bou.libs.usched.utils;

/**
 * Small reflection helpers used to probe for optional server APIs.
 *
 * <p>Vendored from UniversalScheduler so BOU controls its bytecode level; the upstream
 * artifact ships class file version 65.0 (Java 21) and cannot load on the Java 11
 * runtimes BOU supports.</p>
 */
public class JavaUtil {
    /**
     * Checks whether a class is present on the current classpath.
     *
     * @param className the fully qualified class name
     * @return true if the class could be loaded
     */
    public static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
