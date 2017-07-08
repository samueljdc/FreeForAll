package me.angrypostman.freeforall.data;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;
import org.bukkit.Location;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SQLiteStorage extends DataStorage {

    private FreeForAll plugin = null;
    private File file = null;

    public SQLiteStorage(FreeForAll plugin, File file) {
        this.plugin = plugin;
        this.file = file;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public void close() {

    }

    @Override
    public Optional<User> createUser(UUID uuid, String playerName) {
        return null;
    }

    @Override
    public Optional<User> loadUser(UUID uuid) {
        return null;
    }

    @Override
    public Optional<User> loadUser(String lookupName) {
        return null;
    }

    @Override
    public void saveUser(User user) {

    }

    @Override
    public List<User> getLeardboardTop(int page) {
        return null;
    }

    @Override
    public void saveLocation(Location location) {

    }

    @Override
    public void deleteLocation(int spawnId) {

    }

    @Override
    public List<Location> getLocations() {
        return null;
    }

    public File getFile() {
        return file;
    }
}
