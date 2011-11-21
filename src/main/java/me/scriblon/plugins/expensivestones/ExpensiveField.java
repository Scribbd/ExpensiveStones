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
import me.scriblon.plugins.expensivestones.utils.ChestUtil;
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
    public ExpensiveField(Block sign, Block chest, Field field){
        if(isCorrectValues(sign, chest)){
            this.status = ESStorageManager.ES_ENABLED;
            
            this.sign = (Sign) sign.getState();
            this.signLocation = sign.getLocation();
            this.chest = (Chest) chest.getState();
            this.chestLocation = chest.getLocation();
            this.field = field;
            this.settings = ExpensiveStones.getInstance().getESFieldManager().getESFieldSetting(field.getTypeId());
        }
    }
    
    public ExpensiveField(int status, Location signLocation, Location chestLocation, Location fieldLocation){
        this.status = status;
        if(status != ESStorageManager.ES_DORMANT){
            this.signLocation = signLocation;
            this.chestLocation = chestLocation;
            this.sign = (Sign) signLocation.getBlock().getState();
            this.chest = (Chest) chestLocation.getBlock().getState();
        } else{
            this.signLocation = null;
            this.chestLocation = null;
            this.sign = null;
            this.chest = null;
        }
        this.field = PreciousStones.getInstance().getForceFieldManager().getField(fieldLocation.getBlock());
        this.settings = ExpensiveStones.getInstance().getESFieldManager().getESFieldSetting(field.getTypeId());
    }
    
    /**
     * To add dormant (new) Fields.
     * Also disables field automatically!
     * @param field 
     */
    public ExpensiveField(Field field){
        this.field = field;
        status = ESStorageManager.ES_DORMANT;
        settings = ExpensiveStones.getInstance().getESFieldManager().getESFieldSetting(field.getTypeId());
        
        field.setDisabled(true);
        
        sign = null;
        signLocation = null;
        chest = null;
        chestLocation = null;
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
    
    public boolean setStatus(int status) {
        if(status == ESStorageManager.ES_ENABLED || status == ESStorageManager.ES_ADMIN || status == ESStorageManager.ES_DISABLED){
            this.status = status;
            return true;
        }else if(status == ESStorageManager.ES_DORMANT){
            this.status = status;
            sign = null;
            signLocation = null;
            chest = null;
            chestLocation = null;
            
        }
        return false;
    }
    
    public ESFieldSettings getSettings(){
        return settings;
    }
    
    //Sign commands
    public void setSign(Block sign) {
        this.sign = (Sign) sign;
    }
    
    public void setSignToOP(){
        sign.setLine(3, "<ADMIN>");
        sign.update(true);
    }
    
    public void setSignToProgress(){
        sign.setLine(3, "<DISABLING>");
        sign.update(true);
    }
    
    public void setSignToOff(){
        sign.setLine(3, "<DISABLED>");
        sign.update(true);
    }
    
    public void setSignToOn(){
        sign.setLine(3, "<ENABLED>");
        sign.update(true);       
    }
    
    public void setSignToDepleted(){
        sign.setLine(3, "<DEPLETED>");
        sign.update(true);
    }
    
    public void setError(){
        sign.setLine(3, "<BROKEN>");
        sign.update(true);
    }
    
    //Chest commands
    public void doUpkeeping(){
        ChestUtil.removeInventoryItems(chest.getInventory(), settings.getUpkeepStack());
    }
    
    public boolean chestHasReqContent(){
        return ChestUtil.hasInventoryReqContent(chest.getInventory(), settings.getUpkeepStack());
    }
    
    //Checkers
    public boolean isAdmin(){
        return status == ESStorageManager.ES_ADMIN;
    }
    
    public boolean isDormant(){
        return status == ESStorageManager.ES_DORMANT;
    }
    
    public boolean isActive(){
        return status == ESStorageManager.ES_ENABLED;
    }
    
    public boolean isDisabled(){
        return status == ESStorageManager.ES_DISABLED;
    }
    
    //Field Toglles
    public void setFieldON(){
        field.setDisabled(false);
        //TODO debugcode
        if(!field.isDisabled())
            System.out.println("enabling succes");
        else
            System.out.println("enabling failed");
    }
    
    public void setFieldOFF(){
        field.setDisabled(true);
        //TODO debugcode
        if(field.isDisabled())
            System.out.println("disabling succes");
        else
            System.out.println("disabling failed");
    }
    
    public boolean isFieldDisabled(){
        return field.isDisabled();
    }
    //Pirvates
    private boolean isCorrectValues(Block sign, Block chest){
        if(BlockUtil.isSign(sign))
            if(BlockUtil.isChest(chest))
                return true;
        return false;
    }
}
