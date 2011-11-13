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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.scriblon.plugins.expensivestones.ExpensiveField;
import me.scriblon.plugins.expensivestones.ExpensiveStones;
import me.scriblon.plugins.expensivestones.utils.DBFactory;
import me.scriblon.plugins.expensivestones.utils.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.SettingsManager;
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
    private static final Logger log = ExpensiveStones.getLogger();
    
    private final Map<Long, Integer> pendingStatusMutation = Collections.synchronizedMap(new LinkedHashMap<Long, Integer>());
    private final Set<Long> pendingDeletions = Collections.synchronizedSet(new LinkedHashSet<Long>());
    private final Set<ExpensiveField> pendingAdditions = Collections.synchronizedSet(new LinkedHashSet<ExpensiveField>());
    
    public ESStorageManager(){
        stones = PreciousStones.getInstance();
        db = DBFactory.produceDB();
    }
    
        /**
     * Get List of integers (blockids) which has the expensiveField-tag
     * Might consider using own config.yml file and configure it there. But what is the fun in that ;)
     * @return 
     */
    public Set<Integer> getConfiguredFields(){
        Set<Integer> configured = new LinkedHashSet<Integer>();
        SettingsManager psSettings = stones.getSettingsManager();
        //Get settings from PreciousStones
        List<LinkedHashMap<String, Object>> forceFieldStones = psSettings.getForceFieldBlocks();
        for(LinkedHashMap<String, Object> stone : forceFieldStones){
            if(stone.containsKey("block") && stone.containsKey("ExpensiveField")){
                if(psSettings.isFieldType((Integer) stone.get("block")) &&  Helper.convertBoolean(stone.get("ExpensiveField"))){
                    configured.add((Integer) stone.get("block"));
                }
            }
        }
        return configured;
    }
    
    public boolean dbHasDisabled(){
        return db.existsTable("exstones_disabled");
    }
    
    public boolean dbHasBackup(){
        return db.existsTable("exstones_backup");
    }
    
    public void addDisabledToDatabase(){
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
                db.execute("CREATE TABLE IF NOT EXISTS 'exstone_fields' ("
                    + "'disabled' tinyint NULL default 0, "
                    + "'chestx' int(11) default NULL, 'chesty' int(11) default NULL, 'chestz' int(11) default NULL, "
                    + "'signx int(11) default NULL', 'signy' int(11) default NULL, signz' int(11) default NULL, "
                    + "CONSTRAINT uq_exst UNIQUE KEY ('chestx', 'chesty', 'chestz', 'signx', 'signy', 'signz'"
                        + ")");
            }else{
                log.log(Level.INFO, "[ExpensiveStones] SQLite Connection Failed");
            }
        }
    }
    
    // Offers    
    public void offerToggleOn(ExpensiveField expField){
        pendingStatusMutation.put(expField.getField().getId(), ES_ENABLED);
    }
    
    public void offerToggleOff(ExpensiveField expField){
        pendingStatusMutation.put(expField.getField().getId(), ES_DISABLED);
    }
    
    public void offerToggleOp(ExpensiveField expField){
        pendingStatusMutation.put(expField.getField().getId(), ES_ADMIN);
    }
    
    /**
     * 
     * @param expField 
     */
    public void offerDeletion(ExpensiveField expField){
        pendingDeletions.add(expField.getField().getId());
    }
    
    // Executors
    public List<ExpensiveField> getExpensiveFields(String world){
        if(db.checkConnection()){
            
        }else{
            ExpensiveStones.infoLog("Database Error, can't connect! (selection)");
            return null;
        }
        return null;
    }
    
    public void deleteExpensiveField(){
        if(pendingDeletions.isEmpty())
            return;
        if(db.checkConnection()){
            for(Long single: pendingDeletions){

            }
        }else{
            ExpensiveStones.infoLog("Database Error, can't connect! (deletion)");
        }
    }
    
    public void insertExpensiveField() {
        if(pendingAdditions.isEmpty())
            return;
        if(db.checkConnection()){
            for(ExpensiveField single: pendingAdditions){
            
            
            }
        }else{
            ExpensiveStones.infoLog("Database Error, can't connect! (addition)");
        }
    }
    
    public void saveAll(){
        synchronized(this){
            this.deleteExpensiveField();
            this.insertExpensiveField();
        }
    }
}
