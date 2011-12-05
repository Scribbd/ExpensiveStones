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
    
	//TODO Subjected to be moved to ExpensiveField
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
    
    /**
     * Constructor
     */
    public ESStorageManager(){
        stones = PreciousStones.getInstance();
        db = DBFactory.produceDB();
    }
    
    /**
     * Checks if the ExpensiveField table is present in table
     * @return true if table is present
     */
    public boolean dbHasExpensive(){
        return db.existsTable("exstone_fields");
    }
    
    /**
     * Adds table to database.
     * Subjected to be optimized
     */
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
    /**
     * Adds ExpensiveField to insertionChache
     * @param expField Field to be added
     */
    public void offerAddition(ExpensiveField expField){
        pendingAdditions.add(expField);
    }
    
    /**
     * Adds ExpensiveField to StatusUpdateChache
     * Might be subjected to be removed
     * @param expField Field of which the status has to be updated
     */
    public void offerStatusUpdate(ExpensiveField expField){
        long id = expField.getField().getId();
        if(pendingUpdates.containsKey(id))
            pendingUpdates.put(expField.getField().getId(), expField);
        else
            pendingStatusMutations.put(expField.getField().getId(), expField.getStatus());
    }
    
    /**
     * Adds ExpensiveField to whole update chase.
     * @param expField Field to be updated as a whole
     */
    public void offerUpdatedField(ExpensiveField expField){
        Long id = expField.getField().getId();
        if(pendingStatusMutations.containsKey(id))
            pendingStatusMutations.remove(id);
        pendingUpdates.put(id, expField);
    }
    
    /**
     * Adds ExpensiveField to whole deletion chase by the ExpensiveField
     * Used by the importer
     * @param expField Field to be deleted
     */
    public void offerDeletionByID(ExpensiveField expField){
        pendingDeletions.add(expField.getField().getId());
    }
    
    /**
     * Adds ExpensiveField to whole deletion chase by id
     * Subject to be refracted.
     * @param expField Field to be deleted
     */
     private void offerDeletionOnId(Long id){
    	 pendingDeletions.add(id);
    }
    
    //TODO subjected to be removed
    public boolean checkWholeField(ExpensiveField expField){
        return pendingUpdates.containsKey(expField.getField().getId());
    }
    

    // Executors
    /**
     * Gets the fields out of the database
     * Subjected to be Optimized
     * @param world String with the worldname
     * @return List of ExpensiveFields
     */
    public List<ExpensiveField> getExpensiveFields(String world){
        String query = "SELECT exstone_fields.id as id, "
                + "status, "
                + "chestx, chesty, chestz, "
                + "signx, signy, signz, "
                + "exstone_fields.world as world, "
                + "x, y, z "
                + "FROM exstone_fields ";
        if(stones.getSettingsManager().isUseMysql())
            query = query + "INNER JOIN pstone_fields ON pstone_fields.id = exstone_fields.id ";
        query = query + "WHERE world = '" + Helper.escapeQuotes(world) + "';";
        
        if(db.checkConnection()){
            List<ExpensiveField> fields = new ArrayList<ExpensiveField>();
            ResultSet res =  db.select(query);

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
                                this.offerDeletionOnId(id);
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
    
    /**
     * Inserts the chace into the database
     * Subjected to be optimized
     */
    public void insertExpensiveField() {
        if(pendingAdditions.isEmpty())
            return;
        
        if(db.checkConnection()){
            final boolean useMySQL = stones.getSettingsManager().isUseMysql();
            for(ExpensiveField single: pendingAdditions){
                final boolean isDormant = single.isDormant();
                //Build INSERTS
                String query = "INSERT INTO `exstone_fields` ( `id`, `status`, `world`";
                if(!isDormant)
                    query = query + ", `chestx`, `chesty`, `chestz`, "
                                + "`signx`, `signy`, `signz` ";
                if(!useMySQL)
                    query = query + ", `x`, `y`, `z` ";
                query = query + ") VALUES ( ";
                //build VALUES
                query = query + single.getField().getId() + "," + single.getStatus() + ","
                    + "'" + Helper.escapeQuotes(single.getField().getWorld()) + "'";
                if(!isDormant){
                    final Location chest = single.getChestLocation();
                    final Location sign = single.getSignLocation();
                    query = query + ","
                            + chest.getBlockX() + "," + chest.getBlockY() + "," + chest.getBlockZ() + ","
                            + sign.getBlockX() + "," + sign.getBlockY() + "," + sign.getBlockZ();
                }
                if(!useMySQL){
                    final Location field = single.getField().getLocation();
                    query = query + ","
                            + field.getBlockX() + "," + field.getBlockY() + "," + field.getBlockZ();
                }
                
                query = query + "); ";
                
                db.insert(query);
            }
            pendingAdditions.clear();
        }else{
            ExpensiveStones.infoLog("Database Error, can`t connect! (addition)");
        }     
    }
    
    /**
     * Deletes all fields in chase.
     * Subjected to be optimized
     */
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
    
    /**
     * Push updates to the database. Only status will get updated.
     * Subjected to be optimized
     */
    public void updateStatus(){
        if(pendingStatusMutations.isEmpty())
            return;
        if(db.checkConnection()){
            for(Entry<Long, Integer> single : pendingStatusMutations.entrySet()){
                db.update("UPDATE `exstone_fields` "
                        + "SET `status` = " + single.getValue()
                        + " WHERE `id` = " + single.getKey());
            }
            pendingStatusMutations.clear();
        }else{
            ExpensiveStones.infoLog("Database Error, can`t connect! (deletion)");
        }
    }
    
    /**
     * Push whole fields to the database.
     * Subjected to be optimized
     */
    public void updateField(){
        if(pendingUpdates.isEmpty())
            return;
        if(db.checkConnection()){
            for(Entry<Long, ExpensiveField> single : pendingUpdates.entrySet()){
                final ExpensiveField field = single.getValue();
                final boolean isDormant = field.isDormant();
                final boolean useMySQL = stones.getSettingsManager().isUseMysql();
                //Build start
                String query = "UPDATE `exstone_fields` "
                                + "SET `status` = " + field.getStatus() + ", ";
                if(isDormant){
                    query = query + "`chestx` = NULL, "
                                + "`chesty` = NULL, "
                                + "`chestz` = NULL, "
                                + "`signx` = NULL, "
                                + "`signy` = NULL, "
                                + "`signz` = NULL, "; 
                }else{
                    Location chest = field.getChestLocation();
                    Location sign = field.getSignLocation();
                    query = query + "`chestx` = " + chest.getBlockX() + ", "
                                + "`chesty` = " + chest.getBlockY() + ", "
                                + "`chestz` = " + chest.getBlockZ() + ", "
                                + "`signx` = " + sign.getBlockX() + ", "
                                + "`signy` = " + sign.getBlockY() + ", "
                                + "`signz` = " + sign.getBlockZ() + ", ";
                }
                if(!useMySQL){
                    final Location fieldLocation = field.getField().getLocation();
                    query = query + "`x` = " + fieldLocation.getBlockX() + ", "
                                + "`y` = " + fieldLocation.getBlockY() + ", "
                                + "`z` = " + fieldLocation.getBlockZ() + ", ";
                }
                
                query = query + "`world` = '" + field.getField().getWorld() + "' "
                                + "WHERE `id` = " + single.getKey() + ";";
                db.update(query);
            }
            pendingUpdates.clear();
        }else{
            ExpensiveStones.infoLog("Database Error, can`t connect! (update)");
        }
    }
    
    /**
     * Pushes all changes to the database.
     */
    public void saveAll(){
        synchronized(this){
            this.deleteExpensiveField();
            this.insertExpensiveField();
            this.updateStatus();
            this.updateField();
        }
    }
    
    /**
     * Drops the table
     * !!Only use when you are sure to drop the table.
     * @param sender Needs check of Player is in here.
     */
    public void deïnstallPart(CommandSender sender){
        if(!(sender instanceof Player))
            return;
        
        if(db.checkConnection()){
            db.execute("DROP TABLE `exstone_fields`");
        }
    }
}
