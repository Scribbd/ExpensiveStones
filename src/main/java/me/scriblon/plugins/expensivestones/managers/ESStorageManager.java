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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Hooks up with the PreciousStone storage mechanism and makes transfers possible.
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ESStorageManager {
    
    public static final int ES_DORMANT = -1;
    public static final int ES_DISABLED = 0;
    public static final int ES_ENABLED = 1;
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
        return db.existsTable("`exstone_fields`");
    }
    
    public void addExpensiveTableToDatabase(){
        if(stones.getSettingsManager().isUseMysql()){
            ExpensiveStones.infoLog("Using MySQL to create table.");
            if(db.checkConnection()){
                db.execute("CREATE TABLE IF NOT EXISTS `exstone_fields` ("
                    + "`id` bigint(20) NOT NULL, "
                    + "`status` tinyint NULL default 0, "
                    + "`chestx` int(11) default NULL, "
                        + "`chesty` int(11) default NULL, "
                        + "`chestz` int(11) default NULL, "
                    + "`signx` int(11) default NULL, "
                        + "`signy` int(11) default NULL, "
                        + "`signz` int(11) default NULL, "
                    + "`world` varchar(25) default NULL, "
                    + "CONSTRAINT pk_exst_dis PRIMARY KEY (`id`), "
                    + "CONSTRAINT uq_exst UNIQUE (`chestx`, `chesty`, `chestz`, `signx`, `signy`, `signz`, `world`) "
                    + "CONSTRAINT fk_extdis_pfield FOREIGN KEY (`id`) REFERENCES pstone_fields (`id`));");
                ExpensiveStones.infoLog("MySQL table should be created.");
            }else{
                log.log(Level.INFO, "MySQL Connection Failed");
            }
        }else{
            ExpensiveStones.infoLog("Using SQLite to create table.");
            if(db.checkConnection()){
                db.execute("CREATE TABLE IF NOT EXISTS `exstone_fields` ( "
                    + "`id` INTEGER PRIMARY KEY,  "
                    + "`status` int(2) NULL default NULL,  "
                    + "`chestx` int(11) default NULL,  "
                        + "`chesty` int(11) default NULL,  "
                        + "`chestz` int(11) default NULL,  "
                    + "`signx` int(11) default NULL,  "
                        + "`signy` int(11) default NULL,  "
                        + "`signz` int(11) default NULL,  "
                    + "`x` int(11) default NULL,  "
                        + "`y` int(11) default NULL,  "
                        + "`z` int(11) default NULL,  "
                    + "`world` varchar(25) default NULL,  "
                    + "CONSTRAINT uq_exst UNIQUE (`chestx`,`chesty`,`chestz`,`signx`,`signy`,`signz`,`x`,`y`,`z`,`world`) "
                        + ");");
                ExpensiveStones.infoLog("SQLite table should be created.");
            }else{
                ExpensiveStones.infoLog("SQLite Connection Failed");
            }
        }
    }
    
    // Offers
    public void offerAddition(ExpensiveField expField){
        pendingAdditions.add(expField);
    }
    
    public void offerStatusUpdate(ExpensiveField expField){
        long id = expField.getField().getId();
        if(pendingUpdates.containsKey(id))
            pendingUpdates.put(expField.getField().getId(), expField);
        else
            pendingStatusMutations.put(expField.getField().getId(), expField.getStatus());
    }
    
    public void offerUpdatedField(ExpensiveField expField){
        Long id = expField.getField().getId();
        if(pendingStatusMutations.containsKey(id))
            pendingStatusMutations.remove(id);
        pendingUpdates.put(id, expField);
    }
    
    public boolean checkWholeField(ExpensiveField expField){
        return pendingUpdates.containsKey(expField.getField().getId());
    }
    
    /**
     * 
     * @param expField 
     */
    public void offerDeletion(ExpensiveField expField){
        pendingDeletions.add(expField.getField().getId());
    }
    
    private void offerDeletionOnLoad(Long id){
        pendingDeletions.add(id);
    }
    
    // Executors
    public List<ExpensiveField> getExpensiveFields(String world){
        if(db.checkConnection()){
            List<ExpensiveField> fields = new ArrayList<ExpensiveField>();
            ResultSet res;
            if(!stones.getSettingsManager().isUseMysql()){
                ExpensiveStones.infoLog("Using MySQL to get table.");
                res =  db.select("SELECT exstone_fields.id as id, "
                        + "status, "
                        + "chestx, chesty, chestz, "
                        + "signx, signy, signz, "
                        + "exstone_fields.world as world, "
                        + "x, y, z "
                        + "FROM exstone_fields "
                        + "WHERE world = '" + Helper.escapeQuotes(world) + "';");
                System.out.println(world);
                ExpensiveStones.infoLog("Done using MySQL to get table.");
            }else{
                res =  db.select("SELECT pstone_fields.id as id, "
                        + "status, "
                        + "chestx, chesty, chestz, "
                        + "signx, signy, signz, "
                        + "pstone_fields.world as world, "
                        + "x, y, z "
                        + "FROM exstone_fields INNER JOIN pstone_fields ON pstone_fields.id = exstone_fields.id "
                        + "WHERE world = '" + Helper.escapeQuotes(world) + "';");

            }
            if(res != null){
                try{
                    while(res.next()){
                        try{
                            long id = res.getLong("id");
                            int status = res.getInt("status");
                            int chestx = 0, chesty = 0, chestz = 0, signx = 0, signy = 0, signz = 0;
                            //Check dormant
                            if(status != ES_DORMANT){
                                chestx = res.getInt("chestx");
                                chesty = res.getInt("chesty");
                                chestz = res.getInt("chestz");
                                signx = res.getInt("signx");
                                signy = res.getInt("signy");
                                signz = res.getInt("signz");
                            }
                            int x = res.getInt("x");
                            int y = res.getInt("y");
                            int z = res.getInt("z");
                            String recordedWorld = res.getString("world");
                            
                            if(!world.equalsIgnoreCase(recordedWorld)){
                                ExpensiveStones.infoLog("worlds don`t match, recorded world taken \nrec: " + recordedWorld + " asked: " + world);
                            }
                            
                            World thisWorld = stones.getServer().getWorld(recordedWorld);
                            
                            Location chest = new Location(thisWorld, chestx, chesty, chestz);
                            Location sign = new Location(thisWorld, signx, signy, signz);
                            Location field = new Location(thisWorld, x, y, z);
                            if(PreciousStones.getInstance().getForceFieldManager().getField(field.getBlock()) == null){
                                log.log(Level.SEVERE, " Database is invalid! Deletion Offered on ID: " + id);
                                this.offerDeletionOnLoad(id);
                                continue;
                            }
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
            //TODO dbug
            System.out.println("Loading found fields: " + fields.size());
            return fields;
        }else{
            ExpensiveStones.infoLog("Database Error, can`t connect! (selection)");
            return null;
        }
    }
        
    public void insertExpensiveField() {
        if(pendingAdditions.isEmpty())
            return;
        if(db.checkConnection()){
            for(ExpensiveField single: pendingAdditions){
                //TODO debugCode
                System.out.println(" `world` tester = " + Helper.escapeQuotes(single.getField().getWorld()));
                if(single.isDormant()){
                    if(stones.getSettingsManager().isUseMysql()){
                        db.insert("INSERT INTO `exstone_fields` ( `id`, `status`, `world` ) "
                            + "VALUES ( " + single.getField().getId() + "," + single.getStatus() + ","
                                + "'" + Helper.escapeQuotes(single.getField().getWorld()) + "');");
                    } else {
                        final Location fieldLocation = single.getField().getLocation();
                        db.insert("INSERT INTO `exstone_fields` ( `id`, `status`, "
                                + "`x`, `y`, `z`, "
                                + "`world` ) "
                            + "VALUES ( " + single.getField().getId() + "," + single.getStatus() + ","
                                + fieldLocation.getBlockX() + "," + fieldLocation.getBlockY() + "," + fieldLocation.getBlockZ() + ","
                                + "'" + Helper.escapeQuotes(single.getField().getWorld()) + "');"); 
                    }
                } else {
                    Location chest = single.getChestLocation();
                    Location sign = single.getSignLocation();
                    if(stones.getSettingsManager().isUseMysql()){
                        db.insert("INSERT INTO `exstone_fields` ( `id`, `status`, "
                                + "`chestx`, `chesty`, `chestz`, "
                                + "`signx`, `signy`, `signz`, "
                                + "`world` ) "
                            + "VALUES ( " + single.getField().getId() + "," + single.getStatus() + ","
                                + chest.getBlockX() + "," + chest.getBlockY() + "," + chest.getBlockZ() + ","
                                + sign.getBlockX() + "," + sign.getBlockY() + "," + sign.getBlockZ() + ","
                                + "'" + Helper.escapeQuotes(single.getField().getWorld()) + "');");
                    } else {
                        final Location fieldLocation = single.getField().getLocation();
                        db.insert("INSERT INTO `exstone_fields` ( `id`, `status`, "
                                + "`chestx`, `chesty`, `chestz`, "
                                + "`signx`, `signy`, `signz`, "
                                + "`x`, `y`, `z`, "
                                + "`world` ) "
                            + "VALUES ( " + single.getField().getId() + "," + single.getStatus() + ","
                                + chest.getBlockX() + "," + chest.getBlockY() + "," + chest.getBlockZ() + ","
                                + sign.getBlockX() + "," + sign.getBlockY() + "," + sign.getBlockZ() + ","
                                + fieldLocation.getBlockX() + "," + fieldLocation.getBlockY() + "," + fieldLocation.getBlockZ() + ","
                                + "'" + Helper.escapeQuotes(single.getField().getWorld()) + "');"); 
                    }
                }
            }
            pendingAdditions.clear();
        }else{
            ExpensiveStones.infoLog("Database Error, can`t connect! (addition)");
        }     
    }
    
    public void deleteExpensiveField(){
        if(pendingDeletions.isEmpty())
            return;
        if(db.checkConnection()){
            for(Long single : pendingDeletions){
                db.delete("DELETE FROM `exstone_fields` "
                        + "WHERE `id` = " + single);
            }
            pendingDeletions.clear();
        }else{
            ExpensiveStones.infoLog("Database Error, can`t connect! (deletion)");
        }
    }
    
    public void updateStatus(){
        if(pendingStatusMutations.isEmpty())
            return;
        if(db.checkConnection()){
            for(Entry single : pendingStatusMutations.entrySet()){
                db.update("UPDATE `exstone_fields` "
                        + "SET `status` = " + single.getValue()
                        + " WHERE `id` = " + single.getKey());
            }
            pendingStatusMutations.clear();
        }else{
            ExpensiveStones.infoLog("Database Error, can`t connect! (deletion)");
        }
    }
    
    public void updateField(){
        if(pendingUpdates.isEmpty())
            return;
        if(db.checkConnection()){
            for(Entry single : pendingUpdates.entrySet()){
                ExpensiveField field = (ExpensiveField) single.getValue();
                if(field.getStatus() == ES_DORMANT){
                    if(stones.getSettingsManager().isUseMysql()){
                        final Location fieldLocation = field.getField().getLocation();
                        db.update("UPDATE `exstone_fields` "
                                + "SET `status` = NULL, "
                                + "`chestx` = NULL, "
                                + "`chesty` = NULL, "
                                + "`chestz` = NULL, "
                                + "`signx` = NULL, "
                                + "`signy` = NULL, "
                                + "`signz` = NULL, " 
                                + "`x` = " + fieldLocation.getBlockX() + ", "
                                + "`y` = " + fieldLocation.getBlockY() + ", "
                                + "`z` = " + fieldLocation.getBlockZ() + ", "
                                + "`world` = '" + field.getField().getWorld() + "' "
                                + "WHERE `id` = " + single.getKey() + ";");
                    }else{
                        db.update("UPDATE `exstone_fields` "
                                + "SET `status` = " + field.getStatus() + ","
                                + "`chestx` = NULL, "
                                + "`chesty` = NULL, "
                                + "`chestz` = NULL, "
                                + "`signx` = NULL, "
                                + "`signy` = NULL, "
                                + "`signz` = NULL, " 
                                + "`world` = '" + field.getField().getWorld() + "' "
                                + "WHERE `id` = " + single.getKey() + ";");
                    }
                } else {
                    Location chest = field.getChestLocation();
                    Location sign = field.getSignLocation();
                    if(stones.getSettingsManager().isUseMysql()){
                        final Location fieldLocation = field.getField().getLocation();
                        db.update("UPDATE `exstone_fields` "
                                + "SET `status` = " + field.getStatus() + ", "
                                + "`chestx` = " + chest.getBlockX() + ", "
                                + "`chesty` = " + chest.getBlockY() + ", "
                                + "`chestz` = " + chest.getBlockZ() + ", "
                                + "`signx` = " + sign.getBlockX() + ", "
                                + "`signy` = " + sign.getBlockY() + ", "
                                + "`signz` = " + sign.getBlockZ() + ", " 
                                + "`x` = " + fieldLocation.getBlockX() + ", "
                                + "`y` = " + fieldLocation.getBlockY() + ", "
                                + "`z` = " + fieldLocation.getBlockZ() + ", "
                                + "`world` = '" + field.getField().getWorld() + "' "
                                + "WHERE `id` = " + single.getKey() + ";");
                    }else{
                        db.update("UPDATE `exstone_fields` "
                                + "SET `status` = " + field.getStatus() + ", "
                                + "`chestx` = " + chest.getBlockX() + ", "
                                + "`chesty` = " + chest.getBlockY() + ", "
                                + "`chestz` = " + chest.getBlockZ() + ", "
                                + "`signx` = " + sign.getBlockX() + ", "
                                + "`signy` = " + sign.getBlockY() + ", "
                                + "`signz` = " + sign.getBlockZ() + ", "
                                + "`world` = '" + field.getField().getWorld() + "' "
                                + "WHERE `id` = " + single.getKey() + ";");
                    }
                }
            }
            pendingUpdates.clear();
        }else{
            ExpensiveStones.infoLog("Database Error, can`t connect! (deletion)");
        }
    }
    
    public void saveAll(){
        synchronized(this){
            this.deleteExpensiveField();
            this.insertExpensiveField();
            this.updateStatus();
            this.updateField();
        }
    }
    
    public void deïnstallPart(CommandSender sender){
        if(!(sender instanceof Player))
            return;
        
        if(db.checkConnection()){
            db.execute("DROP TABLE `exstone_fields`");
        }
    }
}
