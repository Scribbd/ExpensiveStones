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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.scriblon.plugins.expensivestones.ExpensiveField;
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
    private Logger log;
    
    private final List<ExpensiveField> pending = Collections.synchronizedList(new LinkedList());;
    
    public ESStorageManager(Logger log){
        stones = PreciousStones.getInstance();
        db = DBFactory.produceDB();
        this.log = log;
    }
    
    public boolean isUpgraded(){
        return db.existsTable("exstones_disabled");
    }
    
    public void upgradeDatabase(){
        if(stones.getSettingsManager().isUseMysql()){
            if(db.checkConnection()){
                db.execute("CREATE TABLE IF NOT EXISTS 'exstone_fields' "
                    + "('id' bigint(20) NOT NULL, "
                    + "'disabled' tinyint NULL default 0, "
                    + "'chestx' int(11) default NULL, 'chesty' int(11) default NULL, 'chestz' int(11) default NULL, "
                    + "'signx int(11) default NULL', 'signy' int(11) default NULL, signz' int(11) default NULL, "
                    + "CONSTRAINT pk_exst_dis PRIMARY KEY ('id'), "
                    + "CONSTRAINT uq_exst UNIQUE KEY ('chestx', 'chesty', 'chestz', 'signx', 'signy', 'signz') "
                    + "CONSTRAINT fk_extdis_pfield FOREIGN KEY ('id') REFERENCES pstone_fields ('id'))");
            }else{
                log.log(Level.INFO, "[ExpensiveStones] MySQL Connection Failed");
            }
        }else{
            if(db.checkConnection()){
                db.execute("CREATE TABLE IF NOT EXISTS 'exstone_fields' "
                    + "('id' bigint(20) PRIMARY KEY, "
                    + "'disabled' tinyint NULL default 0, "
                    + "'chestx' int(11) default NULL, 'chesty' int(11) default NULL, 'chestz' int(11) default NULL, "
                    + "'signx int(11) default NULL', 'signy' int(11) default NULL, signz' int(11) default NULL, "
                    + "CONSTRAINT uq_exst UNIQUE KEY ('chestx', 'chesty', 'chestz', 'signx', 'signy', 'signz')");
            }else{
                log.log(Level.INFO, "[ExpensiveStones] SQLite Connection Failed");
            }
        }
    }
    
    public void offerExpensiveField(ExpensiveField expField){
        pending.add(expField);
    }
    
    public void deleteExpensiveField(ExpensiveField expField){
        
    }
    
    public void insertExpensiveField(ExpensiveField expField) {
        
    }
    
    public void getExpensiveFields(){
        
    }
}
