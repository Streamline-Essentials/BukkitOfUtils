package host.plas.bou.profiles;

import java.util.Optional;

/**
 * The outcome of a player lookup, distinguishing "this player does not exist" from
 * "nothing could be checked" (web lookups disabled, no internet, or every provider failing).
 */
public final class LookupResult {
    /**
     * The status of a lookup.
     */
    public enum Status {
        /** At least a UUID and name (or a skin) are known. */
        FOUND,
        /** A provider answered authoritatively that no such player exists. */
        NOT_FOUND,
        /** Nothing is known locally and no provider could be asked or reached. */
        UNAVAILABLE,
        ;
    }

    private final Status status;
    private final ProfileData profile;
    private final boolean complete;

    private LookupResult(Status status, ProfileData profile, boolean complete) {
        this.status = status;
        this.profile = profile;
        this.complete = complete;
    }

    /**
     * Creates a found result.
     *
     * @param profile  the profile data
     * @param complete whether every applicable source answered (incomplete results are cached briefly)
     * @return the result
     */
    public static LookupResult found(ProfileData profile, boolean complete) {
        return new LookupResult(Status.FOUND, profile, complete);
    }

    /**
     * Creates a not-found result.
     *
     * @return the result
     */
    public static LookupResult notFound() {
        return new LookupResult(Status.NOT_FOUND, null, true);
    }

    /**
     * Creates an unavailable result.
     *
     * @return the result
     */
    public static LookupResult unavailable() {
        return new LookupResult(Status.UNAVAILABLE, null, false);
    }

    /**
     * Returns the lookup status.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the profile data, present only when {@link #getStatus()} is {@link Status#FOUND}.
     *
     * @return the profile data
     */
    public Optional<ProfileData> getProfile() {
        return Optional.ofNullable(profile);
    }

    /**
     * Whether every applicable source answered. A found result can be incomplete when, for
     * example, the name is known locally but the skin provider could not be reached.
     *
     * @return true if complete
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Whether the lookup found the player.
     *
     * @return true for {@link Status#FOUND}
     */
    public boolean isFound() {
        return status == Status.FOUND;
    }
}
