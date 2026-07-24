package host.plas.bou.sql;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks JDBC prepared-statement parameter indexes (1-based when using {@link #next()} from start 0).
 */
@Getter
@Setter
public class DbArg {
    /**
     * The start index of the argument.
     *
     * @param start the start index to set
     * @return the start index
     */
    private int start;
    /**
     * The current argument index.
     *
     * @param i the argument index to set
     * @return the current argument index
     */
    private AtomicInteger i;

    /**
     * Constructor with specified start index.
     * Note: {@link #next()} increments then returns, so the first call yields {@code start + 1}.
     *
     * @param start The start index of the argument
     */
    public DbArg(int start) {
        this.start = start;
        this.i = new AtomicInteger(start);
    }

    /**
     * Constructor with default start index of 0.
     * Note: the first {@link #next()} call returns 1.
     */
    public DbArg() {
        this(0);
    }

    /**
     * Increment and get the next argument index.
     *
     * @return The next argument index
     */
    public int next() {
        return i.incrementAndGet();
    }

    /**
     * Returns the current index without incrementing.
     *
     * @return current index value
     */
    public int current() {
        return i.get();
    }

    /**
     * Set the argument index to the specified value.
     *
     * @param i The new argument index
     */
    public void set(int i) {
        this.i.set(i);
    }

    /**
     * Reset the argument index to the start value.
     */
    public void reset() {
        this.set(this.start);
    }
}
