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

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager;
import net.sacredlabyrinth.Phaed.PreciousStones.storage.DBCore;
import net.sacredlabyrinth.Phaed.PreciousStones.storage.MySQLCore;
import net.sacredlabyrinth.Phaed.PreciousStones.storage.SQLiteCore;

/**
 * To create a copy of the datacore available in preciousstones
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class DBFactory {
    
    public static DBCore produceDB(){
        PreciousStones stones = PreciousStones.getInstance();
        SettingsManager pSettings = stones.getSettingsManager();
        
        if(pSettings.isUseMysql()){
            return new MySQLCore(pSettings.getHost(), pSettings.getPort(), pSettings.getDatabase(), pSettings.getUsername(), pSettings.getPassword());
        }else{
            return new SQLiteCore("PreciousStones", stones.getDataFolder().getAbsolutePath());
        }
    }
}
