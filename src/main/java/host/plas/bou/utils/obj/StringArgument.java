package host.plas.bou.utils.obj;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a single string argument with an index, content, content type classification,
 * and context type (command or placeholder).
 */
@Getter
public class StringArgument implements Comparable<StringArgument> {
    /**
     * Enum representing the type of content this argument holds.
     */
    public enum ContentType {
        /** Represents a non-null, non-empty, usable string argument. */
        USABLE_STRING,
        /** Represents an empty or blank string argument. */
        EMPTY_STRING,
        /** Represents a null string argument. */
        NULL_STRING,
        /** Represents a broken or invalid argument. */
        BROKEN,
        ;
    }

    /**
     * Enum representing the context in which this argument is used.
     */
    public enum ContextType {
        /** Indicates the argument is used in a command context. */
        COMMAND,
        /** Indicates the argument is used in a placeholder context. */
        PLACEHOLDER,
        ;
    }

    /**
     * The positional index of this argument.
     *
     * @return the positional index
     */
    private final int index;
    /**
     * The string content of this argument.
     *
     * @param content the string content to set
     * @return the string content
     */
    @Setter
    private String content;
    /**
     * An optional content type override; when non-null, bypasses automatic content type detection.
     *
     * @param overrideType the content type override to set, or null for automatic detection
     * @return the content type override, or null if not set
     */
    @Setter @Nullable
    private ContentType overrideType;
    /**
     * The context type indicating whether this argument is used in a command or placeholder context.
     *
     * @param contextType the context type to set
     * @return the context type
     */
    @Getter @Setter
    private ContextType contextType;

    /**
     * Constructs a StringArgument with all fields specified.
     *
     * @param index        the positional index of this argument
     * @param content      the string content
     * @param overrideType an optional content type override, or null for automatic detection
     * @param contextType  the context type (COMMAND or PLACEHOLDER)
     */
    public StringArgument(int index, String content, @Nullable ContentType overrideType, ContextType contextType) {
        this.index = index;
        this.content = content;
        this.overrideType = overrideType;
        this.contextType = contextType;
    }

    /**
     * Constructs a StringArgument with automatic content type detection.
     *
     * @param index       the positional index of this argument
     * @param content     the string content
     * @param contextType the context type (COMMAND or PLACEHOLDER)
     */
    public StringArgument(int index, String content, ContextType contextType) {
        this(index, content, null, contextType);
    }

    /**
     * Constructs a broken (invalid) StringArgument with no content.
     *
     * @param contextType the context type (COMMAND or PLACEHOLDER)
     */
    public StringArgument(ContextType contextType) {
        this(-1, null, ContentType.BROKEN, contextType);
    }

    /**
     * Determines the content type of this argument, respecting any override.
     *
     * @return the content type
     */
    public ContentType getContentType() {
        if (overrideType != null) return overrideType;

        if (content == null) return ContentType.NULL_STRING;
        if (content.isEmpty()) return ContentType.EMPTY_STRING;
        if (content.isBlank()) return ContentType.EMPTY_STRING;
        if (content.equals("")) return ContentType.EMPTY_STRING;

        return ContentType.USABLE_STRING;
    }

    /**
     * Checks whether this argument has usable (non-null, non-empty) content.
     *
     * @return true if the content type is USABLE_STRING
     */
    public boolean isUsable() {
        return getContentType() == ContentType.USABLE_STRING;
    }

    /**
     * Checks whether this argument has empty content.
     *
     * @return true if the content type is EMPTY_STRING
     */
    public boolean isEmpty() {
        return getContentType() == ContentType.EMPTY_STRING;
    }

    /**
     * Checks whether this argument has null content.
     *
     * @return true if the content type is NULL_STRING
     */
    public boolean isNull() {
        return getContentType() == ContentType.NULL_STRING;
    }

    /**
     * Checks whether this argument's content equals the given string.
     *
     * @param string the string to compare against
     * @return true if the content equals the given string
     */
    public boolean equals(String string) {
        return Objects.equals(content, string);
    }

    /**
     * Returns the content of this argument, or an empty string if the content is not usable.
     *
     * @return the content string, or empty string if not usable
     */
    public String getContent() {
        if (! isUsable()) return "";

        return content;
    }

    @Override
    public int compareTo(@NotNull StringArgument o) {
        return Integer.compare(index, o.index);
    }
}
