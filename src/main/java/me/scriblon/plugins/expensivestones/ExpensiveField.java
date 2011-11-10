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
import me.scriblon.plugins.expensivestones.managers.ESFieldManager;
import me.scriblon.plugins.expensivestones.utils.BlockUtil;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

/**
 * A class deciated to the ExpensiveField combo. Which is a fieldblock, a chest and a sign.
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ExpensiveField {
    // basics
    private Sign sign;
    private Chest chest;
    private Field field;
    // extended
    private int upkeepCost;
    private long upkeepPeriod;
    private Material upkeepMaterial;
    private ESFieldManager esFieldManager;
    
    public ExpensiveField(Block sign, Block chest, Field field) throws UnknownTypeException{
        if(isCorrectValues(sign, chest)){
            this.sign = (Sign) sign;
            this.chest = (Chest) chest;
            this.field = field;
            //TODO figure efficient way to get upkeepMaterial, amount and cost
        }
    }
    
    public long getUpkeepPeriod(){
        return upkeepPeriod;
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

    public Material getUpkeepMaterial() {
        return upkeepMaterial;
    }

    public ItemStack getUpkeepStack(){
        return new ItemStack(upkeepMaterial, upkeepCost);
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
        chest.getInventory().remove(this.getUpkeepStack());
        return chest.update();
    }
    
    public boolean chestHasReqContent(){
        return chest.getInventory().contains(upkeepMaterial);
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
