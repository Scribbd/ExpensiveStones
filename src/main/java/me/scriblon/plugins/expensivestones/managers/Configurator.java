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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.scriblon.plugins.expensivestones.ESFieldSettings;
import me.scriblon.plugins.expensivestones.ExpensiveField;
import me.scriblon.plugins.expensivestones.ExpensiveStones;
import me.scriblon.plugins.expensivestones.tasks.UpKeeper;
import me.scriblon.plugins.expensivestones.utils.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager;
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;

/**
 * Class dedicated to configure PreciousStone on startup.
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class Configurator {
    
    private PluginManager pm;
    private PreciousStones stones;
    private ExpensiveStones plugin;
    
    private Logger log;
    private ESStorageManager storageManager;
    private ESFieldManager fieldManager;
    private SettingsManager psSettings;
    
    public Configurator(PluginManager pm){
        this.pm = pm;
        log = ExpensiveStones.getLogger();
        // ES Fields
        plugin = ExpensiveStones.getInstance();
        storageManager = plugin.getESStorageManager();
        fieldManager = plugin.getESFieldManager();
        // PS Fields
        stones = PreciousStones.getInstance();
        psSettings = stones.getSettingsManager();
    }
    
    /**
     * Main method to configure ExpensiveStones
     * Loading progress
     * PS-Load>ES-check>ES-modify>ES-Schedule
     */
    public void configureStones(){
        //Check and add if tables are present
        if(!storageManager.dbHasExpensive()){
            logInfo("Adding ExpenisveStone Table");
            storageManager.addExpensiveTableToDatabase();
        }else{
            logInfo("ExpensiveStone Table pressent");
        }
        //Get data from ExpensiveStones-table to match other tables
        
        Map<Integer, ESFieldSettings> settings = Collections.synchronizedMap(this.getFieldSettings());
        // Get Fields
        
        
        //Schedule Tasks
        for(ExpensiveField field : fields){
            UpKeeper keeper = new UpKeeper(field);
            keeper.scheduleThis();
        }
    }
    
    public Map<Integer, ESFieldSettings> getFieldSettings(){
        Map<Integer, ESFieldSettings> settings = new LinkedHashMap<Integer, ESFieldSettings>();
        List<LinkedHashMap<String, Object>> forceFieldStones = psSettings.getForceFieldBlocks();
        for(LinkedHashMap<String, Object> stone : forceFieldStones){
            if(stone.containsKey("block") && stone.containsKey("ExpensiveField")){
                if(psSettings.isFieldType((Integer) stone.get("block")) &&  Helper.convertBoolean(stone.get("ExpensiveField"))){
                    int id; 
                    String name; 
                    Material material; 
                    int amount; 
                    Long upkeepPeriod;
                                        //TODO Finish this
                    ESFieldSettings fieldSetting = new ESFieldSettings(id, name, material, amount, upkeepPeriod);
                    settings.put((Integer) stone.get("block"), fieldSetting);
                }
            }
        }
        return settings;
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
