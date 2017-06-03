package me.angrypostman.freeforall.data;

import me.angrypostman.freeforall.user.User;
import org.bukkit.Location;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class DataStorage {

    public abstract boolean initialize();

    public abstract void close();

    public abstract Optional<User> createUser(UUID uuid, String playerName);

    public abstract Optional<User> loadUser(UUID uuid);

    public abstract Optional<User> loadUser(String lookupName);

    public abstract void saveUser(User user);

    public abstract List<User> getLeardboardTop(int page);

    public abstract void saveLocation(Location location);

    public abstract List<Location> getLocations();

}
