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

import me.scriblon.plugins.expensivestones.ExpensiveStones;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Daan Meulenkamp
 */
public class ESPlayerListener extends PlayerListener{
        
    private PreciousStones stones;
    private Plugin plugin;
    
    public ESPlayerListener(){
        stones = PreciousStones.getInstance();
        plugin = ExpensiveStones.getInstance();
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        //When clicked toggle sign when available
    }
    
    
}
