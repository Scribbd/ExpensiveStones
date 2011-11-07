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
package me.scriblon.plugins.expensivestones.configurators;

import com.avaje.ebean.EbeanServer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;

/**
 * Class dedicated to configure PreciousStone on startup.
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class Configurator {
    
    private PluginManager pm;
    private PreciousStones stones;
    private EbeanServer db;
    
    public Configurator(PluginManager pm){
        this.pm = pm;
        stones = PreciousStones.getInstance();
        db = stones.getDatabase();
    }
    
    public boolean isPSAvailable(){
        return pm.getPlugin("PreciousStones") == null;
    }
    
    public PreciousStones getPS(){
        return stones;
    }
    
    /**
     * Get List of integers (blockids) which has the expensiveField-tag
     * Might consider using own config.yml file and configure it there. But what is the fun in that ;)
     * @return 
     */
    public List<Integer> getConfiguredFields(){
        List<Integer> configured = new LinkedList<Integer>();
        SettingsManager psSettings = stones.getSettingsManager();
        FileConfiguration config = stones.getConfig();
        //Get settings from PreciousStones
        List<LinkedHashMap<String, Object>> forceFieldStones = psSettings.getForeceFieldBlocks();
        for(LinkedHashMap<String, Object> stone : forceFieldStones){
            if(stone.containsKey("block") && stone.containsKey("ExpensiveField")){
                if(psSettings.isFieldType((Integer) stone.get("block")) && isBoolean(stone.get("ExpensiveField"))){
                    configured.add((Integer) stone.get("block"));
                }
            }
        }
        return configured;
    }
    
    private boolean isBoolean(Object o){
        if(o instanceof Boolean)
            return (Boolean) o;
        return false;
    }
}
