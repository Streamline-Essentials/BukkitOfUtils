package host.plas.bou.notifications;

import host.plas.bou.scheduling.BaseRunnable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A timer-based notification system that tracks temporary notifications per player.
 * Each notification automatically removes itself after a short delay (approximately 5 seconds).
 * Extends {@link BaseRunnable} for scheduled execution and implements {@link Comparable}
 * for ordered storage in a {@link ConcurrentSkipListSet}.
 */
@Getter @Setter
public class NotificationTimer extends BaseRunnable implements Comparable<NotificationTimer> {
    /**
     * The global set of all active notification timers.
     *
     * @param notifications the set of notification timers to set
     * @return the set of active notification timers
     */
    @Getter @Setter
    private static ConcurrentSkipListSet<NotificationTimer> notifications = new ConcurrentSkipListSet<>();

    /**
     * Adds a new notification for the specified player with the given identifier.
     * If a notification with the same identifier already exists for the player, no new one is created.
     *
     * @param identifier the unique identifier for this notification
     * @param player the command sender (player) to associate with the notification
     * @return an Optional containing the new NotificationTimer if created, or empty if one already exists
     */
    public static Optional<NotificationTimer> addNotification(String identifier, CommandSender player) {
        if (hasNotification(identifier, player)) return Optional.empty();

        NotificationTimer notificationTimer = new NotificationTimer(identifier, player);
        notifications.add(notificationTimer);

        return Optional.of(notificationTimer);
    }

    /**
     * Removes the notification with the specified identifier for the given player, if it exists.
     *
     * @param identifier the unique identifier for the notification to remove
     * @param player the command sender (player) whose notification should be removed
     */
    public static void removeNotification(String identifier, CommandSender player) {
        if (! hasNotification(identifier, player)) return;

        getNotificationTimer(identifier, player).ifPresent(notification -> notifications.remove(notification));
    }

    /**
     * Retrieves the notification timer matching the given identifier and player.
     *
     * @param identifier the unique identifier of the notification
     * @param player the command sender (player) associated with the notification
     * @return an Optional containing the matching NotificationTimer, or empty if not found
     */
    public static Optional<NotificationTimer> getNotificationTimer(String identifier, CommandSender player) {
        AtomicReference<NotificationTimer> notificationTimer = new AtomicReference<>();

        notifications.forEach(notification -> {
            if (notification.getIdentifier().equals(identifier) && notification.getPlayer().getName().equals(player.getName())) {
                notificationTimer.set(notification);
            }
        });

        return Optional.ofNullable(notificationTimer.get());
    }

    /**
     * Checks whether a notification with the given identifier exists for the specified player.
     *
     * @param identifier the unique identifier of the notification
     * @param player the command sender (player) to check
     * @return true if the notification exists, false otherwise
     */
    public static boolean hasNotification(String identifier, CommandSender player) {
        return getNotificationTimer(identifier, player).isPresent();
    }

    /**
     * The unique identifier for this notification.
     *
     * @param identifier the notification identifier to set
     * @return the notification identifier
     */
    private String identifier;

    /**
     * The command sender (player) this notification is associated with.
     *
     * @param player the command sender to set
     * @return the command sender
     */
    private CommandSender player;

    /**
     * Constructs a new NotificationTimer with the specified identifier and player.
     * The timer is configured with a 20-tick delay (approximately 1 second) and runs once.
     *
     * @param identifier the unique identifier for this notification
     * @param player the command sender (player) this notification is associated with
     */
    private NotificationTimer(String identifier, CommandSender player) {
        super(20, 1); // 5 second delayed then cancels. Asynchronous.

        this.identifier = identifier;
        this.player = player;
    }

    /**
     * Executes when the timer fires. Removes this notification from the active set and cancels the task.
     */
    @Override
    public void run() {
        removeNotification(identifier, player);

        cancel();
    }

    /**
     * Compares this NotificationTimer to another based on their identifiers.
     *
     * @param o the other NotificationTimer to compare to
     * @return a negative integer, zero, or a positive integer as this identifier
     *         is less than, equal to, or greater than the other identifier
     */
    @Override
    public int compareTo(@NotNull NotificationTimer o) {
        return getIdentifier().compareTo(o.getIdentifier());
    }
}
