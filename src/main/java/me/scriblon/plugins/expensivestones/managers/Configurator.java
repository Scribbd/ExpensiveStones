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
package me.scriblon.plugins.expensivestones.managers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.scriblon.plugins.expensivestones.ExpensiveField;
import me.scriblon.plugins.expensivestones.ExpensiveStones;
import me.scriblon.plugins.expensivestones.tasks.UpKeeper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * Class dedicated to configure PreciousStone on startup.
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class Configurator {
    
    private PluginManager pm;
    private BukkitScheduler schedule;
    private PreciousStones stones;
    private ExpensiveStones plugin;
    
    private Logger log;
    private ESStorageManager storageManager;
    private ESFieldManager fieldManager;
    
    public Configurator(PluginManager pm, BukkitScheduler schedule){
        this.pm = pm;
        this.schedule = schedule;
        stones = PreciousStones.getInstance();
        log = ExpensiveStones.getLogger();
        storageManager = new ESStorageManager();
        plugin = ExpensiveStones.getInstance();
    }
    
    /**
     * Main method to configure ExpensiveStones
     * Loading progress
     * PS-Load>ES-check>ES-modify>ES-Schedule
     */
    public void configureStones(){
        //Check and add if tables are present
        if(!storageManager.dbHasBackup()){
            logInfo("Adding Backup-table");
            storageManager.addBackupToDatabase();
        }else{
            logInfo("Backup-table pressent");
        }
        if(!storageManager.dbHasDisabled()){
            logInfo("Adding Disabled Table");
            storageManager.addDisabledToDatabase();
        }else{
            logInfo("Disable-table pressent");
        }
        //Get data from disabled-field to match other tables
        List<ExpensiveField> fields = Collections.synchronizedList(new LinkedList<ExpensiveField>());
        
        //Get data from backup-field for immediate processing
        
        //Schedule Tasks
        for(ExpensiveField field : fields){
            UpKeeper keeper = new UpKeeper(field);
            keeper.scheduleThis();
        }
    }
    
    
    public boolean isPSAvailable(){
        return pm.getPlugin("PreciousStones") == null;
    }
    
    public PreciousStones getPS(){
        return stones;
    }
    
    private void logInfo(String message){
        message = "[ExpensiveStone] " + message;
        log.log(Level.INFO, message);
    }
}
