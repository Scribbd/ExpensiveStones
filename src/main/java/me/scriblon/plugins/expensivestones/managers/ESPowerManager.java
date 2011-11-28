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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import me.scriblon.plugins.expensivestones.ExpensiveField;
import me.scriblon.plugins.expensivestones.ExpensiveStones;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 *
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ESPowerManager {
    private final ExpensiveStones plugin;
    private final Map<Location, Long> powerBlock;
    
    public ESPowerManager(){
        plugin = ExpensiveStones.getInstance();
        powerBlock = Collections.synchronizedMap(new HashMap<Location,Long>());
    }
    
    public void addBlock(Location newLocation, long iD){
        powerBlock.put(newLocation, iD);
    }
    
    public void addBlock(Map<Location, Long> locationMap){
        powerBlock.putAll(locationMap);        
    }
    
    public void addFieldBlocks(ExpensiveField field){
        powerBlock.put(field.getSignLocation(), field.getField().getId());
    }
    
    public void addFieldBlocks(long iD){
        addFieldBlocks(plugin.getESFieldManager().getExpensiveField(iD));
    }
    
    public void addFieldBlocksCollection(Collection<ExpensiveField> list){
        for(ExpensiveField field : list){
            addFieldBlocks(field);
        }
    }
    
    public void removeBlock(Location location){
        powerBlock.remove(location);
    }
    
    public void removeFieldBlocks(long iD){
        removeFieldBlocks(plugin.getESFieldManager().getExpensiveField(iD));
    }
    
    public void removeFieldBlocks(ExpensiveField field){
        final Location signLoc = field.getSignLocation();
        powerBlock.remove(signLoc);
    }
    
    public boolean isLocationInteresting(Location location){
        return powerBlock.containsKey(location);
    }
    
    public boolean isFieldInteresting(ExpensiveField field){
        return isLocationInteresting(field.getSignLocation());
    }
    
    public boolean isBlockPowered(Block block){
        if(isLocationInteresting(block.getLocation()))
            return block.getBlockPower() > 0;
        return false;
    }
    
    public long getLinkedId(Location location){
        return powerBlock.get(location);
    }
    
    public long getLinkedId(Block block){
        return getLinkedId(block.getLocation());
    }
    
}
