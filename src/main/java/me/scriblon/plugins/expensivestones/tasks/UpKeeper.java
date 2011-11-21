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
import org.bukkit.Material;
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
        System.out.println("Now keep up!");
        // Check if field should be disabled
        if(field.isDisabled()){
            // Check if field was in disabled list
            if(ExpensiveStones.getInstance().getESFieldManager().isInDisabled(field.getField().getId())){
                ExpensiveStones.getInstance().getESFieldManager().disableField(field);
                ExpensiveStones.infoLog("(UpKeeper) field was still on enabled list! On ID: " + field.getField().getId());
            }
            this.stopMe();
            field.setSignToOff();
            return;
        }
        // Check if chest is still there
        if(!field.getChestLocation().getBlock().getType().equals(Material.CHEST)){
            field.setError();
            ExpensiveStones.getInstance().getESFieldManager().setDormantField(field);
            return;
        }
        
        // Check chest for required content
        if(field.chestHasReqContent()){
            field.doUpkeeping();
        }else{
            //Disable Field
            ExpensiveStones.getInstance().getESFieldManager().disableField(field);
            //Change sign
            field.setSignToDepleted();
            //Cancel task 
            System.out.println("(Upkeeper) Now canceling job for id: " + field.getField().getId());
            if(iD != -1)
                this.stopMe();
            else
                field.setError();
        }
        
    }
    
    public long scheduleMe(){
        iD = this.scheduler.scheduleSyncRepeatingTask(plugin, this, 1L, field.getSettings().getUpkeepPeriod());
        return iD;
    }
    
    public long scheduleMeFreeTick(){
        iD = this.scheduler.scheduleSyncRepeatingTask(plugin, this, 
                field.getSettings().getUpkeepPeriod(), field.getSettings().getUpkeepPeriod());
        return iD;
    }
    
    public void stopMe(){
        this.scheduler.cancelTask(iD);
        ExpensiveStones.getInstance().getESFieldManager().removeTask(iD, field);
    }
}
