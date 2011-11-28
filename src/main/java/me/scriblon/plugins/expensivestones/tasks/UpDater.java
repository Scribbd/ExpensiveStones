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

import me.scriblon.plugins.expensivestones.ExpensiveStones;
import me.scriblon.plugins.expensivestones.managers.ESStorageManager;

import org.bukkit.scheduler.BukkitScheduler;

/**
 * For regular updating the database from the chase
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class UpDater implements Runnable{
    
    private final ESStorageManager storage;
    private final ExpensiveStones plugin;
    private final BukkitScheduler scheduler;
    
    private int iD;
    
    /**
     * Needs storage to function
     * @param storage the ESStorageManager of ExpensiveStones
     */
    public UpDater(){
    	this.plugin = ExpensiveStones.getInstance();
        this.storage = plugin.getESStorageManager();
        this.scheduler = plugin.getServer().getScheduler();
    }
            
    public void run() {
        storage.saveAll();
    }
    
    /**
     * Schedules task with a 1-tick delay.
     */
    public void scheduleMe(){
        iD = scheduler.scheduleSyncRepeatingTask(plugin, this, 300L, 300L);
    }
    
    /**
     * Stops the task by the known iD.
     */
    public void stopMe(){
        scheduler.cancelTask(iD);
    }
    
}
