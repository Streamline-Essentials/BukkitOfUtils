package host.plas.bou.utils.obj;

/**
 * Enum representing the different capitalization strategies that can be applied to strings.
 */
public enum CapitalizationType {
    /** Converts the entire string to lower case. */
    LOWER_ALL,
    /** Converts only the first character to lower case. */
    LOWER_FIRST,

    /** Converts the entire string to upper case. */
    UPPER_ALL,
    /** Converts only the first character to upper case. */
    UPPER_FIRST,

    /** Converts each word entirely to lower case. */
    WORD_LOWER_ALL,
    /** Converts the first character of each word to lower case. */
    WORD_LOWER_FIRST,

    /** Converts each word entirely to upper case. */
    WORD_UPPER_ALL,
    /** Converts the first character of each word to upper case. */
    WORD_UPPER_FIRST,

    ;
}
