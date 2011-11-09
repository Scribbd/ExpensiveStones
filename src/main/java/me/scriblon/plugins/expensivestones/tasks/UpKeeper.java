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
package me.scriblon.plugins.expensivestones.tasks;

import me.scriblon.plugins.expensivestones.ExpensiveField;
import me.scriblon.plugins.expensivestones.ExpensiveStones;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 *
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class UpKeeper implements Runnable{
    
    private JavaPlugin plugin;
    private ExpensiveField field;
    private BukkitScheduler scheduler;
    
    public UpKeeper(JavaPlugin plugin, ExpensiveField field, BukkitScheduler scheduler){
        this.field = field;
        this.scheduler = scheduler;
        this.plugin = plugin;
    }
    
    public void run() {
        // Check chest for required content
        if(field.chestHasReqContent()){
            //Substract if available
            field.getChest().getInventory().remove(field.getUpkeepStack());
            //scheduler.scheduleSyncDelayedTask(ExpensiveStones.getInstance(), new UpKeeper(field, scheduler), field.getUpkeepPeriod());
        }else{
            
        }
        
    }
    
}
