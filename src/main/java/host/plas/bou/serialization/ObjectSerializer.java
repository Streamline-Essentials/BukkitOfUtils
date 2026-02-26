package host.plas.bou.serialization;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Getter
public class ObjectSerializer<V> {
    private final String code;
    private final V object;

    @SneakyThrows
    public ObjectSerializer(String code) {
        final ByteArrayInputStream stream = new ByteArrayInputStream(Base64Coder.decodeLines(code));
        final BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(stream);

        final Object object = objectInputStream.readObject();
        objectInputStream.close();

        this.code = code;
        this.object = (V) object;
    }

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