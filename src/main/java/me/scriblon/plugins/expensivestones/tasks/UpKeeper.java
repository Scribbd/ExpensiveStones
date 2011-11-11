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
import org.bukkit.block.Chest;
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
    private int iD = -1;
    
    public UpKeeper(ExpensiveField field){
        this.field = field;
        plugin = ExpensiveStones.getInstance();
        scheduler = plugin.getServer().getScheduler();
    }
    
    public void run() {
        // Check chest for required content
        if(field.chestHasReqContent()){
            field.doUpkeeping();
        }else{
            //Change sign
            field.setSignToDepleted();
            //Cancel task
            if(iD != -1)
                this.stopThis();
            else
                field.setError();
        }
        
    }
    
    public void scheduleThis(){
        iD = this.scheduler.scheduleSyncRepeatingTask(plugin, this, 0, field.getSettings().getUpkeepPeriod());
    }
    
    public void stopThis(){
        this.scheduler.cancelTask(iD);
    }
}
