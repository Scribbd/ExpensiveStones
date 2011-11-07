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

import com.avaje.ebean.EbeanServer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.plugin.PluginManager;

/**
 * Class dedicated to configure PreciousStone on startup.
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class Configurator {
    
    private PluginManager pm;
    private PreciousStones stones;
    private EbeanServer db;
    private Logger log;
    private ESStorageManager storageManager;
    private ESFieldManager fieldManager;
    
    public Configurator(PluginManager pm, Logger log){
        this.pm = pm;
        stones = PreciousStones.getInstance();
        db = stones.getDatabase();
        this.log = log;
        storageManager = new ESStorageManager(log);
    }
    
    /**
     * Main method to configure ExpensiveStones
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
        
        //Get data from backup-field for immediate processing
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
