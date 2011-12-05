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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.scriblon.plugins.expensivestones.ESFieldSettings;
import me.scriblon.plugins.expensivestones.ExpensiveField;
import me.scriblon.plugins.expensivestones.ExpensiveStones;
import me.scriblon.plugins.expensivestones.tasks.UpKeeper;
import me.scriblon.plugins.expensivestones.utils.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldSettings;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Class dedicated to configure PreciousStone on startup.
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class Configurator {
    
    private final PluginManager pm;
    private final PreciousStones stones;
    private final ExpensiveStones plugin;
    
    private final ESStorageManager storageManager;
    private final ESFieldManager fieldManager;
    private final ESPowerManager powerManager;
    private final SettingsManager vanillaSettings;
    
    /**
     * Initiate for execution required.
     * @param pm PluginManager from server
     */
    public Configurator(PluginManager pm){
        this.pm = pm;
        // ES Fields
        plugin = ExpensiveStones.getInstance();
        storageManager = plugin.getESStorageManager();
        fieldManager = plugin.getESFieldManager();
        powerManager = plugin.getESPowerManager();
        // PS Fields
        stones = PreciousStones.getInstance();
        vanillaSettings = stones.getSettingsManager();
    }
    
    /**
     * Main method to configure ExpensiveStones
     * Loading progress:
     * PS-Load>ES-check>ES-modify>ES-Schedule
     */
    public void configureStones(){
        //Check and add if tables are present
        if(!storageManager.dbHasExpensive()){
            ExpensiveStones.infoLog("Adding ExpenisveStone Table");
            storageManager.addExpensiveTableToDatabase();
        }else{
            ExpensiveStones.infoLog("ExpensiveStone Table pressent");
        }
        //Get data from ExpensiveStones-table to match other tables
        Map<Integer, ESFieldSettings> settings = Collections.synchronizedMap(this.getFieldSettings());
        fieldManager.setSettings(settings);
            // Get all ESFieldSettings defined in the PreciousStones config.yml
        addDestinedFieldsToManager();
            // Schedule active Field Tasks
        activateDestinedFields();
        
    }
    
    /**
     * Get the map of ItemIds/TypeIds combined with its corresponding ExpensiveFieldSettings.
     * @return Map with TypeId as keys and FieldSettings as values.
     */
    public Map<Integer, ESFieldSettings> getFieldSettings(){
        System.out.println("ExpensiveField starts detecting configured fields.");
        Map<Integer, ESFieldSettings> settings = new LinkedHashMap<Integer, ESFieldSettings>();
        List<LinkedHashMap<String, Object>> forceFieldStones = vanillaSettings.getForceFieldBlocks();
        for(LinkedHashMap<String, Object> stone : forceFieldStones){
            if(stone.containsKey("block") && stone.containsKey("ExpensiveStone")){
                System.out.println("ExpensiveField detected " + stone.get("title") + ": "+ stone.get("ExpensiveStone"));
                if(!Helper.isInteger(stone.get("block")))
                        continue;
                int type = (Integer) stone.get("block");
                if(vanillaSettings.isFieldType(type) &&  Helper.convertBoolean(stone.get("ExpensiveStone"))){
                    FieldSettings vanillaFieldSettings = vanillaSettings.getFieldSettings(type);
                    //Get material
                    Material material = this.extractMaterial(stone); 
                    int amount = this.extractCost(stone); 
                    Long upkeepPeriod = this.extractTime(stone);
                    ESFieldSettings fieldSetting = new ESFieldSettings(vanillaFieldSettings.getTypeId(), vanillaFieldSettings.getTitle(),
                            material, amount, upkeepPeriod);
                    settings.put(vanillaFieldSettings.getTypeId(), fieldSetting);
                }
            }
        }
        return settings;
    }
    
    /**
     * A check if PreciousStones is available if loading queue starts this plugin without checking dependencies.
     * @return true if PreciousStone is available.
     */
    public boolean isPSAvailable(){
        final Plugin testPlugin = pm.getPlugin("PreciousStones");
        if(testPlugin != null)
            return testPlugin.isEnabled();
        return false;
            
    }
    
    private void activateDestinedFields(){
        activateFields(fieldManager.getActiveFields().values());
    }
    
    public void activateFields(Collection<ExpensiveField> fields){
        for(ExpensiveField field : fields){
            if(!field.isAdmin()){
                powerManager.addFieldBlocks(field);
                final UpKeeper keeper = new UpKeeper(field);
                keeper.scheduleMeFreePeriod();
            }
        }
    }
    
    private void addDestinedFieldsToManager(){
        addFieldsToManager(this.getExpensiveFields());
    }
    
    public void addFieldsToManager(List<ExpensiveField> expensiveFields){
        if(expensiveFields == null)
            expensiveFields = Collections.emptyList();
        
        fieldManager.addFields(expensiveFields, false);
        powerManager.addFieldBlocksCollection(fieldManager.getDisabledFields().values());
    }
    
    private Material extractMaterial(LinkedHashMap<String, Object> stone){
        if (stone.containsKey("ExpensiveMaterial")){
            if(Helper.isString(stone.get("ExpensiveMaterial"))){
                return Material.getMaterial((String) stone.get("ExpensiveMaterial"));
            }else if(Helper.isInteger(stone.get("ExpensiveMaterial"))){
                return Material.getMaterial(Integer.parseInt((String) stone.get("ExpensiveMaterial")));
            }
        }
        return ESFieldManager.STANDARD_MATERIAL;
    }
    
    /**
     * Extracts time from PreciousStones settings
     * Subjected to be moved to an util-class
     */
    private Long extractTime(LinkedHashMap<String, Object> stone){
        if(stone.containsKey("ExpensivePeriod")){
            if(Helper.isString(stone.get("ExpensivePeriod"))){
                String period = (String) stone.get("ExpensivePeriod");
                period = period.replaceAll(" ", "");
                if(period.endsWith("L") || period.endsWith("l")){
                    return Long.parseLong(period);
                } else {
                    period = period.replaceAll("[a-zA-Z]", period);
                    return Long.parseLong(period) * 20L;
                }
            }
        }
        return ESFieldManager.STANDARD_PERIOD;
    }
    
    /**
     * Extracts costs from PreciousStones settings
     * Subjected to be moved to an util-class
     */
    private int extractCost(LinkedHashMap<String, Object> stone){
        if(stone.containsKey("ExpensiveCost")){
            if(Helper.isInteger(stone.containsKey("ExpensiveCost")))
                return (Integer) stone.get("ExpensiveCost");
        }
        return ESFieldManager.STANDARD_AMOUNT;
    }
    
    /**
     * Extracts List of ExpensiveFields from the database. world by world.
     */
    private List<ExpensiveField> getExpensiveFields(){
        List<ExpensiveField> output = new ArrayList<ExpensiveField>();
        for(World world : plugin.getServer().getWorlds()){
            List<ExpensiveField> inGet = storageManager.getExpensiveFields(world.getName());
            if(inGet != null && !inGet.isEmpty())
                output.addAll(inGet);
        }
        return output;
    }
}
