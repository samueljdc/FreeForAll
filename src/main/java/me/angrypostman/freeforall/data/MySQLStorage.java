package me.angrypostman.freeforall.data;

import com.google.common.base.Preconditions;

import com.zaxxer.hikari.HikariDataSource;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserData;
import me.angrypostman.freeforall.user.UserCache;

import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MySQLStorage extends DataStorage{

    private static final int PAGE_ROWS=10;
    private FreeForAll plugin=null;
    private HikariDataSource dataSource=null;
    private String host=null;
    private String database=null;
    private String username=null;
    private String password=null;
    private int port=0;
    private List<Location> locations;

    public MySQLStorage(FreeForAll plugin, String host, String database, String username, String password, int port){
        this.host=host;
        this.database=database;
        this.username=username;
        this.password=password;
        this.port=port;
        this.locations=new ArrayList<>();
        this.plugin=plugin;
    }

    @Override
    public boolean initialize(){

        Preconditions.checkArgument(!isLoaded(), "cannot start data storage as data storage is already initialized");

        plugin.getLogger().info("Initializing database connection pool...");

        dataSource=new HikariDataSource();

        String jdbcUrl="jdbc:mysql://" + host + ":" + port + "/" + database;
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(30);
        dataSource.setConnectionTimeout(TimeUnit.SECONDS.toMillis(5));

        plugin.getLogger().info("Attempting to connect to " + jdbcUrl + "@" + username + "...");

        Connection connection=null;
        PreparedStatement statement=null;
        ResultSet set=null;
        try{
            connection=getConnection();
            DatabaseMetaData databaseMeta=connection.getMetaData();

            plugin.getLogger().info("A connection was successfully established to MySQL, validating tables...");

            String table="ffa_player_data";
            if(!databaseMeta.getTables(null, null, table, null).next()){

                plugin.getLogger().info("Table `" + table + "` not found, creating it...");
                String values="`playerId` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY," + "`playerUUID` VARCHAR(36) NOT NULL UNIQUE INDEX," +
                        //cannot enforce uniqueness on playerName and lookupName
                        "`playerName` VARCHAR(16) NOT NULL INDEX," + "`lookupName` VARCHAR(16) NOT NULL INDEX, " + "`points` INT(11) NOT NULL DEFAULT '0'," + "`kills` INT(11) NOT NULL DEFAULT '0'," + "`deaths` INT(11) NOT NULL DEFAULT '0'";
                String query="CREATE TABLE `" + table + "`(" + values + ");";
                statement=connection.prepareStatement(query);
                statement.executeUpdate();

                plugin.getLogger().info("Table `" + table + "` has been created!");
            } else{
                plugin.getLogger().info("Found table `" + table + "`!");
            }

            table="ffa_locations";
            if(!databaseMeta.getTables(null, null, table, null).next()){

                plugin.getLogger().info("Table `" + table + "` not found, creating it...");
                String values="`locationId` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY," + "`world` TEXT NOT NULL, " + "`locationX` DOUBLE NOT NULL DEFAULT '0.0'," + "`locationY` DOUBLE NOT NULL DEFAULT '0.0'," + "`locationZ` DOUBLE NOT NULL DEFAULT '0.0'," + "`locationPitch` FLOAT NOT NULL DEFAULT '0'," + "`locationYaw` FLOAT NOT NULL DEFAULT '0'";
                String query="CREATE TABLE `" + table + "`(" + values + ");";
                statement=connection.prepareStatement(query);
                statement.executeUpdate();

                plugin.getLogger().info("Table `" + table + "` has been created!");
            } else{
                plugin.getLogger().info("Found table `" + table + "`!");

                //Table exists so query the table for existing data
                List<Location> locations=new ArrayList<>();

                String query="SELECT * FROM `ffa_locations`;";
                statement=connection.prepareStatement(query);
                set=statement.executeQuery();

                while(set.next()){

                    String world=set.getString("world");
                    World bukkitWorld=plugin.getServer().getWorld(world);

                    if(bukkitWorld == null) throw new IllegalArgumentException("Unknown world '" + world + "'");

                    double locationX=set.getDouble("locationX");
                    double locationY=set.getDouble("locationY");
                    double locationZ=set.getDouble("locationZ");
                    float locationPitch=set.getFloat("locationPitch");
                    float locationYaw=set.getFloat("locationYaw");

                    Location location=new Location(bukkitWorld, locationX, locationY, locationZ, locationPitch, locationYaw);
                    locations.add(location);

                }

                this.locations.addAll(locations);

            }

        } catch(SQLException ex){
            plugin.getLogger().info("An error occurred whilst validating MySQL tables.");
            plugin.getLogger().info("Message: " + ex.getMessage());
            return false;
        } finally{

            if(set != null){
                try{
                    set.close();
                } catch(SQLException ignored){
                }
            }

            if(statement != null){
                try{
                    statement.close();
                } catch(SQLException ignored){
                }
            }

            if(connection != null){
                try{
                    connection.close();
                } catch(SQLException ignored){
                }
            }

        }

        return true;
    }

    @Override
    public void close(){

        Preconditions.checkArgument(isLoaded(), "cannot shutdown data storage as data storage is not initialized");

        plugin.getLogger().info("Performing DataStorage shutdown...");

        plugin.getLogger().info("Saving user data of " + plugin.getServer().getOnlinePlayers().size() + " players...");
        Iterator<User> iterator=UserCache.getUsers().iterator();
        while(iterator.hasNext()){
            User user=iterator.next();
            saveUser(user);
            iterator.remove();
        }

        this.locations.clear();

        plugin.getLogger().info("Shutting down connection pool...");
        if(dataSource != null && !dataSource.isClosed()){
            dataSource.close();
        }
        plugin.getLogger().info("DataStorage shutdown complete!");
    }

    @Override
    public Optional<User> createUser(UUID playerUUID, String playerName){

        Preconditions.checkNotNull(playerUUID, "uuid cannot be null");
        Preconditions.checkArgument(playerName != null && !playerName.isEmpty(), "player name cannot be null or effectively null");

        Connection connection=null;
        PreparedStatement statement=null;
        ResultSet set=null;
        try{

            connection=getConnection();

            String query="INSERT INTO `ffa_player_data`(`playerUUID`, `playerName`, `lookupName`) VALUES(?, ?, ?);";

            statement=connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.setString(3, playerName.toLowerCase());

            statement.executeUpdate();

            set=statement.getGeneratedKeys();

            if(!set.next()) throw new SQLException("failed to retrieve generated keys from result set");

            return Optional.of(new User(set.getInt(1), playerUUID, playerName)); //defaults for everything else
        } catch(SQLException ex){
            plugin.getLogger().info("An error occurred whilst attempting to create database record for '" + playerName + "'");
            plugin.getLogger().info("Message: " + ex.getMessage());
        } finally{

            if(set != null){
                try{
                    set.close();
                } catch(SQLException ignored){
                }
            }

            if(statement != null){
                try{
                    statement.close();
                } catch(SQLException ignored){
                }
            }

            if(connection != null){
                try{
                    connection.close();
                } catch(SQLException ignored){
                }
            }

        }

        return Optional.empty();
    }

    @Override
    public Optional<User> loadUser(UUID uuid){

        Preconditions.checkNotNull(uuid, "uuid cannot be null");

        //If user is in cache, refer to the cache for the data instead as the data should never be different
        Optional<User> tempUser=UserCache.getUserIfPresent(uuid);
        if(tempUser.isPresent()) return tempUser;

        Connection connection=null;
        PreparedStatement statement=null;
        ResultSet set=null;
        try{

            connection=getConnection();

            String query="SELECT * FROM `ffa_player_data` WHERE `playerUUID`=? LIMIT 1;";

            statement=connection.prepareStatement(query);
            statement.setString(1, uuid.toString());

            set=statement.executeQuery();
            if(set.next()){

                int playerId=set.getInt("playerId");
                String playerName=set.getString("playerName");
                int points=set.getInt("points");
                int kills=set.getInt("kills");
                int deaths=set.getInt("deaths");

                return Optional.of(new User(playerId, uuid, playerName, points, kills, deaths));
            }

        } catch(SQLException ex){
            plugin.getLogger().info("An error occurred whilst loading user data for '" + uuid + "'");
            plugin.getLogger().info("Message: " + ex.getMessage());
        } finally{

            if(set != null){
                try{
                    set.close();
                } catch(SQLException ignored){
                }
            }

            if(statement != null){
                try{
                    statement.close();
                } catch(SQLException ignored){
                }
            }

            if(connection != null){
                try{
                    connection.close();
                } catch(SQLException ignored){
                }
            }

        }

        return Optional.empty();
    }

    @Override
    public Optional<User> loadUser(String lookupName){

        Preconditions.checkArgument(lookupName != null && !lookupName.isEmpty(), "lookupName cannot be null or effectively null");

        //If user is in cache, refer to the cache for the data instead as the data should never be different
        Optional<User> tempUser=UserCache.getUserIfPresent(lookupName);
        if(tempUser.isPresent()) return tempUser;

        Connection connection=null;
        PreparedStatement statement=null;
        ResultSet set=null;
        try{

            connection=getConnection();

            String query="SELECT * FROM `ffa_player_data` WHERE `lookupName`=? LIMIT 1;";

            statement=connection.prepareStatement(query);
            statement.setString(1, lookupName.toLowerCase());

            set=statement.executeQuery();
            if(set.next()){
                int playerId=set.getInt("playerId");
                UUID playerUUID=UUID.fromString(set.getString("playerUUID"));
                String playerName=set.getString("playerName");
                int points=set.getInt("points");
                int kills=set.getInt("kills");
                int deaths=set.getInt("deaths");

                return Optional.of(new User(playerId, playerUUID, playerName, points, kills, deaths));
            }

        } catch(SQLException ex){
            plugin.getLogger().info("An error occurred whilst loading user data for '" + lookupName + "'");
            plugin.getLogger().info("Message: " + ex.getMessage());
        } finally{

            if(set != null){
                try{
                    set.close();
                } catch(SQLException ignored){
                }
            }

            if(statement != null){
                try{
                    statement.close();
                } catch(SQLException ignored){
                }
            }

            if(connection != null){
                try{
                    connection.close();
                } catch(SQLException ignored){
                }
            }

        }

        return Optional.empty();
    }

    @Override
    public void saveUser(User user){

        Preconditions.checkNotNull(user, "user cannot be null");

        Connection connection=null;
        PreparedStatement statement=null;
        try{

            connection=getConnection();

            String query="UPDATE `ffa_player_data` SET `playerName`=?, `lookupName`=?, " + "`points`=?, `kills`=?," + "`deaths`=? WHERE `playerUUID`=?;";

            UserData data=user.getUserData();

            statement=connection.prepareStatement(query);
            statement.setString(1, user.getName());
            statement.setString(2, user.getLookupName());
            statement.setInt(3, data.getPoints().getValue());
            statement.setInt(4, data.getKills().getValue());
            statement.setInt(5, data.getDeaths().getValue());
            statement.setString(6, user.getUniqueId().toString());

            statement.executeUpdate();
        } catch(SQLException ex){
            plugin.getLogger().info("An error occurred whilst saving user data for '" + user.getName() + "'");
            plugin.getLogger().info("Message: " + ex.getMessage());
        } finally{

            if(statement != null){
                try{
                    statement.close();
                } catch(SQLException ignored){
                }
            }

            if(connection != null){
                try{
                    connection.close();
                } catch(SQLException ignored){
                }
            }

        }

    }

    @Override
    public List<User> getLeaderboardTop(int page){

        List<User> leaderboard=new ArrayList<>();

        Connection connection=null;
        PreparedStatement statement=null;
        ResultSet set=null;
        try{

            connection=getConnection();

            String query="SELECT `playerUUID` FROM `ffa_player_data` ORDER BY `points` DESC LIMIT " + ((page - 1) * PAGE_ROWS) + ", " + PAGE_ROWS;
            statement=connection.prepareStatement(query);
            set=statement.executeQuery();

            while(set.next()){

                UUID playerUUID=UUID.fromString(set.getString("playerUUID"));
                User user=loadUser(playerUUID).get(); //Never going to be not present
                leaderboard.add(user);

            }

        } catch(SQLException ex){
            plugin.getLogger().info("An error occurred whilst retrieving leaderboard information");
            plugin.getLogger().info("Message: " + ex.getMessage());
        } finally{

            if(set != null){
                try{
                    set.close();
                } catch(SQLException ignored){
                }
            }

            if(statement != null){
                try{
                    statement.close();
                } catch(SQLException ignored){
                }
            }

            if(connection != null){
                try{
                    connection.close();
                } catch(SQLException ignored){
                }
            }

        }

        return leaderboard;
    }

    @Override
    public void saveLocation(Location location){

        Preconditions.checkNotNull(location, "location cannot be null");

        Connection connection=null;
        PreparedStatement statement=null;
        try{

            connection=getConnection();

            Location clone=location.clone();

            String query="INSERT INTO `ffa_locations`(world, locationX, locationY, locationZ, locationPitch, locationYaw) VALUES(?, ?, ?, ?, ?, ?);";
            statement=connection.prepareStatement(query);
            statement.setString(1, clone.getWorld().getName());
            statement.setDouble(2, clone.getX());
            statement.setDouble(3, clone.getY());
            statement.setDouble(4, clone.getZ());
            statement.setFloat(5, clone.getPitch());
            statement.setFloat(6, clone.getYaw());

            statement.executeUpdate();

            this.locations.add(clone);

        } catch(SQLException ex){
            plugin.getLogger().info("An error occurred whilst saving location data");
            plugin.getLogger().info("Message: " + ex.getMessage());
        } finally{

            if(statement != null){
                try{
                    statement.close();
                } catch(SQLException ignored){
                }
            }

            if(connection != null){
                try{
                    connection.close();
                } catch(SQLException ignored){
                }
            }

        }

    }

    @Override
    public void deleteLocation(int spawnId){

        Preconditions.checkArgument(spawnId >= 0 && spawnId <= locations.size(), "invalid spawnId");

        Connection connection=null;
        PreparedStatement statement=null;
        try{

            connection=getConnection();

            Location location=locations.get(spawnId);

            //Probably a better way to do this, but if you're setting spawn locations
            //in the same place, then expect things to break, you're just asking for it
            String query="DELETE FROM `ffa_locations` WHERE `world`=? AND `locationX`=? AND `locationY`=? AND `locationZ`=?";
            statement=connection.prepareStatement(query);
            statement.setString(1, location.getWorld().getName());
            statement.setDouble(2, location.getX());
            statement.setDouble(3, location.getY());
            statement.setDouble(4, location.getZ());

            statement.executeUpdate();

            this.locations.remove(location);

        } catch(SQLException ex){
            plugin.getLogger().info("An error occurred whilst deleting location data");
            plugin.getLogger().info("Message: " + ex.getMessage());
        } finally{

            if(statement != null){
                try{
                    statement.close();
                } catch(SQLException ignored){
                }
            }

            if(connection != null){
                try{
                    connection.close();
                } catch(SQLException ignored){
                }
            }

        }

    }

    @Override
    public List<Location> getLocations(){
        return locations;
    }

    @Override
    public boolean isLoaded(){
        return dataSource != null && !dataSource.isClosed();
    }

    private Connection getConnection() throws SQLException{
        Preconditions.checkArgument(dataSource != null && !dataSource.isClosed(), "data source must be initialized first");
        return dataSource.getConnection();
    }

    private String getHost(){
        return host;
    }

    private int getPort(){
        return port;
    }

    private String getDatabase(){
        return database;
    }

    private String getUsername(){
        return username;
    }

    private String getPassword(){
        return password;
    }
}
