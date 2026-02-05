package host.plas.bou.serialization;

public interface ISerializer<V> {
    default V fromString(String value) {
        return new ObjectSerializer<V>(value).getObject();
    }

    default String toString(V object) {
        return new ObjectSerializer<>(object).getCode();
    }
}