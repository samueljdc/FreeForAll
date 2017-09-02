package me.angrypostman.freeforall.listeners;

import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.*;
import me.angrypostman.freeforall.util.Configuration;
import me.angrypostman.freeforall.util.Message;
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

public class PlayerDeathListener implements Listener{

    private FreeForAll plugin=null;
    private Configuration configuration=null;
    private DataStorage storage=null;
    public PlayerDeathListener(FreeForAll plugin){
        this.plugin=plugin;
        this.configuration=plugin.getConfiguration();
        this.storage=plugin.getDataStorage();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){

        event.setDeathMessage(null);
        event.setKeepInventory(false);
        event.setKeepLevel(false);
        event.setNewExp(0);
        event.setNewLevel(0);
        event.setNewTotalExp(0);
        event.setDroppedExp(0);

        Player player=event.getEntity();
        Optional<User> optional=UserCache.getUserIfPresent(player);

        PlayerInventory inventory=player.getInventory();
        inventory.setArmorContents(null);
        inventory.clear();

        if(!optional.isPresent()){ //This should NEVER happen
            player.kickPlayer(ChatColor.RED + "Failed to load player data, please relog");
            return;
        }

        User user=optional.get();
        UserData userData=user.getUserData();

        String deathMessage=Message.get("player-death-message").replace("%player%", user.getName()).getContent();
        if(Combat.inCombat(user)){

            Damage damage=Combat.getLastDamage(user);
            User killer=damage.getDamager();

            UserData killerData=killer.getUserData();

            //Need a better method for calculating gained/lost,
            //Might use optional percentages or something
            int playerPoints=userData.getPoints().getValue();

            int gained=0;
            String gainedLost=configuration.getGainedLost();
            if (gainedLost.endsWith("%")){

                gainedLost=gainedLost.substring(0, gainedLost.length()-1);
                float percentage=Float.parseFloat(gainedLost)/100;
                gained=Math.round(userData.getPoints().getValue()*percentage);

            } else { gained=Integer.parseInt(gainedLost); }

            if(gained <= 0)gained=5;

            int lost=(playerPoints - gained < 0 ? playerPoints : gained);

            killerData.addPoints(gained);
            userData.subtractPoints(lost);

            Message.get("player-death-private-message").replace("%killer%", killer.getName()).replace("%lostPoints%", lost).send(user.getBukkitPlayer());
            Message.get("player-killed-private-message").replace("%player%", user.getName()).replace("%gainedPoints%", gained).send(killer.getBukkitPlayer());

//            user.sendMessage(ChatColor.RED + "You were killed by " + killerPlayer.getName() + " and lost " + lost + " points.");
//            killerPlayer.sendMessage(ChatColor.GREEN + "You killed " + player.getName() + " and gained " + gained + " points.");

            deathMessage=Message.get("player-slain-message").replace("%player%", user.getName()).replace("%killer%", killer.getName()).getContent();
            if(userData.hasKillStreak() && userData.getKillStreak().getValue() > 3){
                deathMessage=Message.get("player-kill-streak-ended-message")
                        .replace("%player%", user.getName()).replace("%killer%", killer.getName())
                        .replace("%killStreak%", userData.getKillStreak()).getContent();

            }

            userData.endStreak();
            killerData.addKill();

            Combat.setLastDamage(user, null);

        }

        userData.addDeath();
        event.setDeathMessage(deathMessage);

        World world=player.getWorld();
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
