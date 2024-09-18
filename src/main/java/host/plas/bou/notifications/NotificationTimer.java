package host.plas.bou.notifications;

import host.plas.bou.scheduling.BaseRunnable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

@Getter @Setter
public class NotificationTimer extends BaseRunnable implements Comparable<NotificationTimer> {
    @Getter @Setter
    private static ConcurrentSkipListSet<NotificationTimer> notifications = new ConcurrentSkipListSet<>();

    public static Optional<NotificationTimer> addNotification(String identifier, CommandSender player) {
        if (hasNotification(identifier, player)) return Optional.empty();

        NotificationTimer notificationTimer = new NotificationTimer(identifier, player);
        notifications.add(notificationTimer);

        return Optional.of(notificationTimer);
    }

    public static void removeNotification(String identifier, CommandSender player) {
        if (! hasNotification(identifier, player)) return;

        getNotificationTimer(identifier, player).ifPresent(notification -> notifications.remove(notification));
    }

    public static Optional<NotificationTimer> getNotificationTimer(String identifier, CommandSender player) {
        AtomicReference<NotificationTimer> notificationTimer = new AtomicReference<>();

        notifications.forEach(notification -> {
            if (notification.getIdentifier().equals(identifier) && notification.getPlayer().getName().equals(player.getName())) {
                notificationTimer.set(notification);
            }
        });

        return Optional.ofNullable(notificationTimer.get());
    }

    public static boolean hasNotification(String identifier, CommandSender player) {
        return getNotificationTimer(identifier, player).isPresent();
    }

    private String identifier;
    private CommandSender player;

    private NotificationTimer(String identifier, CommandSender player) {
        super(20, 1); // 5 second delayed then cancels. Asynchronous.

        this.identifier = identifier;
        this.player = player;
    }

    @Override
    public void run() {
        removeNotification(identifier, player);

        cancel();
    }

    @Override
    public int compareTo(@NotNull NotificationTimer o) {
        return getIdentifier().compareTo(o.getIdentifier());
    }
}
