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
package me.scriblon.plugins.expensivestones.configurators;

import me.scriblon.plugins.expensivestones.utils.DBFactory;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.storage.DBCore;

/**
 * Hooks up with the PreciousStone storage mechanism and makes transfers possible.
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ESStorageManager {
    
    public static final int ES_ENABLED = 0;
    public static final int ES_DISABLED = 1;
    public static final int ES_ADMIN = 2;
    
    private PreciousStones stones;
    private DBCore db;
    
    public ESStorageManager(){
        stones = PreciousStones.getInstance();
        db = DBFactory.produceDB();
    }
    
    public boolean isUpgraded(){
        return db.existsTable("exstones_disabled");
    }
    
    public void upgradeDatabase(){
        db.execute("CREATE TABLE IF NOT EXISTS `exstones_disabled' ('id' bigint(20) NOT NULL, 'disabled' tinyint NOT NULL default 0, CONSTRAINT pk_exst_dis PRIMARY KEY ('id'), CONSTRAINT fk_extdis_)");
    }
}
