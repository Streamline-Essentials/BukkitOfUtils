package host.plas.bou.serialization;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Serializes and deserializes objects to and from Base64-encoded strings
 * using Bukkit's object stream classes. Supports any serializable type.
 *
 * @param <V> the type of object being serialized or deserialized
 */
@Getter
public class ObjectSerializer<V> {
    /**
     * The Base64-encoded string representation of the serialized object.
     *
     * @return the Base64-encoded string
     */
    private final String code;
    /**
     * The deserialized object instance.
     *
     * @return the object
     */
    private final V object;

    /**
     * Deserializes an object from a Base64-encoded string.
     *
     * @param code the Base64-encoded string to decode and deserialize
     */
    @SneakyThrows
    public ObjectSerializer(String code) {
        final ByteArrayInputStream stream = new ByteArrayInputStream(Base64Coder.decodeLines(code));
        final BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(stream);

        final Object object = objectInputStream.readObject();
        objectInputStream.close();

        this.code = code;
        this.object = (V) object;
    }

    /**
     * Serializes the given object to a Base64-encoded string.
     *
     * @param object the object to serialize
     */
    @SneakyThrows
    public ObjectSerializer(V object) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final BukkitObjectOutputStream objectOutputStream = new BukkitObjectOutputStream(stream);

        objectOutputStream.writeObject(object);
        objectOutputStream.close();

        this.object = object;
        this.code = Base64Coder.encodeLines(stream.toByteArray());
    }
}