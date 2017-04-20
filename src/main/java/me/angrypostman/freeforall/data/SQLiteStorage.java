package me.angrypostman.freeforall.data;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;

import java.io.File;
import java.util.UUID;

public class SQLiteStorage extends DataStorage {

    private FreeForAll plugin = null;
    private File file = null;

    public SQLiteStorage(FreeForAll plugin, File file) {
        this.plugin = plugin;
        this.file = file;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void close() {

    }

    @Override
    public User createUser(UUID uuid, String playerName) {
        return null;
    }

    @Override
    public User loadUser(UUID uuid) {
        return null;
    }

    @Override
    public User loadUser(String lookupName) {
        return null;
    }

    @Override
    public void saveUser(User user) {

    }

    public File getFile() {
        return file;
    }
}
