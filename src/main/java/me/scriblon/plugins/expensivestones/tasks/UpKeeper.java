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
import me.scriblon.plugins.expensivestones.managers.ESFieldManager;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import org.bukkit.Material;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * A task for scheduling Fields for upkeeping.
 * Can cancel itself if subjected chest is either broken or doesn't have the required contents. 
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class UpKeeper implements Runnable{
    
    final private ExpensiveStones plugin;
    final private ExpensiveField field;
    final private BukkitScheduler scheduler;
    
    private int iD = -1;
    
    /**
     * A upkeeper for fields.
     * @param ExpensiveField that gets scheduled.
     */
    public UpKeeper(ExpensiveField field){
        this.field = field;
        plugin = ExpensiveStones.getInstance();
        scheduler = plugin.getServer().getScheduler();
    }
    
    public void run() {
        System.out.println("(Upkeeper) Now keeping up!");
        // Check if field is still known
        if(!PreciousStones.getInstance().getForceFieldManager().isField(field.getField().getBlock())){
            field.setError();
            this.stopMe();
            return;
        }
        
        // Check if field is dormanted
        if(field.isDormant()){
            System.out.println("(Upkeeper) field is dormant disabling myself on id : " + field.getField().getId());
            this.stopMe();
        }
        
        // Check if field should be disabled
        if(field.isDisabled()){
            // Check if field was in disabled list
            if(plugin.getESFieldManager().isInDisabled(field.getField().getId())){
                plugin.getESFieldManager().disableField(field);
                ExpensiveStones.infoLog("(UpKeeper) field was still on enabled list! On ID: " + field.getField().getId());
            }
            this.stopMe();
            field.setSignToOff();
            return;
        }
        
        final ESFieldManager manager = plugin.getESFieldManager();
        
        // Check if chest is still there
        if(!field.getChestLocation().getBlock().getType().equals(Material.CHEST)){
            field.setError();
            manager.setDormantField(field);
            return;
        }
        
        // Check chest for required content
        if(field.chestHasReqContent()){
            field.doUpkeeping();
        }else{
            //Disable Field
            manager.disableField(field);
            //Change sign
            field.setSignToDepleted();
            //Cancel task 
            System.out.println("(Upkeeper) Now canceling job for id: " + field.getField().getId());
            if(iD != -1)
                this.stopMe();
            else
                field.setError();
            //TODO Subject to deletion
            System.out.println("(Upkeeper) Check if field is disabled to ES : " + field.isDisabled());
            System.out.println("(Upkeeper) Check if field is disabled to PS : " + field.getField().isDisabled());
            System.out.println("(Upkeeper) Check if field is still in list : " + PreciousStones.getInstance().getForceFieldManager().isField(field.getField().getBlock()));
        }
        
    }
    
    /**
     * Schedules task with a 1-tick delay.
     * @return long ID of scheduled task (-1 if task couldn't be scheduled)
     */
    public long scheduleMe(){
        iD = this.scheduler.scheduleSyncRepeatingTask(plugin, this, 1L, field.getSettings().getUpkeepPeriod());
        return iD;
    }
    
    /**
     * Schedules tasks with one free period.
     * Subject for change after removal ID-tracker.
     * @return long iD of scheduled task (-1 if task couldn't be scheduled)
     */
    public long scheduleMeFreePeriod(){
        iD = this.scheduler.scheduleSyncRepeatingTask(plugin, this, 
                field.getSettings().getUpkeepPeriod(), field.getSettings().getUpkeepPeriod());
        return iD;
    }
    
    /**
     * Stops the task by the known iD.
     * Subject to change after removal ID-tracker.
     */
    public void stopMe(){
        this.scheduler.cancelTask(iD);
        plugin.getESFieldManager().removeTask(iD, field);
    }
}
