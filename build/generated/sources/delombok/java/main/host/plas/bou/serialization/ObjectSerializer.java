package host.plas.bou.serialization;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ObjectSerializer<V> {
    private final String code;
    private final V object;

    public ObjectSerializer(String code) {
        try {
            final ByteArrayInputStream stream = new ByteArrayInputStream(Base64Coder.decodeLines(code));
            final BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(stream);
            final Object object = objectInputStream.readObject();
            objectInputStream.close();
            this.code = code;
            this.object = (V) object;
        } catch (final java.lang.Throwable $ex) {
            throw lombok.Lombok.sneakyThrow($ex);
        }
    }

    public ObjectSerializer(V object) {
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final BukkitObjectOutputStream objectOutputStream = new BukkitObjectOutputStream(stream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
            this.object = object;
            this.code = Base64Coder.encodeLines(stream.toByteArray());
        } catch (final java.lang.Throwable $ex) {
            throw lombok.Lombok.sneakyThrow($ex);
        }
    }

    public String getCode() {
        return this.code;
    }

    public V getObject() {
        return this.object;
    }
}
