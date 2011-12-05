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

import org.bukkit.ChatColor;
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
        
        if(!event.getLine(0).equalsIgnoreCase("[ExpStone]"))
            return;
        
        //Get basics
        final Block block = event.getBlock();
        final Player player = event.getPlayer();
        //Get a certein block.
        final Block fieldBlock = BlockUtil.getFieldStone(block, false);
        //Chech if any surrounding blocks is a known fieldblock.
        if(fieldBlock == null){
            player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: No Expensive StoneType Found.");
            return;
        }
    //_____________________Test Adming_____________________________
        if(event.getLine(2).equalsIgnoreCase("admin")){
            //Check rights
            if(!player.hasPermission("ExpensiveStones.admin")){
                player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: You don't have the permissions to make an adminField.");
            }else{
                manager.setAdminField(manager.getExpensiveField(block));                
                player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: Admin-Field created, field is now handled by PreciousStones.");
                player.sendMessage(ChatColor.YELLOW + "You can now destroy this sign.");
            }
            return;
        }
        
    //_____________________Normal Player___________________________
        final Block chestBlock = BlockUtil.getChest(block);
        //Chech chest
        if(chestBlock == null){
            player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: No chest found for register");
            return;
        }
        final ExpensiveField expField = new ExpensiveField(block, chestBlock, stones.getForceFieldManager().getField(fieldBlock));
        manager.disableField(expField);
        event.setLine(1, expField.getField().getOwner());
        event.setLine(2, expField.getSettings().getMaterial().toString());
        event.setLine(3, "<DISABLED>");
        player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: Field Registered, click to enable field");
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.isCancelled())
            return;
        
        final Block block = event.getBlock();
        
        if(!stones.getSettingsManager().isFieldType(event.getBlock()) && !BlockUtil.isSign(block))
            return;
        
        // SignCheck
        if(BlockUtil.isSign(block)){
            //TODO debugcode
            System.out.println("ExpStone: Signbreak event triggered");
            final Player player = event.getPlayer();
            final Block fieldBlock = BlockUtil.getFieldStone(block, false);
            //Check if fieldBlock has a known field
            if(fieldBlock == null){
                //TODO debugcode
                System.out.println("ExpStone: Couldn't find asked stone.");
                return;
            }   
            //Get ExpensiveField and dormant it.
            final ExpensiveField field = manager.getExpensiveField(fieldBlock);
            manager.setDormantField(field);
            player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: Field is succesfully dormanted.");
            return;
        }
        // Check if block is known to ExpensiveField
        if(manager.isKnown(block)){
            final ExpensiveField field = manager.getExpensiveField(block);
            
            field.setFieldON();
            if(!manager.isInDormant(field.getField().getLocation()))
                field.setError();
            manager.removeField(field);
            
            event.getPlayer().sendMessage(ChatColor.YELLOW + "ExpensiveStones: Field is ready to be deleted.");
        }  
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.isCancelled())
            return;
        
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        
        //Check if block in an ExpensiveType
        if(!manager.isExpensiveType(block.getTypeId())) 
            return;
        //TODO debugcode
        System.out.println("Expst: stone is of type");
        //Check if block is known to PreciousStone (did stone get registered there)
        if(!stones.getForceFieldManager().isField(block))
            return;
        System.out.println("Expst: stone is a Field block.");
        //Do nothing when player has bypass permissions
        if(player.hasPermission("ExpensiveStones.bypass")){
            player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: Bypassed! Field handled by PreciousStones.");
            return;
        }
        
        //Add Field, will auto-dormant in creation of ExpensiveField
        ExpensiveField expField = new ExpensiveField(stones.getForceFieldManager().getField(block));
        manager.addField(expField, true);
        player.sendMessage(ChatColor.YELLOW + "ExpensiveStones: stone detected! Stone is disabled.");
        player.sendMessage(ChatColor.YELLOW + "Place chest and sign to activate the field.");
    }

    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        //When sign or chest is powered enable field when possible.
    }
}
