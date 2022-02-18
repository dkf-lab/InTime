package me.dkflab.intime;

import me.dkflab.intime.listeners.JoinListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class InTime extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new JoinListener(this);
        new MainCommand(this);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (getTimeInSecondsPlayer(p) <= 0) {
                        p.kickPlayer("You have run out of time." +
                                " Get another player to resurrect you.");
                    }
                    setTimeInSecondsPlayer(p,getTimeInSecondsPlayer(p)-1);
                }
                saveReloadConfig();
            }
        };
        Bukkit.getScheduler().runTaskTimer(this,task,20,20);
    }

    @Override
    public void onDisable() {

    }




    public boolean resetPlayer (String playerName) {
        ConfigurationSection players = getConfig().getConfigurationSection("players");
        if (players != null) {
            for (String key : players.getKeys(false)) {
                if (players.getConfigurationSection(key).getString("name").equalsIgnoreCase(playerName)) {
                    players.set(key,null);
                    saveReloadConfig();
                    return true;
                }
            }
        }
        return false;
    }

    public void addTimeInMinutes(Player p, int time) {
        setTimeInSecondsPlayer(p, getTimeInSecondsPlayer(p)+(time*60));
    }

    public int getPriceInMinutes(Material m) {
        ConfigurationSection sellPrices = getConfig().getConfigurationSection("sell-prices");
        if (sellPrices != null) {
            return sellPrices.getInt(m.name());
        }
        return 0;
    }

    public void setPriceInMinutes(Material m,int priceInMinutes) {
        getConfig().set("sell-prices." + m.name(), priceInMinutes);
        saveReloadConfig();
    }

    public int getTimeInSecondsPlayer(Player p) {
        ConfigurationSection players = getConfig().getConfigurationSection("players");
        if (players != null) {
            for (String key : players.getKeys(false)) {
                if (key.equals(p.getUniqueId().toString())) {
                    ConfigurationSection sec = players.getConfigurationSection(key);
                    return sec.getInt("time");
                }
            }
        }
        int time = getConfig().getInt("default-time-amount")*60;
        setTimeInSecondsPlayer(p, time);
        return time;
    }

    public void setTimeInSecondsPlayer(Player p, int time) {
        ConfigurationSection players = getConfig().getConfigurationSection("players");
        players.set(p.getUniqueId().toString() + ".time", time);
        players.set(p.getUniqueId().toString() + ".name", p.getName());
        if (getTimeInSecondsPlayer(p) > getConfig().getInt("max-time")*60) {
            setTimeInSecondsPlayer(p,getConfig().getInt("max-time")*60);
        }
        saveReloadConfig();
    }

    private void saveReloadConfig() {
        saveConfig();
        reloadConfig();
    }
}
