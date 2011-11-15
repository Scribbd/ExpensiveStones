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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.scriblon.plugins.expensivestones.ExpensiveField;
import me.scriblon.plugins.expensivestones.ExpensiveStones;
import me.scriblon.plugins.expensivestones.utils.DBFactory;
import me.scriblon.plugins.expensivestones.utils.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.storage.DBCore;
import org.bukkit.Location;
import org.bukkit.World;

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
    
    private final Map<Long, Integer> pendingStatusMutations = Collections.synchronizedMap(new LinkedHashMap<Long, Integer>());
    private final Map<Long, ExpensiveField> pendingUpdates = Collections.synchronizedMap(new LinkedHashMap<Long, ExpensiveField>());
    private final Set<Long> pendingDeletions = Collections.synchronizedSet(new LinkedHashSet<Long>());
    private final Set<ExpensiveField> pendingAdditions = Collections.synchronizedSet(new LinkedHashSet<ExpensiveField>());
    
    public ESStorageManager(){
        stones = PreciousStones.getInstance();
        db = DBFactory.produceDB();
    }
    
    public boolean dbHasExpensive(){
        return db.existsTable("exstones_disabled");
    }
    
    public void addExpensiveTableToDatabase(){
        if(stones.getSettingsManager().isUseMysql()){
            if(db.checkConnection()){
                db.execute("CREATE TABLE IF NOT EXISTS 'exstone_fields' "
                    + "('id' bigint(20) NOT NULL, "
                    + "'status' tinyint NULL default 0, "
                    + "'chestx' int(11) default NULL, 'chesty' int(11) default NULL, 'chestz' int(11) default NULL, "
                    + "'signx' int(11) default NULL, 'signy' int(11) default NULL, signz' int(11) default NULL, "
                    + "'world' varchar(25) default NULL, "
                    + "CONSTRAINT pk_exst_dis PRIMARY KEY ('id'), "
                    + "CONSTRAINT uq_exst UNIQUE KEY ('chestx', 'chesty', 'chestz', 'signx', 'signy', 'signz', 'world') "
                    + "CONSTRAINT fk_extdis_pfield FOREIGN KEY ('id') REFERENCES pstone_fields ('id'))");
            }else{
                log.log(Level.INFO, "[ExpensiveStones] MySQL Connection Failed");
            }
        }else{
            if(db.checkConnection()){
                db.execute("CREATE TABLE IF NOT EXISTS 'exstone_fields' ("
                    + "'status' tinyint NULL default 0, "
                    + "'chestx' int(11) default NULL, 'chesty' int(11) default NULL, 'chestz' int(11) default NULL, "
                    + "'signx' int(11) default NULL, 'signy' int(11) default NULL, signz' int(11) default NULL, "
                    + "'world' varchar(25) default NULL, "
                    + "CONSTRAINT uq_exst UNIQUE KEY ('chestx', 'chesty', 'chestz', 'signx', 'signy', 'signz', 'world')"
                    + ")");
            }else{
                log.log(Level.INFO, "[ExpensiveStones] SQLite Connection Failed");
            }
        }
    }
    
    // Offers
    public void offerAddition(ExpensiveField expField){
        pendingAdditions.add(expField);
    }
    
    public void offerToggleOn(ExpensiveField expField){
        long id = expField.getField().getId();
        if(!pendingUpdates.containsKey(id))
            pendingStatusMutations.put(expField.getField().getId(), ES_ENABLED);
        else
            expField.setStatus(ES_ENABLED);
    }
    
    public void offerToggleOff(ExpensiveField expField){
        long id = expField.getField().getId();
        if(!pendingUpdates.containsKey(id))
            pendingStatusMutations.put(expField.getField().getId(), ES_DISABLED);
        else
            expField.setStatus(ES_DISABLED);
    }
    
    public void offerToggleOp(ExpensiveField expField){
        long id = expField.getField().getId();
        if(!pendingUpdates.containsKey(id))
            pendingStatusMutations.put(expField.getField().getId(), ES_ADMIN);
        else
            expField.setStatus(ES_ADMIN);
    }
    
    public boolean setToggle(ExpensiveField expField, int status){
        if(status == ES_ENABLED || status == ES_ADMIN || status == ES_DISABLED){
            pendingStatusMutations.put(expField.getField().getId(), status);
            return true;
        }
        return false;
    }
    
    public void offerUpdatedField(ExpensiveField expField){
        Long id = expField.getField().getId();
        if(pendingStatusMutations.containsKey(id)){
            expField.setStatus(pendingStatusMutations.get(id));
            pendingStatusMutations.remove(id);
        }
        pendingUpdates.put(id, expField);
    }
    
    private boolean checkWholeField(ExpensiveField expField){
        return pendingUpdates.containsKey(expField.getField().getId());
    }
    
    /**
     * When
     * @param expField 
     */
    public void offerDeletion(ExpensiveField expField){
        pendingDeletions.add(expField.getField().getId());
    }
    
    // Executors
    public List<ExpensiveField> getExpensiveFields(String world){
        if(db.checkConnection()){
            List<ExpensiveField> fields = new ArrayList<ExpensiveField>();
            
            ResultSet res = db.select("SELECT pstone_fields.id as id, "
                    + "status, "
                    + "chestx, chesty, chestz, "
                    + "signx, signy, signz, "
                    + "pstone_fields.world as world, "
                    + "x, y, z "
                    + "FROM exstone_fields INNER JOIN pstone_fields ON pstone_fields.id = exstone_fields.id"
                    + "WHERE world = '" + Helper.escapeQuotes(world) + "';");
            if(res != null){
                try{
                    while(res.next()){
                        try{
                            long id = res.getLong("id");
                            int status = res.getInt("status");
                            int chestx = res.getInt("chestx");
                            int chesty = res.getInt("chesty");
                            int chestz = res.getInt("chestz");
                            int signx = res.getInt("signx");
                            int signy = res.getInt("signy");
                            int signz = res.getInt("signz");
                            int x = res.getInt("x");
                            int y = res.getInt("y");
                            int z = res.getInt("z");
                            String recordedWorld = res.getString("world");
                            
                            if(world.equalsIgnoreCase(recordedWorld) && world != null)
                                ExpensiveStones.infoLog("worlds don't match, recorded world taken");
                            
                            World thisWorld = stones.getServer().getWorld(recordedWorld);
                            
                            Location chest = new Location(thisWorld, chestx, chesty, chestz);
                            Location sign = new Location(thisWorld, signx, signy, signz);
                            Location field = new Location(thisWorld, x, y, z);
                            
                            fields.add(new ExpensiveField(status, sign, chest, field));
                         
                        }catch(Exception ex){
                            ExpensiveStones.infoLog(ex.getMessage()); 
                        }                        
                    }   
                    res.close();
                }catch(SQLException ex){
                    Logger.getLogger(ESStorageManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }else{
            ExpensiveStones.infoLog("Database Error, can't connect! (selection)");
            return null;
        }
        return null;
    }
        
    public void insertExpensiveField() {
        if(pendingAdditions.isEmpty())
            return;
        if(db.checkConnection()){
            for(ExpensiveField single: pendingAdditions){
                Location chest = single.getChestLocation();
                Location sign = single.getSignLocation();
                db.insert("INSERT INTO 'exstone_fields' ( 'id', 'status', "
                        + "'chestx', 'chesty', 'chestz', "
                        + "'signx', 'signy', 'signz', "
                        + "world "
                    + "VALUES( " + single.getField().getId() + "," + single.getStatus() + ","
                        + chest.getBlockX() + "," + chest.getBlockY() + "," + chest.getBlockZ() + ","
                        + sign.getBlockX() + "," + sign.getBlockY() + "," + sign.getBlockZ() + ",'"
                        + Helper.escapeQuotes(single.getField().getWorld()) + "');");
            }
            pendingAdditions.clear();
        }else{
            ExpensiveStones.infoLog("Database Error, can't connect! (addition)");
        }     
    }
    
    public void deleteExpensiveField(){
        if(pendingDeletions.isEmpty())
            return;
        if(db.checkConnection()){
            for(Long single: pendingDeletions){
                db.delete("DELETE FROM 'exstone_fields' "
                        + "WHERE id = " + single);
            }
            pendingDeletions.clear();
        }else{
            ExpensiveStones.infoLog("Database Error, can't connect! (deletion)");
        }
    }
    
    public void updateStatus(){
        if(pendingStatusMutations.isEmpty())
            return;
        if(db.checkConnection()){
            for(Entry single : pendingStatusMutations.entrySet()){
                db.update("UPDATE 'exstone_fields' "
                        + "SET 'status' = " + single.getValue()
                        + "WHERE 'id' = " + single.getKey());
            }
            pendingStatusMutations.clear();
        }else{
            ExpensiveStones.infoLog("Database Error, can't connect! (deletion)");
        }
    }
    
    public void updateField(){
        if(pendingUpdates.isEmpty())
            return;
        if(db.checkConnection()){
            for(Entry single : pendingUpdates.entrySet()){
                ExpensiveField field = (ExpensiveField) single.getValue();
                Location chest = field.getChestLocation();
                Location sign = field.getSignLocation();
                db.update("UPDATE 'exstone_fields' "
                        + "SET 'status' = " + field.getStatus() + ","
                        + "'chestx' = " + chest.getBlockX() + ","
                        + "'chesty' = " + chest.getBlockY() + ","
                        + "'chestz' = " + chest.getBlockZ() + ","
                        + "'signx' = " + sign.getBlockX() + ","
                        + "'signy' = " + sign.getBlockY() + ","
                        + "'signz' = " + sign.getBlockZ() + ","
                        + "'world' = '" + field.getField().getLocation().getWorld() + "' "
                        + "WHERE 'id' = " + single.getKey() + ";");
            }
            pendingUpdates.clear();
        }else{
            ExpensiveStones.infoLog("Database Error, can't connect! (deletion)");
        }
    }
    
    public void saveAll(){
        synchronized(this){
            this.insertExpensiveField();
            this.deleteExpensiveField();
            this.updateStatus();
            this.updateField();
        }
    }
}
