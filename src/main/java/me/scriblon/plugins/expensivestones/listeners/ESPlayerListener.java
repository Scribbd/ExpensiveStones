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
package me.scriblon.plugins.expensivestones.listeners;

import me.scriblon.plugins.expensivestones.ExpensiveField;
import me.scriblon.plugins.expensivestones.ExpensiveStones;
import me.scriblon.plugins.expensivestones.managers.ESFieldManager;
import me.scriblon.plugins.expensivestones.utils.BlockUtil;

import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ESPlayerListener extends PlayerListener{
        
    private final ExpensiveStones plugin;
    private final ESFieldManager manager;
    
    public ESPlayerListener(){
        plugin = ExpensiveStones.getInstance();
        manager = plugin.getESFieldManager();
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.isCancelled())
            return;
                                                                                                                                            
        final Block block = event.getClickedBlock();
    //___Implementation on clicking sign
        if(BlockUtil.isSign(block)){
            final Block fieldBlock = BlockUtil.getFieldStone(block, false);
            //Check if valid block is given back
            if(fieldBlock == null){
                System.out.println("SignInteract: FieldBlock == null");
                return;
            }
            //Check if field is dormant
            if(manager.isInDormant(fieldBlock.getLocation()))
                return;
            
            //Get ExpensiveField and process request
            final ExpensiveField field = manager.getExpensiveField(fieldBlock);
            this.toggleField(field);
            return;
        }
    //___Implementation on clicking on stone
        if(manager.isExpensiveType(block.getTypeId())){
            //Check if field is known
            if(!manager.isKnown(block))
                return;
            
            //Check if field is in dormant (then it should  be skipped)
            if(manager.isInDormant(block.getLocation()))
                return;
            
            final ExpensiveField field = manager.getExpensiveField(block);
            this.toggleField(field);
        }
    }
    
    private void toggleField(ExpensiveField field){
        if(manager.isInDisabled(field.getField().getId())){
            manager.enableField(field);
        } else {
            manager.disableField(field);
        }
    }
}
