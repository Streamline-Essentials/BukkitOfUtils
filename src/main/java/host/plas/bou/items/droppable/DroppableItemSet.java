package host.plas.bou.items.droppable;

import host.plas.dma.arena.GameType;
import host.plas.dma.managers.LoggingHandler;
import lombok.Getter;
import lombok.Setter;
import tv.quaint.objects.Identifiable;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter @Setter
public class DroppableItemSet implements Identifiable {
    private String identifier;
    private ConcurrentSkipListSet<String> arenaSet;
    private ConcurrentSkipListSet<DroppableItem> itemSet;

    public DroppableItemSet(String identifier, ConcurrentSkipListSet<String> arenaSet, ConcurrentSkipListSet<DroppableItem> itemSet) {
        this.identifier = identifier;
        this.arenaSet = arenaSet;
        this.itemSet = itemSet;
    }

    public DroppableItemSet(String identifier, ConcurrentSkipListSet<String> arenaSet, DroppableItem... items) {
        this(identifier, arenaSet, createItemSet(items));
    }

    public DroppableItemSet(String identifier, String arena, DroppableItem... items) {
        this(identifier, createArenaSet(arena), createItemSet(items));
    }

    public static ConcurrentSkipListSet<String> createArenaSet(String... arenas) {
        return new ConcurrentSkipListSet<>(Arrays.asList(arenas));
    }

    public static ConcurrentSkipListSet<DroppableItem> createItemSet(DroppableItem... items) {
        return new ConcurrentSkipListSet<>(Arrays.asList(items));
    }

    public ConcurrentSkipListSet<GameType> getGameTypes() {
        ConcurrentSkipListSet<GameType> gameTypes = new ConcurrentSkipListSet<>();

        getArenaSet().forEach(arena -> {
            try {
                if (arena.startsWith("TYPE:")) {
                    String type = arena.split(":")[1];
                    gameTypes.add(GameType.valueOf(type));
                }
            } catch (Throwable e) {
                // ignore
            }
        });

        return gameTypes;
    }

    // Returns a single DroppableItem that passes the roll
    // DroppableItem chances will be added together in a map,
    // then a random number will be generated and checked against the chances
    // So if you have 2 items with 50% chance each, you will have a 50% chance of getting one of them
    public Optional<DroppableItem> pollItems() {
        ConcurrentSkipListMap<Double, DroppableItem> itemChances = new ConcurrentSkipListMap<>();

        getItemSet().forEach(item -> {
            double lastChance = itemChances.isEmpty() ? 0 : itemChances.lastKey();
            double itemChance = item.getChance();

            double chance = lastChance + itemChance;

            itemChances.put(chance, item);
        });

        Random RNG = new Random();
        Double lastChance = itemChances.lastKey();
        if (lastChance == null) {
            return Optional.empty();
        }
        double random = RNG.nextDouble(0, lastChance);

        try {
            return Optional.ofNullable(itemChances.ceilingEntry(random).getValue());
        } catch (Throwable e) {
            LoggingHandler.logWarning("Failed to poll item from DroppableItemSet: " + getIdentifier(), e);
            return Optional.empty();
        }
    }

    public boolean checkArena(String arena) {
        return getArenaSet().contains(arena);
    }

    public boolean checkGameType(GameType type) {
        return getGameTypes().contains(type);
    }
}
