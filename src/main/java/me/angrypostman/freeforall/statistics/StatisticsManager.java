package me.angrypostman.freeforall.statistics;

import com.google.common.base.Preconditions;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Stream;

//This class will come into use at a later date
public class StatisticsManager {

    private static List<Statistic> statistics = new ArrayList<>();
    private static Map<UUID, List<StatValue>> playerStats = new HashMap<>();

    public static List<Statistic> getStatistics() {
        return new ArrayList<>(statistics);
    }

    public static Optional<Statistic> getStatistic(String name) {
        Preconditions.checkNotNull(name, "statistic name cannot be null");
        return statistics.stream().filter(statistic -> statistic.getName().equalsIgnoreCase(name)).findFirst();
    }

    public static Optional<StatValue> getValue(Player player, Statistic statistic) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(statistic, "statistic cannot be null");
        Preconditions.checkArgument(player.isOnline(), "player not online");
        return playerStats.entrySet().stream()
                .filter(entry -> entry.getKey().equals(player.getUniqueId()))
                .flatMap(entry -> entry.getValue().stream())
                .filter(value -> value.getStatistic().getName().equals(statistic.getName())).findFirst();
    }

    public static void registerStatistic(Statistic statistic) {
        Preconditions.checkNotNull(statistic, "statistic");
        Preconditions.checkArgument(!isRegistered(statistic), "statistic already registered");
        statistics.add(statistic);
    }

    private static boolean isRegistered(Statistic statistic) {
        return getStatistic(statistic.getName()).isPresent();
    }

}
