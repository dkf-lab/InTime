package me.dkflab.intime;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static me.dkflab.intime.Utils.*;

public class MainCommand implements CommandExecutor, TabExecutor {

    private InTime main;
    public MainCommand(InTime main) {
        this.main = main;
        main.getCommand("intime").setExecutor(this);
        main.getCommand("intime").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("intime")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    balance(((Player)sender));
                } else {
                    help(sender);
                }
                return true;
            }

            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("balance")) {
                    if (sender instanceof Player) {
                        balance(((Player)sender));
                    } else {
                        notPlayer(sender);
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("sell")) {
                    if (sender instanceof Player) {
                        Player p = (Player)sender;
                        ItemStack hand = p.getInventory().getItemInMainHand();
                        int price = main.getPriceInMinutes(hand.getType());
                        if (price == 0) {
                            error(sender, "You cannot sell that item.");
                            return true;
                        }
                        hand.setAmount(hand.getAmount()-1);
                        main.addTimeInMinutes(p, price);
                        success(sender, "Sold for &a" + price + " &7minutes.");
                    } else {
                        notPlayer(sender);
                    }
                    return true;
                }
            }

            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("resurrect")) {
                    // resurrect [player]
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        int cost = main.getConfig().getInt("resurrection-cost");
                        if (main.getTimeInSecondsPlayer(p)/60 < cost) {
                            error(sender, "Insufficient balance.");
                            return true;
                        }
                        main.setTimeInSecondsPlayer(p, main.getTimeInSecondsPlayer(p)-(cost*60));
                    }
                    if (main.resetPlayer(args[1])) {
                        success(sender, "&e" + args[1]+"&7 can now join.");
                    } else {
                        error(sender, "&e" + args[1] +"&7 is not a player. Check spelling and try again.");
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("set")) {
                    // set [time]
                    if (!sender.hasPermission("time.admin")) {
                        noPerms(sender);
                        return true;
                    }
                    if (!(sender instanceof Player)) {
                        Utils.notPlayer(sender);
                        return true;
                    }
                    Player p = (Player) sender;
                    if (Utils.parseInt(sender, args[1])) {
                        int time = Integer.parseInt(args[1]);
                        Material item = p.getInventory().getItemInMainHand().getType();
                        main.getConfig().set("sell-prices." + item.name(), time);
                        success(sender, "Added item to config.");
                        main.saveConfig();
                        main.reloadConfig();
                    }
                    return true;
                }
            }

            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("player")) {
                    if (!sender.hasPermission("time.admin")) {
                        noPerms(sender);
                        return true;
                    }
                    // player [player] [time]
                    Player target = null;
                    for (Player all : Bukkit.getOnlinePlayers()) {
                        if (all.getName().equalsIgnoreCase(args[1])) {
                            target = all;
                        }
                    }
                    if (target == null) {
                        error(sender, args[1] + " is not an online player.");
                        return true;
                    }
                    if (Utils.parseInt(sender,args[2])) {
                        int time = Integer.parseInt(args[2]);
                        main.setTimeInSecondsPlayer(target,time*60);
                        success(sender, "Set balance.");
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("pay")) {
                    // pay [player] [amount]
                    if (!(sender instanceof Player)) {
                        Utils.notPlayer(sender);
                        return true;
                    }
                    Player target = null;
                    for (Player all : Bukkit.getOnlinePlayers()) {
                        if (all.getName().equalsIgnoreCase(args[1])) {
                            target = all;
                        }
                    }
                    if (target == null) {
                        error(sender, args[1] + " is not an online player.");
                        return true;
                    }
                    if (Utils.parseInt(sender,args[2])) {
                        int amount = Integer.parseInt(args[2])*60;
                        if (main.getTimeInSecondsPlayer((Player)sender)-amount < 0) {
                            error(sender, "Insufficient funds.");
                            return true;
                        }
                        main.setTimeInSecondsPlayer(target,main.getTimeInSecondsPlayer(target)+amount);
                        main.setTimeInSecondsPlayer(((Player)sender), main.getTimeInSecondsPlayer((Player)sender)-amount);
                        success(sender, "Sent " + target.getName() + " " + amount + " minutes.");
                    }
                    return true;
                }
            }
        }
        help(sender);
        return true;
    }

    private void balance(Player player) {
        info(player, "Your balance is &a" + Math.round(main.getTimeInSecondsPlayer(player)/60)+ " &7minutes.");
    }

    private void help(CommandSender s) {
        info(s, "Help");
        sendMessage(s, "&8/t &ebalance &7- View time balance"); // 1
        sendMessage(s, "&8/t &epay [player] [amount] &7- Pay a player time (amount in minutes)."); // 3
        sendMessage(s, "&8/t &esell &7- Sell the item in your hand"); // 1
        sendMessage(s, "&8/t &eresurrect [player] &7- Resurrect a dead player for a fee of " + main.getConfig().getInt("resurrection-cost") + " minutes."); // 2
        if (s.hasPermission("time.admin")) {
            info(s, "Admin Commands");
            sendMessage(s, "&8/t &eset [time] &7- Set the item in your hand to sell for specified time (minutes)."); // 2
            sendMessage(s, "&8/t &eplayer [player] [time] &7- Manually set time for a player (minutes)."); // 3
        }
    }

    List<String> arguments = new ArrayList<>();
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (arguments.isEmpty()) {
            arguments.add("help");
            arguments.add("balance");
            arguments.add("pay");
            arguments.add("sell");
            arguments.add("resurrect");
            arguments.add("set");
            arguments.add("player");
        }
        List<String> result = new ArrayList<String>();
        if (args.length == 1) {
            for (String a : arguments) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(a);
                }
            }
            return result;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("pay")) {
                result.add("[player]");
                for (Player all : Bukkit.getOnlinePlayers()) {
                    result.add(all.getName());
                }
            }
            if (args[0].equalsIgnoreCase("resurrect")) {
                result.add("[player]");
                for (Player all : Bukkit.getOnlinePlayers()) {
                    result.add(all.getName());
                }
            }
            if (args[0].equalsIgnoreCase("set")) {
                result.add("[time]");
            }
            if (args[0].equalsIgnoreCase("player")) {
                result.add("[player]");
                for (Player all : Bukkit.getOnlinePlayers()) {
                    result.add(all.getName());
                }
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("player")) {
                result.add("[time]");
            }
            if (args[0].equalsIgnoreCase("pay")) {
                result.add("[time]");
            }
        }

        return result;
    }
}
