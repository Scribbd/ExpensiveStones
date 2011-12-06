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
package me.scriblon.plugins.expensivestones.managers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;

/**
 *
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ESPermissionManager {
    
    public static final String PERM_BYPASS = "ExpensiveStones.bypass";
    public static final String PERM_BYPASS_TOGGLE = "ExpensiveStones.bypass.toggle";
    public static final String PERM_ADMIN = "ExpensiveStones.admin";
    public static final String PERM_INFO = "ExpensiveStones.info";
    
    private final boolean defaultToggle;
    private final Set<Player> toggled;
    
    public ESPermissionManager(){
        toggled = Collections.synchronizedSet(new HashSet<Player>());
        //TODO make config :( If i want to toggle I can't leach of ExpensiveStones any further.
        defaultToggle = true;
    }
    
    public void toggleBypass(Player player){
        if(toggled.contains(player)){
            toggled.remove(player);
        }else{
            toggled.add(player);
        }
    }
    
    public boolean bypassResult(Player player){
        if(toggled.contains(player))
            return defaultToggle;
        else
            return !defaultToggle;
    }
    
}
