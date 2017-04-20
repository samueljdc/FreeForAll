package me.angrypostman.freeforall.data;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariDataSource;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.user.User;
import me.angrypostman.freeforall.user.UserManager;
import me.angrypostman.freeforall.util.UUIDFetcher;

import java.sql.*;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MySQLStorage extends DataStorage {

    private FreeForAll plugin = null;
    private HikariDataSource dataSource = null;
    private String host = null;
    private String database = null;
    private String username = null;
    private String password = null;
    private int port = 0;

    public MySQLStorage(FreeForAll plugin, String host, String database, String username, String password, int port) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.port = port;
        this.plugin = plugin;
    }

    private void setupTables() {

        plugin.getLogger().info("Setting up MySQL tables...");

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            DatabaseMetaData databaseMeta = connection.getMetaData();

            String table = "ffa_playerData";
            if (!databaseMeta.getTables(null, null, table, null).next()) {
                plugin.getLogger().info("Table `"+table+"` not found, creating it...");
                String values = "`playerId` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                        "`playerUUID` VARCHAR(36) NOT NULL," +
                        "`playerName` VARCHAR(16) NOT NULL," +
                        "`lookupName` VARCHAR(16) NOT NULL, " +
                        "`points` INT(11) NOT NULL DEFAULT '100'," +
                        "`kills` INT(11) NOT NULL DEFAULT '0'," +
                        "`deaths` INT(11) NOT NULL DEFAULT '0'";
                String query = "CREATE TABLE `"+table+"`("+values+");";
                statement = connection.prepareStatement(query);
                statement.executeQuery();

                plugin.getLogger().info("Table `"+table+"` has been created!");
            } else {
                plugin.getLogger().info("Found table `"+table+"`!");
            }

            if (plugin.getConfiguration().getVersion() < 2.1) {

            }

        } catch (SQLException | IllegalStateException ex) {
            ex.printStackTrace();
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {}
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {}
            }

        }

    }

    @Override
    public void initialize() {

        if (dataSource != null && !dataSource.isClosed()) {
            throw new IllegalArgumentException("DataSource already initialized.");
        }

        plugin.getLogger().info("Initializing connection pool...");

        dataSource = new HikariDataSource();

        String jdbcUrl = "jdbc:mysql://"+getHost()+":"+getPort()+"/"+getDatabase();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(30);
        dataSource.setConnectionTimeout(TimeUnit.SECONDS.toMillis(5));

        setupTables();

    }

    @Override
    public void close() {

        Iterator<User> iterator = UserManager.getUsers().iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            saveUser(user);
            iterator.remove();
        }

        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public User createUser(UUID playerUUID, String playerName) {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;

        try {

            connection = getConnection();

            String query = "INSERT INTO `ffa_playerData`(`playerUUID`, `playerName`, `lookupName`) VALUES(?, ?);";

            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.setString(3, playerName.toLowerCase());

            statement.executeUpdate();

            set = statement.getGeneratedKeys();

            if (!set.next()) throw new IllegalStateException("Failed to retrieve generated keys");

            return new User(set.getInt(1), playerUUID, playerName);
        } catch (SQLException | IllegalStateException ex) {
            ex.printStackTrace();
        } finally {

            if (set != null) {
                try {
                    set.close();
                } catch (SQLException ignored) {}
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {}
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {}
            }

        }

        return null;
    }

    @Override
    public User loadUser(UUID uuid) {

        Preconditions.checkNotNull(uuid, "uuid");

        User tempUser = UserManager.getUser(uuid);
        if (tempUser != null) return tempUser;

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;

        try {

            connection = getConnection();

            String query = "SELECT * FROM `ffa_playerData` WHERE `uuid`=? LIMIT 1;";

            statement = connection.prepareStatement(query);
            statement.setString(1, uuid.toString());

            set = statement.executeQuery();
            if (set.next()) {

                int playerId = set.getInt("playerId");
                String playerName = set.getString("playerName");
                int points = set.getInt("points");
                int kills = set.getInt("kills");
                int deaths = set.getInt("deaths");

                return new User(playerId, uuid, playerName, points, kills, deaths);
            }

        } catch (SQLException | IllegalStateException ex) {
            ex.printStackTrace();
        } finally {

            if (set != null) {
                try {
                    set.close();
                } catch (SQLException ignored) {}
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {}
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {}
            }

        }

        return null;
    }

    @Override
    public User loadUser(String lookupName) {

        Preconditions.checkNotNull(lookupName, "lookupName");
        Preconditions.checkArgument(!lookupName.isEmpty(), "lookupName empty");

        User tempUser = UserManager.getUser(lookupName);
        if (tempUser != null) return tempUser;

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;

        try {

            connection = getConnection();

            String query = "SELECT * FROM `ffa_playerData` WHERE `lookupName`=? LIMIT 1;";

            statement = connection.prepareStatement(query);
            statement.setString(1, lookupName.toLowerCase());

            set = statement.executeQuery();
            if (set.next()) {

                int playerId = set.getInt("playerId");
                UUID playerUUID = UUID.fromString(set.getString("playerUUID"));
                String playerName = set.getString("playerName");
                int points = set.getInt("points");
                int kills = set.getInt("kills");
                int deaths = set.getInt("deaths");

                return new User(playerId, playerUUID, playerName, points, kills, deaths);
            }

        } catch (SQLException | IllegalStateException ex) {
            ex.printStackTrace();
        } finally {

            if (set != null) {
                try {
                    set.close();
                } catch (SQLException ignored) {}
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {}
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {}
            }

        }

        return null;
    }

    @Override
    public void saveUser(User user) {

        Preconditions.checkNotNull(user, "user");

        Connection connection = null;
        PreparedStatement statement = null;

        try {

            connection = getConnection();

            String query = "UPDATE `ffa_playerData` SET `playerName`=?, `lookupName`=?, " +
                    "`points`=?, `kills`=?," +
                    "`deaths`=? WHERE `playerUUID`=?;";

            statement = connection.prepareStatement(query);
            statement.setString(1, user.getName());
            statement.setString(2, user.getLookupName());
            statement.setInt(3, user.getPoints());
            statement.setInt(4, user.getKills());
            statement.setInt(5, user.getDeaths());
            statement.setString(6, user.getPlayerUUID().toString());

            statement.executeUpdate();
        } catch (SQLException | IllegalStateException ex) {
            ex.printStackTrace();
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {}
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {}
            }

        }

    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException("DataSource must be initialized first.");
        }
        return dataSource.getConnection();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
