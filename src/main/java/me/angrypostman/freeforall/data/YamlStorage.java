package me.angrypostman.freeforall.data;

import com.google.common.base.Preconditions;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.util.FileUtils;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
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
    public void initialize() {

        configuration = new YamlConfiguration();

        if (!getFile().exists()) {
            try {
                getFile().createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            getConfiguration().load(getFile());
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }

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

    public FileConfiguration getConfiguration() {
        return configuration;
    }
}
