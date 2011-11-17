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
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;

//TODO place permissions as public statics somewhere else!

/**
 * ExpensiveStones Listener for sign place event and the placing of FieldBlocks
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ESBlockListener extends BlockListener{
    
    private PreciousStones stones;
    private ExpensiveStones plugin;
    private ESFieldManager manager;
    
    public ESBlockListener(){
        stones = PreciousStones.getInstance();
        plugin = ExpensiveStones.getInstance();
        manager = plugin.getESFieldManager();
    }
    
    
    @Override
    public void onSignChange(SignChangeEvent event) {
        if(event.isCancelled())
            return;
        
        if(!event.getLine(0).equalsIgnoreCase("[ExpField]"))
            return;
        
        Player player = event.getPlayer();
        // admin________________________________________________________________
        if(event.getLine(1).equalsIgnoreCase("admin")){
           //If player has admin-right
            if(!player.hasPermission("ExpensiveStones.admin")){
              //Check if block there is a new expensiveField
                final Block block = BlockUtil.getNewFieldStone(event.getBlock());
                if(block == null || manager.isExpensiveType(block.getTypeId())){
                    player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: No (Expensive) Field-Block Found");
                    return;
                }
                //Check if field is known as ExpensiveField
                if(!manager.isKnown(block)){
                    player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: Field is not know as ExpensiveField.");
                    return;
                }
                
                ExpensiveField field = manager.getExpensiveField(block);
                manager.setAdminField(field);
            }else{
                player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: You don't have permission to create an admin-field");
                event.setCancelled(true);
                return;
            }
        //Normal_______________________________________________________________________
        }else{
            //TODO register as ExpensiveField
        }        
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.isCancelled())
            return;
        
        final Block block = event.getBlock();
        // SignCheck
        if(block.getType() == Material.SIGN){
            //TODO handle dormanting of field
            return;
        }
        // Check if block is known to ExpensiveField
        if(manager.isKnown(block)){
            final ExpensiveField field = manager.getExpensiveField(block);
            if(manager.isInDormant(block.getLocation()))
                field.setFieldON();
            stones.getForceFieldManager().deleteField(field.getField());
            manager.removeField(field);
        }  
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.isCancelled())
            return;
        
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        
        //Check if block in and PSField and expField
        if(!stones.getForceFieldManager().isField(block) && !manager.isExpensiveType(block.getTypeId())) 
            return;
        
        //Do nothing when player has bypass permissions
        if(player.hasPermission("ExpensiveStones.bypass")){
            player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: Bypassed! Field handled by PreciousStones.");
            return;
        }
        
        //Add Field, will auto-dormant in creation of ExpensiveField
        if(stones.getForceFieldManager().isField(block)){
            ExpensiveField expField = new ExpensiveField(stones.getForceFieldManager().getField(block));
            manager.addField(expField, true);
            player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: stone detected! Stone is disabled.");
        }
    }

    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        //When sign or chest is powered enable field when possible.
    }
}
