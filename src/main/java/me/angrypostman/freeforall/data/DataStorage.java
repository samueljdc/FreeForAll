package me.angrypostman.freeforall.data;

import me.angrypostman.freeforall.user.User;

import java.util.UUID;

public abstract class DataStorage {

    public abstract void initialize();

    public abstract void close();

    public abstract User createUser(UUID uuid, String playerName);

    public abstract User loadUser(UUID uuid);

    public abstract User loadUser(String lookupName);

    public abstract void saveUser(User user);

}
