package me.angrypostman.freeforall.data;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.util.FileUtils;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class YamlStorage extends DataStorage{

    public YamlStorage(final FreeForAll plugin,
                       final File file){
        Preconditions.checkNotNull(file, "file");
        Preconditions.checkArgument(FileUtils.getFileExtension(file)
                                             .equalsIgnoreCase("yml"), "file type must be a YML file");
        this.file=file;
        this.locations=new ArrayList<>();
        this.plugin=plugin;
    }

    @Override
    public boolean initialize(){

        this.config=new YamlConfiguration();

        if(!getFile().exists()){
            try{
                if(this.file.getParentFile()!=null){
                    this.file.getParentFile()
                             .mkdirs();
                }
                this.file.createNewFile();
            }catch(final IOException e){
                e.printStackTrace();
            }
        }

        try{
            this.config.load(this.file);
        }catch(InvalidConfigurationException | IOException e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void close(){

    }

    @Override
    public Optional<User> createUser(final UUID uuid,
                                     final String playerName){
        return Optional.empty();
    }

    @Override
    public Optional<User> loadUser(final UUID uuid){
        return Optional.empty();
    }

    @Override
    public Optional<User> loadUser(final String lookupName){
        return Optional.empty();
    }

    @Override
    public void saveUser(final User user){

    }

    @Override
    public List<User> getLeaderboardTop(final int page){
        return null;
    }

    @Override
    public void saveLocation(final Location location){

    }

    @Override
    public void deleteLocation(final int spawnId){

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
        return this.file;
    }

    public FileConfiguration getConfig(){
        return this.config;
    }

    private FreeForAll plugin=null;
    private File file=null;
    private FileConfiguration config=null;
    private List<Location> locations=null;
}
