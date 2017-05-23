package me.angrypostman.freeforall.data;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;

import java.io.File;
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
        return false;
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

    public File getFile() {
        return file;
    }
}
