package me.dkflab.intime.listeners;

import me.dkflab.intime.InTime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private InTime main;
    public JoinListener(InTime main) {
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this,main);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (main.getTimeInSecondsPlayer(e.getPlayer()) <= 0) {
            e.getPlayer().kickPlayer("You have run out of time." +
                    " Get another player to resurrect you.");
        }
    }
}
