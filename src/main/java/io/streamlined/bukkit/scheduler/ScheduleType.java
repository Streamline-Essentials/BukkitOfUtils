package io.streamlined.bukkit.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;

public enum ScheduleType {
    LOCATION,
    ENTITY,
    GLOBAL,
    ;

    public static ScheduleType getType(@Nullable Entity entity, @Nullable Location runAt) {
        if (entity != null) {
            return ScheduleType.ENTITY;
        } else if (runAt != null) {
            return ScheduleType.LOCATION;
        }
        return ScheduleType.GLOBAL;
    }
}