package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.*;
import me.angrypostman.freeforall.util.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.Optional;

import static me.angrypostman.freeforall.FreeForAll.doSyncLater;

public class PlayerDeathListener implements Listener {

    private FreeForAll plugin = null;
    private Configuration configuration = null;
    private DataStorage storage = null;

    public PlayerDeathListener(FreeForAll plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfiguration();
        this.storage = plugin.getDataStorage();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        event.setDeathMessage(null);
        event.setKeepInventory(false);
        event.setKeepLevel(false);
        event.setNewExp(0);
        event.setNewLevel(0);
        event.setNewTotalExp(0);
        event.setDroppedExp(0);

        Player player = event.getEntity();
        Optional<User> optional = UserManager.getUserIfPresent(player);

        PlayerInventory inventory = player.getInventory();
        inventory.setArmorContents(null);
        inventory.clear();

        if (!optional.isPresent()) { //This should NEVER happen
            player.kickPlayer(ChatColor.RED + "Failed to load player data, please relog");
            return;
        }

        User user = optional.get();
        UserData userData = user.getUserData();

        User killer = null;

        String deathMessage = ChatColor.RED+player.getName()+" has been killed.";
        if (Combat.inCombat(user)) {

            Damage damage = Combat.getLastDamage(user);
            killer = damage.getDamager();

            UserData killerData = killer.getUserData();

            int playerPoints = userData.getPoints();

            int gained = configuration.getGainedLost();
            int lost = (playerPoints - gained < 0 ? playerPoints : gained);

            killerData.addPoints(gained);
            userData.subtractPoints(lost);

            Player killerPlayer = killer.getBukkitPlayer();
            player.sendMessage(ChatColor.RED+"You were killed by "+killerPlayer.getName()+" and lost "+lost+" points.");
            killerPlayer.sendMessage(ChatColor.GREEN+"You killed "+player.getName()+" and gained "+gained+" points.");

            deathMessage = ChatColor.RED+player.getName()+" has been slain by "+killerPlayer.getName();
            if (userData.hasKillStreak() && userData.getKillStreak() > 3) {
                deathMessage = ChatColor.RED+player.getName()+"'s kill streak was brought to a fatal end by "+killerPlayer.getName();
                userData.endStreak();
            }

            killerData.addKill();

            Combat.setLastDamage(user, null);

        }

        userData.addDeath();

        World world = player.getWorld();

        event.setDeathMessage(deathMessage);

        doSyncLater(() -> {

            player.spigot().respawn();
            player.setGameMode(GameMode.SURVIVAL);
            player.setFoodLevel(20);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            player.setInvulnerable(false);
            player.setFireTicks(0);
            player.setLevel(0);
            player.setExp(0);
            player.setTotalExperience(0);

            //Doing it 5 ticks later seems to make the player properly reset
        }, 5L);

    }

}
