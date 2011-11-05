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
import me.scriblon.plugins.expensivestones.utils.BlockUtil;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.block.Block;

/**
 * A class deciated to the ExpensiveField combo. Which is a fieldblock, a chest and a sign.
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ExpensiveField {
    
    private Block sign;
    private Block chest;
    private Field field;
    
    private ExpensiveField(Block sign, Block chest, Field field) throws UnknownTypeException{
        if(BlockUtil.isSign(sign))
            this.sign = sign;
        else
            throw new UnknownTypeException(null, "Type is geen Sign-type");
   
        if(BlockUtil.isChest(chest))
            this.chest = chest;
        else
            throw new UnknownTypeException(null, "Type is geen chest-type");
        
        this.field = field;
    }
}
