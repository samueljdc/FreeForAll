package me.angrypostman.freeforall.data;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserCache;
import me.angrypostman.freeforall.user.UserData;
import org.bukkit.Location;
import org.bukkit.World;

public class SQLiteStorage extends DataStorage{

    private static final int PAGE_ROWS=10;

    public SQLiteStorage(final FreeForAll plugin,
                         final File dataFile){
        this.dataFile=dataFile;
        this.locations=new ArrayList<>();
        this.plugin=plugin;
    }

    @Override
    public boolean initialize(){

        Preconditions.checkArgument(!isLoaded(),
                                    "cannot initialize data storage as data storage is already initialized");

        if(!this.dataFile.exists()){
            final File parent=this.dataFile.getParentFile();
            if(parent!=null){ parent.mkdirs(); }
            try{
                this.dataFile.createNewFile();
            }catch(final IOException e){
                this.plugin.getLogger()
                           .log(Level.INFO, "Failed to create data file", e);
                return false;
            }
        }

        this.plugin.getLogger()
                   .info("Initializing database connection pool...");

        try{
            Class.forName("org.sqlite.JDBC");
        }catch(final ClassNotFoundException ex){
            this.plugin.getLogger()
                       .info("Failed to resolve SQLite class name");
            return false;
        }

        final String jdbcUrl="jdbc:sqlite:"+this.dataFile.getPath();
        this.plugin.getLogger()
                   .info("Attempting to connect to "+jdbcUrl+"...");

        PreparedStatement statement=null;
        ResultSet set=null;
        try{
            this.connection=DriverManager.getConnection(jdbcUrl);
            final DatabaseMetaData databaseMeta=this.connection.getMetaData();

            this.plugin.getLogger()
                       .info("A connection was successfully established to SQLite, validating tables...");

            String table="ffa_player_data";
            if(!databaseMeta.getTables(null, null, table, null)
                            .next()){

                this.plugin.getLogger()
                           .info("Table `"+table+"` not found, creating it...");
                final String values="`playerId` INT(11) PRIMARY KEY,"+"`playerUUID` VARCHAR(36) NOT NULL UNIQUE,"+
                                    //cannot enforce uniqueness on playerName and lookupName
                                    "`playerName` VARCHAR(16) NOT NULL,"+"`lookupName` VARCHAR(16) NOT NULL, "+"`points` INT(11) NOT NULL DEFAULT '0',"+"`kills` INT(11) NOT NULL DEFAULT '0',"+"`deaths` INT(11) NOT NULL DEFAULT '0'";
                final String query="CREATE TABLE `"+table+"`("+values+");"+"CREATE INDEX uniqueIdIndex ON `"+table+"` (playerUUID);"+"CREATE INDEX playerIndex ON `"+table+"` (playerName);"+"CREATE INDEX lookupIndex ON `"+table+"` (lookupName);";
                statement=this.connection.prepareStatement(query);
                statement.executeUpdate();

                this.plugin.getLogger()
                           .info("Table `"+table+"` has been created!");
            }else{
                this.plugin.getLogger()
                           .info("Found table `"+table+"`!");
            }

            table="ffa_locations";
            if(!databaseMeta.getTables(null, null, table, null)
                            .next()){

                this.plugin.getLogger()
                           .info("Table `"+table+"` not found, creating it...");
                final String values="`locationId` INT(11) PRIMARY KEY,"+"`world` TEXT NOT NULL, "+"`locationX` DOUBLE NOT NULL DEFAULT '0.0',"+"`locationY` DOUBLE NOT NULL DEFAULT '0.0',"+"`locationZ` DOUBLE NOT NULL DEFAULT '0.0',"+"`locationPitch` FLOAT NOT NULL DEFAULT '0',"+"`locationYaw` FLOAT NOT NULL DEFAULT '0'";
                final String query="CREATE TABLE `"+table+"`("+values+");";
                statement=this.connection.prepareStatement(query);
                statement.executeUpdate();

                this.plugin.getLogger()
                           .info("Table `"+table+"` has been created!");
            }else{
                this.plugin.getLogger()
                           .info("Found table `"+table+"`!");

                //Table exists so query the table for existing data
                final List<Location> locations=new ArrayList<>();

                final String query="SELECT * FROM `ffa_locations`;";
                statement=this.connection.prepareStatement(query);
                set=statement.executeQuery();

                while(set.next()){

                    final String world=set.getString("world");
                    final World bukkitWorld=this.plugin.getServer()
                                                       .getWorld(world);

                    if(bukkitWorld==null){ throw new IllegalArgumentException("Unknown world '"+world+"'"); }

                    final double locationX=set.getDouble("locationX");
                    final double locationY=set.getDouble("locationY");
                    final double locationZ=set.getDouble("locationZ");
                    final float locationPitch=set.getFloat("locationPitch");
                    final float locationYaw=set.getFloat("locationYaw");

                    final Location location=new Location(bukkitWorld, locationX, locationY, locationZ, locationPitch,
                                                         locationYaw);
                    locations.add(location);
                }

                this.locations.addAll(locations);
            }
        }catch(final SQLException ex){
            this.plugin.getLogger()
                       .info("An error occurred whilst validating SQLite tables.");
            this.plugin.getLogger()
                       .info("Message: "+ex.getMessage());
            return false;
        }finally{

            if(set!=null){
                try{
                    set.close();
                }catch(final SQLException ignored){
                }
            }

            if(statement!=null){
                try{
                    statement.close();
                }catch(final SQLException ignored){
                }
            }
        }

        return true;
    }

    @Override
    public void close(){

        Preconditions.checkArgument(isLoaded(), "data storage not initialized");

        this.plugin.getLogger()
                   .info("Performing DataStorage shutdown...");

        this.plugin.getLogger()
                   .info("Saving user data of "+this.plugin.getServer()
                                                           .getOnlinePlayers()
                                                           .size()+" players...");
        for(final User user : UserCache.getUsers()){
            UserCache.expireUser(user);
            saveUser(user);
        }

        this.locations.clear();

        this.plugin.getLogger()
                   .info("Shutting down connections...");
        if(this.connection!=null){
            try{
                if(!this.connection.isClosed()){
                    this.connection.close();
                }
            }catch(final SQLException e){
                e.printStackTrace();
            }
        }

        this.plugin.getLogger()
                   .info("DataStorage shutdown complete!");
    }

    @Override
    public Optional<User> createUser(final UUID playerUUID,
                                     final String playerName){

        Preconditions.checkArgument(isLoaded(), "data storage not initialized");
        Preconditions.checkNotNull(playerUUID, "uuid cannot be null");
        Preconditions.checkArgument(playerName!=null&&!playerName.isEmpty(),
                                    "player name cannot be null or effectively null");

        this.plugin.getLogger()
                   .info("Attempting to create a new database entry for "+playerName+"("+playerUUID+")...");

        Connection connection=null;
        PreparedStatement statement=null;
        ResultSet set=null;
        try{

            connection=getConnection();

            final String query="INSERT INTO `ffa_player_data`(`playerId`, `playerUUID`, `playerName`, `lookupName`) VALUES(NULL, ?, ?, ?);";

            statement=connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.setString(3, playerName.toLowerCase());

            statement.executeUpdate();

            set=statement.getGeneratedKeys();

            if(!set.next()){ throw new SQLException("failed to retrieve generated keys from result set"); }

            return Optional.of(new User(set.getInt(1), playerUUID, playerName)); //defaults for everything else
        }catch(final SQLException ex){
            this.plugin.getLogger()
                       .info("An error occurred whilst attempting to create database record for '"+playerName+"'");
            this.plugin.getLogger()
                       .info("Message: "+ex.getMessage());
        }finally{

            if(set!=null){
                try{
                    set.close();
                }catch(final SQLException ignored){
                }
            }

            if(statement!=null){
                try{
                    statement.close();
                }catch(final SQLException ignored){
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> loadUser(final UUID uuid){

        Preconditions.checkArgument(isLoaded(), "data storage not initialized");
        Preconditions.checkNotNull(uuid, "uuid cannot be null");

        //If user is in cache, refer to the cache for the data instead as the data should never be different
        final Optional<User> tempUser=UserCache.getUserIfPresent(uuid);
        if(tempUser.isPresent()){ return tempUser; }

        this.plugin.getLogger()
                   .info("Loading user data for user "+uuid+"...");

        Connection connection=null;
        PreparedStatement statement=null;
        ResultSet set=null;
        try{

            connection=getConnection();

            final String query="SELECT * FROM `ffa_player_data` WHERE `playerUUID`=? LIMIT 1;";

            statement=connection.prepareStatement(query);
            statement.setString(1, uuid.toString());

            set=statement.executeQuery();
            if(set.next()){

                final int playerId=set.getInt("playerId");
                final String playerName=set.getString("playerName");
                final int points=set.getInt("points");
                final int kills=set.getInt("kills");
                final int deaths=set.getInt("deaths");

                return Optional.of(new User(playerId, uuid, playerName, points, kills, deaths));
            }
        }catch(final SQLException ex){
            this.plugin.getLogger()
                       .info("An error occurred whilst loading user data for '"+uuid+"'");
            this.plugin.getLogger()
                       .info("Message: "+ex.getMessage());
        }finally{

            if(set!=null){
                try{
                    set.close();
                }catch(final SQLException ignored){
                }
            }

            if(statement!=null){
                try{
                    statement.close();
                }catch(final SQLException ignored){
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> loadUser(final String lookupName){

        Preconditions.checkArgument(isLoaded(), "data storage not initialized");
        Preconditions.checkArgument(lookupName!=null&&!lookupName.isEmpty(),
                                    "lookupName cannot be null or effectively null");

        //If user is in cache, refer to the cache for the data instead as the data should never be different
        final Optional<User> tempUser=UserCache.getUserIfPresent(lookupName);
        if(tempUser.isPresent()){ return tempUser; }

        this.plugin.getLogger()
                   .info("Loading user data for "+lookupName+"...");

        Connection connection=null;
        PreparedStatement statement=null;
        ResultSet set=null;
        try{

            connection=getConnection();

            final String query="SELECT * FROM `ffa_player_data` WHERE `lookupName`=? LIMIT 1;";

            statement=connection.prepareStatement(query);
            statement.setString(1, lookupName.toLowerCase());

            set=statement.executeQuery();
            if(set.next()){
                final int playerId=set.getInt("playerId");
                final UUID playerUUID=UUID.fromString(set.getString("playerUUID"));
                final String playerName=set.getString("playerName");
                final int points=set.getInt("points");
                final int kills=set.getInt("kills");
                final int deaths=set.getInt("deaths");

                return Optional.of(new User(playerId, playerUUID, playerName, points, kills, deaths));
            }
        }catch(final SQLException ex){
            this.plugin.getLogger()
                       .info("An error occurred whilst loading user data for '"+lookupName+"'");
            this.plugin.getLogger()
                       .info("Message: "+ex.getMessage());
        }finally{

            if(set!=null){
                try{
                    set.close();
                }catch(final SQLException ignored){
                }
            }

            if(statement!=null){
                try{
                    statement.close();
                }catch(final SQLException ignored){
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void saveUser(final User user){

        Preconditions.checkArgument(isLoaded(), "data storage not initialized");
        Preconditions.checkNotNull(user, "user cannot be null");

        this.plugin.getLogger()
                   .info("Saving user data for "+user.getName()+"("+user.getUniqueId()+")...");

        Connection connection=null;
        PreparedStatement statement=null;
        try{

            connection=getConnection();

            final String query="UPDATE `ffa_player_data` SET `playerName`=?, `lookupName`=?, "+"`points`=?, `kills`=?,"+"`deaths`=? WHERE `playerUUID`=?;";

            final UserData data=user.getUserData();

            statement=connection.prepareStatement(query);
            statement.setString(1, user.getName());
            statement.setString(2, user.getLookupName());
            statement.setInt(3, data.getPoints()
                                    .getValue());
            statement.setInt(4, data.getKills()
                                    .getValue());
            statement.setInt(5, data.getDeaths()
                                    .getValue());
            statement.setString(6, user.getUniqueId()
                                       .toString());

            statement.executeUpdate();
        }catch(final SQLException ex){
            this.plugin.getLogger()
                       .info("An error occurred whilst saving user data for '"+user.getName()+"'");
            this.plugin.getLogger()
                       .info("Message: "+ex.getMessage());
        }finally{

            if(statement!=null){
                try{
                    statement.close();
                }catch(final SQLException ignored){
                }
            }
        }
    }

    @Override
    public List<User> getLeaderboardTop(final int page){

        Preconditions.checkArgument(isLoaded(), "data storage not initialized");
        Preconditions.checkArgument(page>=0, "page cannot be negative");

        final List<User> leaderboard=new ArrayList<>();

        Connection connection=null;
        PreparedStatement statement=null;
        ResultSet set=null;
        try{

            connection=getConnection();

            final String query="SELECT `playerUUID` FROM `ffa_player_data` ORDER BY `points` DESC LIMIT "+((page-1)*PAGE_ROWS)+", "+PAGE_ROWS;
            statement=connection.prepareStatement(query);
            set=statement.executeQuery();

            while(set.next()){

                final UUID playerUUID=UUID.fromString(set.getString("playerUUID"));
                final User user=loadUser(playerUUID).get(); //Never going to be not present
                leaderboard.add(user);
            }
        }catch(final SQLException ex){
            this.plugin.getLogger()
                       .info("An error occurred whilst retrieving leaderboard information");
            this.plugin.getLogger()
                       .info("Message: "+ex.getMessage());
        }finally{

            if(set!=null){
                try{
                    set.close();
                }catch(final SQLException ignored){
                }
            }

            if(statement!=null){
                try{
                    statement.close();
                }catch(final SQLException ignored){
                }
            }
        }

        return leaderboard;
    }

    @Override
    public void saveLocation(final Location location){

        Preconditions.checkArgument(isLoaded(), "data storage not initialized");
        Preconditions.checkNotNull(location, "location cannot be null");

        Connection connection=null;
        PreparedStatement statement=null;
        try{

            connection=getConnection();

            final Location clone=location.clone();

            final String query="INSERT INTO `ffa_locations`(world, locationX, locationY, locationZ, locationPitch, locationYaw) VALUES(?, ?, ?, ?, ?, ?);";
            statement=connection.prepareStatement(query);
            statement.setString(1, clone.getWorld()
                                        .getName());
            statement.setDouble(2, clone.getX());
            statement.setDouble(3, clone.getY());
            statement.setDouble(4, clone.getZ());
            statement.setFloat(5, clone.getPitch());
            statement.setFloat(6, clone.getYaw());

            statement.executeUpdate();

            this.locations.add(clone);
        }catch(final SQLException ex){
            this.plugin.getLogger()
                       .info("An error occurred whilst saving location data");
            this.plugin.getLogger()
                       .info("Message: "+ex.getMessage());
        }finally{

            if(statement!=null){
                try{
                    statement.close();
                }catch(final SQLException ignored){
                }
            }
        }
    }

    @Override
    public void deleteLocation(final int spawnId){

        Preconditions.checkArgument(isLoaded(), "data storage not initialized");
        Preconditions.checkArgument(spawnId>=0&&spawnId<=this.locations.size(), "invalid spawnId");

        Connection connection=null;
        PreparedStatement statement=null;
        try{

            connection=getConnection();

            final Location location=this.locations.get(spawnId);

            //Probably a better way to do this, but if you're setting spawn locations
            //in the same place, then expect things to break, you're just asking for it
            final String query="DELETE FROM `ffa_locations` WHERE `world`=? AND `locationX`=? AND `locationY`=? AND `locationZ`=?";
            statement=connection.prepareStatement(query);
            statement.setString(1, location.getWorld()
                                           .getName());
            statement.setDouble(2, location.getX());
            statement.setDouble(3, location.getY());
            statement.setDouble(4, location.getZ());

            statement.executeUpdate();

            this.locations.remove(location);
        }catch(final SQLException ex){
            this.plugin.getLogger()
                       .info("An error occurred whilst deleting location data");
            this.plugin.getLogger()
                       .info("Message: "+ex.getMessage());
        }finally{

            if(statement!=null){
                try{
                    statement.close();
                }catch(final SQLException ignored){
                }
            }
        }
    }

    @Override
    public List<Location> getLocations(){
        return this.locations;
    }

    @Override
    public boolean isLoaded(){
        try{
            return this.connection!=null&&!this.connection.isClosed();
        }catch(final SQLException ex){
            ex.printStackTrace();
            return false;
        }
    }

    private Connection getConnection() throws
                                       SQLException{
        Preconditions.checkArgument(isLoaded(), "data source must be initialized first");
        return this.connection;
    }

    private FreeForAll plugin=null;
    private final File dataFile;
    private Connection connection;
    private final List<Location> locations;
}
