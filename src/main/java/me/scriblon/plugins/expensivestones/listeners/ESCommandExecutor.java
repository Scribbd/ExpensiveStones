/*
 *Copyright (C) 2011 Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 *This program is free software: you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation, either version 3 of the License, or
 *(at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.scriblon.plugins.expensivestones.listeners;

import me.scriblon.plugins.expensivestones.ExpensiveField;
import me.scriblon.plugins.expensivestones.ExpensiveStones;
import me.scriblon.plugins.expensivestones.managers.ESFieldManager;
import me.scriblon.plugins.expensivestones.managers.ESPermissionManager;
import me.scriblon.plugins.expensivestones.utils.Helper;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Execute commands
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ESCommandExecutor implements CommandExecutor {
    
    public static final String BASE = "ExpensiveStones";
    
    public static final String LABEL_INFO = "info";
    public static final String LABEL_ADMIN = "admin";
    public static final String LABEL_BYPASS = "bypass";
    
    public static final String ARG_POINT = "point";
    public static final String ARG_TOGGLE = "toggle";

    final private ExpensiveStones stones;
    final private ESFieldManager manager;
    final private ESPermissionManager permission;

    public ESCommandExecutor() {
        stones = ExpensiveStones.getInstance();
        manager = stones.getESFieldManager();
        permission = stones.getESPermissionManager();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {        
        if (!(sender instanceof Player)) {
            if (label.equalsIgnoreCase(BASE)) {
                if (args[0].equals("prepareUninstall")) {
                    if (args[1].equals("-a")) {
                        stones.getServer().getScheduler().cancelTasks(stones);
                        ExpensiveStones.infoLog("stopped all tasks related to ExpensiveFields!");
                        stones.getESStorageManager().deïnstallPart(sender);
                        ExpensiveStones.infoLog("Please delete ExpensiveStones.jar to complete the progress before next restart.");
                        stones.getServer().getPluginManager().disablePlugin(stones);
                        return true;
                    }
                }
            }
            ExpensiveStones.infoLog("You should be a player to use the commands.");
            return true;
        }

        Player player = (Player) sender;
        //boolean is for special commands

        if (args[0].equalsIgnoreCase(LABEL_INFO)) {
            final boolean isInfo = player.hasPermission(ESPermissionManager.PERM_INFO);
            if (isInfo) {
                if (args[1].equalsIgnoreCase(ARG_POINT)) {
                    final Block block = player.getTargetBlock(null, 5);
                    if (manager.isKnown(block)) {
                        final ExpensiveField field = manager.getExpensiveField(block);
                        player.sendMessage(getInfo(field));
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Targeted block is not an ExpensiveField!");
                    }
                    return true;

                } else if (Helper.isLong(args[1])) {
                    final long iD = Long.parseLong(args[1]);
                    final ExpensiveField field = manager.getKnownExpensiveFieldsByID().get(iD);
                    if (field != null) {
                        player.sendMessage(getInfo(field));
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Given ID is not an ExpensiveField!");
                    }
                    return true;

                }
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                return true;
            }
        }

        if (args[0].equalsIgnoreCase(LABEL_ADMIN)) {
            final boolean isAdmin = player.hasPermission(ESPermissionManager.PERM_ADMIN);
            if (isAdmin) {
                if (args[1].equalsIgnoreCase(ARG_POINT)) {
                    final Block block = player.getTargetBlock(null, 5);
                    if (manager.isKnown(block)) {
                        final ExpensiveField field = manager.getExpensiveField(block);
                        if(setAdmin(field))
                            player.sendMessage(ChatColor.YELLOW + "Field is now set to Admin-Field.");
                        else
                            player.sendMessage(ChatColor.YELLOW + "Failed to set field to Admin.");
                        return true;
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Targeted block is not an ExpensiveField!");
                    }
                    return true;

                } else if (Helper.isLong(args[1])) {
                    final long iD = Long.parseLong(args[1]);
                    final ExpensiveField field = manager.getKnownExpensiveFieldsByID().get(iD);
                    if (field != null) {
                        if(setAdmin(field))
                            player.sendMessage(ChatColor.YELLOW + "Field is now set to Admin-Field.");
                        else
                            player.sendMessage(ChatColor.YELLOW + "Failed to set field to Admin.");
                        return true;
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Given ID is not an ExpensiveField!");
                    }
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    return true;
                }
            }
        }
        
        if(args[0].equalsIgnoreCase(LABEL_BYPASS) && args.length == 2){
            if(args[1].equalsIgnoreCase(ARG_TOGGLE)){
                final boolean isToggle = player.hasPermission(ESPermissionManager.PERM_BYPASS_TOGGLE);
                if(isToggle){
                    permission.toggleBypass(player);
                    if(permission.bypassResult(player))
                        player.sendMessage(ChatColor.YELLOW + "You are now bypassing ExpensiveStones.");
                    else
                        player.sendMessage(ChatColor.YELLOW + "You disabled the ExpensiveStones bypass.");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                    return true;
                }
            }
        }
        return false;
    }

    private String getInfo(ExpensiveField field) {
        String info = "";
        info = ChatColor.DARK_GREEN + "Info of block: " + ChatColor.YELLOW + field.getField().getId() + "\n"
                + ChatColor.GREEN + "Type: " + ChatColor.YELLOW + field.getField().getType() + "\n"
                + ChatColor.GREEN + "Owner: " + ChatColor.YELLOW + field.getField().getOwner() + "\n"
                + ChatColor.GREEN + "Status: " + ChatColor.YELLOW + field.getStatus();
        return info;
    }

    private boolean setAdmin(ExpensiveField field) {
        if(field.isAdmin())
            return true;
        
        manager.setAdminField(field);
        return field.isAdmin();
    }
}
