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
import me.scriblon.plugins.expensivestones.tasks.UpKeeper;
import me.scriblon.plugins.expensivestones.utils.BlockUtil;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

/**
 * ExpensiveStones Listener for sign place event and the placing of FieldBlocks
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ESBlockListener extends BlockListener{
    
    private PreciousStones stones;
    private Plugin plugin;
    
    public ESBlockListener(){
        stones = PreciousStones.getInstance();
        plugin = ExpensiveStones.getInstance();
    }
    
    
    @Override
    public void onSignChange(SignChangeEvent event) {
        if(event.isCancelled())
            return;
        
        if(!event.getLine(0).equalsIgnoreCase("[ExpField]"))
            return;
        
        Player player = event.getPlayer();
        
        if(!event.getLine(1).equalsIgnoreCase("admin")){
            if(!player.hasPermission("ExpensiveStones.admin")){
                //TODO transfer field over to preciousStone DB.
                //TODO register field in normal plugin.
            }else{
                player.sendMessage(ChatColor.YELLOW + "You don't have permission to create an admin-field");
                event.setCancelled(true);
                return;
            }
        }else{
            //TODO register as ExpensiveField
        }
        //Get values needed for 
        Block sign = event.getBlock();
        
        Block chest = BlockUtil.getChest(sign);
        
        Field field = stones.getForceFieldManager().getField(BlockUtil.getFieldStone(sign));
        //Make expField
        ExpensiveField expField = new ExpensiveField(sign, chest, field);
        
        //If everything succeeds
        UpKeeper keeper;
        
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.isCancelled())
            return;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.isCancelled())
            return;
        
    }
    
    

}
