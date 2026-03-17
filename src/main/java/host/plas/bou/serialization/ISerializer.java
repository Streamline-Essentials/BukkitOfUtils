package host.plas.bou.serialization;

/**
 * A generic interface for serializing and deserializing objects to and from strings
 * using Base64-encoded Bukkit object streams.
 *
 * @param <V> the type of object to serialize and deserialize
 */
public interface ISerializer<V> {
    /**
     * Deserializes an object from its Base64-encoded string representation.
     *
     * @param value the Base64-encoded string to deserialize
     * @return the deserialized object
     */
    default V fromString(String value) {
        return new ObjectSerializer<V>(value).getObject();
    }

    /**
     * Serializes an object to its Base64-encoded string representation.
     *
     * @param object the object to serialize
     * @return the Base64-encoded string representation
     */
    default String toString(V object) {
        return new ObjectSerializer<>(object).getCode();
    }
}