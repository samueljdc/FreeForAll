package me.angrypostman.freeforall.listeners;

import java.util.Optional;
import me.angrypostman.freeforall.FreeForAll;
import me.angrypostman.freeforall.data.DataStorage;
import me.angrypostman.freeforall.user.*;
import me.angrypostman.freeforall.util.Configuration;
import me.angrypostman.freeforall.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static me.angrypostman.freeforall.FreeForAll.doAsync;

public class PlayerQuitListener implements Listener{

    public PlayerQuitListener(final FreeForAll plugin){
        this.plugin=plugin;
        this.configuration=plugin.getConfiguration();
        this.storage=plugin.getDataStorage();
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event){

        final Player player=event.getPlayer();
        final Optional<User> optional=UserCache.getUserIfPresent(player);

        if(!optional.isPresent()){ return; }

        final User user=optional.get();
        final UserData userData=user.getUserData();

        String quitMessage=Message.get("quit-message-broadcast")
                                  .replace("%player%", user.getName())
                                  .getContent();

        if(user.isSpectating()){
            Bukkit.getOnlinePlayers()
                  .forEach(online->{
                      if(!online.canSee(player)){
                          online.showPlayer(player);
                      }
                  });

            player.setAllowFlight(false);
            player.setFlying(false);

            UserCache.setSpectating(user, false);
        }

        if(Combat.inCombat(user)){

            final Damage damage=Combat.getLastDamage(user);
            final User attacker=damage.getDamager();
            final UserData attackerData=attacker.getUserData();

            final int playerPoints=userData.getPoints()
                                           .getValue();

            int gained=0;

            String gainedLost=this.configuration.getGainedLost();
            if(gainedLost.endsWith("%")){
                gainedLost=gainedLost.substring(0, gainedLost.length()-1);

                final float percentage=Float.parseFloat(gainedLost)/100;
                gained=Math.round(userData.getPoints()
                                          .getValue()*percentage);
            }else{ gained=Integer.parseInt(gainedLost); }

            if(gained<=0){ gained=5; }

            final int lost=(playerPoints-gained<0 ? playerPoints : gained);

            attackerData.addPoints(gained);
            attackerData.addKill();

            userData.subtractPoints(lost);
            userData.addDeath();
            userData.endStreak();

            Message.get("player-killed-private-message")
                   .replace("%player%", user.getName())
                   .replace("%gainedPoints%", gained)
                   .send(attacker.getBukkitPlayer());

            quitMessage=Message.get("quit-message-combat-log-broadcast")
                               .replace("%player%", user.getName())
                               .getContent();

            Combat.setLastDamage(user, null);
        }

        event.setQuitMessage(quitMessage);

        doAsync(()->{
            this.storage.saveUser(user);
            UserCache.expireUser(user);
        });
    }

    private FreeForAll plugin=null;
    private Configuration configuration=null;
    private DataStorage storage=null;
}
