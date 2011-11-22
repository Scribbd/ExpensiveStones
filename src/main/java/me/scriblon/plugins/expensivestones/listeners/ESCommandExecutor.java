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

import me.scriblon.plugins.expensivestones.ExpensiveStones;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Execute commands
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ESCommandExecutor implements CommandExecutor{
    
    private ExpensiveStones stones;
    
    public ESCommandExecutor(){
        stones = ExpensiveStones.getInstance();
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            if(!command.getName().equalsIgnoreCase("ExpensiveStones")){
                if(!label.equals("uninstall")){
                    if(!args[0].equals("-a")){
                        stones.getESStorageManager().deïnstallPart(sender);
                        ExpensiveStones.infoLog("SQL error expected, ignore it.");
                        ExpensiveStones.infoLog("Please delete ExpensiveStones.jar to complete the progress before next restart.");
                        stones.getServer().getPluginManager().disablePlugin(stones);
                        
                    }       
                }
            }
        }
                   
        Player player = (Player) sender;
        final boolean isAdmin = player.hasPermission("ExpensiveStones.admin");
        //boolean is for special commands
        
        
        return false;
    }
    
}
