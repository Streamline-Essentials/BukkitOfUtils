package host.plas.bou.utils.obj;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
public class StringArgument implements Comparable<StringArgument> {
    public enum ContentType {
        USABLE_STRING,
        EMPTY_STRING,
        NULL_STRING,
        BROKEN,
        ;
    }

    public enum ContextType {
        COMMAND,
        PLACEHOLDER,
        ;
    }

    private final int index;
    @Setter
    private String content;
    @Setter @Nullable
    private ContentType overrideType;
    @Getter @Setter
    private ContextType contextType;

    public StringArgument(int index, String content, @Nullable ContentType overrideType, ContextType contextType) {
        this.index = index;
        this.content = content;
        this.overrideType = overrideType;
        this.contextType = contextType;
    }

    public StringArgument(int index, String content, ContextType contextType) {
        this(index, content, null, contextType);
    }

    public StringArgument(ContextType contextType) {
        this(-1, null, ContentType.BROKEN, contextType);
    }

    public ContentType getContentType() {
        if (overrideType != null) return overrideType;

        if (content == null) return ContentType.NULL_STRING;
        if (content.isEmpty()) return ContentType.EMPTY_STRING;
        if (content.isBlank()) return ContentType.EMPTY_STRING;
        if (content.equals("")) return ContentType.EMPTY_STRING;

        return ContentType.USABLE_STRING;
    }

    public boolean isUsable() {
        return getContentType() == ContentType.USABLE_STRING;
    }

    public boolean isEmpty() {
        return getContentType() == ContentType.EMPTY_STRING;
    }

    public boolean isNull() {
        return getContentType() == ContentType.NULL_STRING;
    }

    public boolean equals(String string) {
        return Objects.equals(content, string);
    }

    public String getContent() {
        if (! isUsable()) return "";

        return content;
    }

    @Override
    public int compareTo(@NotNull StringArgument o) {
        return Integer.compare(index, o.index);
    }
}
