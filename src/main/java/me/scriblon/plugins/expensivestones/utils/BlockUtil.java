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
package me.scriblon.plugins.expensivestones.utils;

import me.scriblon.plugins.expensivestones.ExpensiveStones;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * Utility for blocks.
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class BlockUtil {
    
    public static boolean isSign(Block block){
        if(block == null) return false;
        Material type = block.getType();
        return type == Material.WALL_SIGN || type == Material.SIGN_POST;
    }
    
    public static boolean isChest(Block block){
        if(block == null) return false;
        Material type = block.getType();
        return type == Material.CHEST;
    }
    
    /**
     * Get chest when available
     * @param sign Block of sign.
     * @return Block with chest, or null when not available
     */
    public static Block getChest(Block sign){
        Block block;
        for(int x = -1; x<2; x++){
            for(int y = -1; y<2; y++){
                for(int z = -1; z<2; z++){
                    block = sign.getRelative(x, y, z);
                    if(isChest(block)){
                        return block;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Get FieldBlock when available
     * @param sign
     * @return gets block
     */
    public static Block getFieldStone(Block sign){
        Block block;
        for(int x = -1; x<2; x++){
            for(int y = -1; y<2; y++){
                for(int z = -1; z<2; z++){
                    block = sign.getRelative(x, y, z);
                    if(isField(block) && !isKnownField(block)){
                        return block;
                    }
                }
            }
        }
        
        block = sign.getRelative(0,0,2);
        if(isField(block)){
            return block;
        }
        return null;
    }
    
    public static boolean isField(Block block){
        return PreciousStones.getInstance().getSettingsManager().isFieldType(block.getType());
    }
    
    public static boolean isKnownField(Block field){
        return ExpensiveStones.getInstance().getESFieldManager().isKnown(((Field) field).getId());
    }
    
}
