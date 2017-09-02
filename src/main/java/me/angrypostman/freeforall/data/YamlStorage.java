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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class YamlStorage extends DataStorage{

    private FreeForAll plugin=null;
    private File file=null;
    private FileConfiguration config=null;
    private List<Location> locations=null;

    public YamlStorage(FreeForAll plugin, File file){
        Preconditions.checkNotNull(file, "file");
        Preconditions.checkArgument(FileUtils.getFileExtension(file).equalsIgnoreCase("yml"), "file type must be a YML file");
        this.file=file;
        this.locations=new ArrayList<>();
        this.plugin=plugin;
    }

    @Override
    public boolean initialize(){

        config=new YamlConfiguration();

        if(!getFile().exists()){
            try{
                if(file.getParentFile() != null){
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        try{
            config.load(file);
        } catch(InvalidConfigurationException | IOException e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void close(){

    }

    @Override
    public Optional<User> createUser(UUID uuid, String playerName){
        return Optional.empty();
    }

    @Override
    public Optional<User> loadUser(UUID uuid){
        return Optional.empty();
    }

    @Override
    public Optional<User> loadUser(String lookupName){
        return Optional.empty();
    }

    @Override
    public void saveUser(User user){

    }

    @Override
    public List<User> getLeaderboardTop(int page){
        return null;
    }

    @Override
    public void saveLocation(Location location){

    }

    @Override
    public void deleteLocation(int spawnId){

    }

    @Override
    public List<Location> getLocations(){
        return null;
    }

    @Override
    public boolean isLoaded(){
        return false;
    }

    public File getFile(){
        return file;
    }

    public FileConfiguration getConfig(){
        return config;
    }
}
