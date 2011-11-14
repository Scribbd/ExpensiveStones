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
package me.scriblon.plugins.expensivestones;

import javax.lang.model.type.UnknownTypeException;
import me.scriblon.plugins.expensivestones.managers.ESStorageManager;
import me.scriblon.plugins.expensivestones.utils.BlockUtil;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

/**
 * A class deciated to the ExpensiveField combo. Which is a fieldblock, a chest and a sign.
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ExpensiveField {
    //Essentials
    private int status;
    // basics
    private Sign sign;
    private Location signLocation;
    private Chest chest;
    private Location chestLocation;
    private Field field;
    // extended
    private ESFieldSettings settings;
    
    /**
     * Contructor gets UpkeepMaterial, amount and cost from 
     * @param sign
     * @param signLocation
     * @param chest
     * @param chestLocation
     * @param field
     * @throws UnknownTypeException 
     */
    public ExpensiveField(Block sign, Block chest, Field field) throws UnknownTypeException{
        if(isCorrectValues(sign, chest)){
            this.status = ESStorageManager.ES_ENABLED;
            
            this.sign = (Sign) sign;
            this.signLocation = sign.getLocation();
            this.chest = (Chest) chest;
            this.chestLocation = chest.getLocation();
            this.field = field;
            this.settings = ExpensiveStones.getInstance().getESFieldManager().getESFieldSetting(field.getTypeId());
        }
    }
    
    public ExpensiveField(int status, Location signLocation, Location chestLocation, Location fieldLocation){
        this.status = status;
        this.signLocation = signLocation;
        this.chestLocation = chestLocation;
        
        this.sign = (Sign) signLocation.getBlock();
        this.chest = (Chest) chestLocation.getBlock();
        
        this.field = PreciousStones.getInstance().getForceFieldManager().getField(fieldLocation.getBlock());
        this.settings = ExpensiveStones.getInstance().getESFieldManager().getESFieldSetting(field.getTypeId());
    }
    
    public Chest getChest() {
        return chest;
    }

    public Field getField() {
        return field;
    }

    public Sign getSign() {
        return sign;
    }

    public Location getChestLocation() {
        return chestLocation;
    }

    public Location getSignLocation() {
        return signLocation;
    }

    public int getStatus() {
        return status;
    }
    
    public ESFieldSettings getSettings(){
        return settings;
    }
    
    //Sign commands
    public void setSign(Block sign) {
        this.sign = (Sign) sign;
    }
    
    public boolean setSignToOP(){
        sign.setLine(3, "<ADMIN>");
        return sign.update();
    }
    
    public boolean setSignToOff(){
        sign.setLine(3, "<DISABLED>");
        return sign.update();
    }
    
    public boolean setSignToOn(){
        sign.setLine(3, "<ENABLED>");
        return sign.update();
    }
    
    public boolean setSignToDepleted(){
        sign.setLine(3, "<DEPLETED>");
        return sign.update();
    }
    
    public boolean setError(){
        sign.setLine(3, "<ERROR>");
        return sign.update();
    }
    
    //Chest commands
    public boolean doUpkeeping(){
        chest.getInventory().remove(settings.getUpkeepStack());
        return chest.update();
    }
    
    public boolean chestHasReqContent(){
        return chest.getInventory().contains(settings.getMaterial());
    }
    
    //Pirvates
    private boolean isCorrectValues(Block sign, Block chest) throws UnknownTypeException{
        if(BlockUtil.isSign(sign)){
            if(BlockUtil.isChest(chest))
                return true;
            else
                throw new UnknownTypeException(null, "Type is geen chest-type"); 
        }
        else
            throw new UnknownTypeException(null, "Type is geen Sign-type");
    }
}
