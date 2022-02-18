package me.dkflab.intime;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Utils {

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&',s);
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static void error(CommandSender sender, String error) {
        sendMessage(sender,"&c&lError! &7" + error);
    }

    public static void info(CommandSender sender, String info) {
        sendMessage(sender, "&b&l[!] &7" + info);
    }

    public static void notPlayer(CommandSender s) {
        error(s, "You need to be a player to execute that command!");
    }

    public static boolean parseInt(CommandSender sender, String parse) {
        try {
            Integer.parseInt(parse);
        } catch (NumberFormatException e) {
            error(sender, parse + " is not an integer!");
            return false;
        }
        return true;
    }

    public static void success(CommandSender sender, String message) {
        sendMessage(sender, "&a&lSuccess! &7" + message);
    }

    public static void noPerms(CommandSender sender) {
        error(sender, "Insufficient permissions.");
    }
}
