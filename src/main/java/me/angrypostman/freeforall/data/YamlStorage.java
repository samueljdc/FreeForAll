package me.angrypostman.freeforall.data;

import com.google.common.base.Preconditions;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.util.FileUtils;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class YamlStorage extends DataStorage {

    private FreeForAll plugin = null;
    private File file = null;
    private FileConfiguration configuration = null;

    public YamlStorage(FreeForAll plugin, File file) {
        Preconditions.checkNotNull(file, "file");
        Preconditions.checkArgument(FileUtils.getFileExtension(file).equalsIgnoreCase("yml"),
                "file extension must be of type YML");
        this.plugin = plugin;
        this.file = file;
    }

    @Override
    public boolean initialize() {

        configuration = new YamlConfiguration();

        if (!getFile().exists()) {
            try {
                getFile().createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            getConfiguration().load(getFile());
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void close() {

    }

    @Override
    public Optional<User> createUser(UUID uuid, String playerName) {
        return Optional.empty();
    }

    @Override
    public Optional<User> loadUser(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<User> loadUser(String lookupName) {
        return Optional.empty();
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
    public List<Location> getLocations() {
        return null;
    }

    public File getFile() {
        return file;
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }
}
