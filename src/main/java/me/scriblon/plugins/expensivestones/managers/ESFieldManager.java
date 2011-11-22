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
import java.util.List;
import java.util.Map;
import me.scriblon.plugins.expensivestones.ESFieldSettings;
import me.scriblon.plugins.expensivestones.ExpensiveField;
import me.scriblon.plugins.expensivestones.ExpensiveStones;
import me.scriblon.plugins.expensivestones.tasks.UpKeeper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * The FieldManager which manages the PreciousFields linked to this plugin.
 * @author 5894913
 */
public class ESFieldManager {
    
    public static Material STANDARD_MATERIAL = Material.REDSTONE;
    public static long STANDARD_PERIOD = 300L; //as ticks 20t is 1s = 15s
    public static int STANDARD_AMOUNT = 2; //1 stack goes 470 seconds, 7m
    
    private PreciousStones stones;
    private ESStorageManager storage;
    
    private Map<Integer, ESFieldSettings> settings = Collections.synchronizedMap(new LinkedHashMap<Integer, ESFieldSettings>());
    private Map<Location, ExpensiveField> dormantFields = Collections.synchronizedMap(new LinkedHashMap<Location, ExpensiveField>());
    private Map<Long, ExpensiveField> activeFields = Collections.synchronizedMap(new LinkedHashMap<Long, ExpensiveField>());
    private Map<Long, ExpensiveField> disabledFields = Collections.synchronizedMap(new LinkedHashMap<Long, ExpensiveField>());
    //Debug
    private Map<Long, Long> taskLink = Collections.synchronizedMap(new LinkedHashMap<Long, Long>());
    
    public ESFieldManager(){
        stones = PreciousStones.getInstance();
        storage = ExpensiveStones.getInstance().getESStorageManager();
    }
    
    //Settings related
    public void setSettings(Map<Integer, ESFieldSettings> settings){
        this.settings.putAll(settings);
    }
    
    public ESFieldSettings getESFieldSetting(int iD){
        if(settings.containsKey(iD))
            return settings.get(iD);
        return null;
    }
    
    //Adders
    public void addField(ExpensiveField field, boolean newField){
        final long id = field.getField().getId();
        if(field.isDisabled()){
            field.setStatus(ESStorageManager.ES_DISABLED);
            field.setFieldOFF();
            field.setSignToOff();
            disabledFields.put(id, field);
        } else if(field.isDormant()) {
            field.setStatus(ESStorageManager.ES_DORMANT);
            field.setFieldOFF();
            dormantFields.put(field.getField().getLocation(), field);
        } else if(field.isAdmin()) {
            field.setStatus(ESStorageManager.ES_ADMIN);
            field.setFieldON();
            field.setSignToOP();
            activeFields.put(id, field);
        } else {
            field.setStatus(ESStorageManager.ES_ENABLED);
            field.setFieldON();
            field.setSignToOn();
            activeFields.put(id, field);
        }
        if(newField){
            storage.offerAddition(field);
        }
    }
    
    public void addFields(List<ExpensiveField> fields, boolean newField){
        for(ExpensiveField field : fields){
            this.addField(field, newField);
        }
    }
    
    //Deleters
    public void removeField(ExpensiveField field){
        synchronized(this){
            Long id = field.getField().getId();
            if(activeFields.containsKey(id))
                activeFields.remove(id);
            if(disabledFields.containsKey(id))
                disabledFields.remove(id);
            if(dormantFields.containsKey(field.getField().getLocation()))
                dormantFields.remove(field.getField().getLocation());
            //TODO debugCode
            if(activeFields.containsKey(id) || disabledFields.containsKey(id) || dormantFields.containsKey(field.getField().getLocation()))
                System.out.println("Deletion Failed!");
            storage.offerDeletion(field);
        }
    }
    
    //Togglers
    public void disableField(ExpensiveField field){
        if(field.isAdmin()){
            ExpensiveStones.infoLog("(Disable)Field is Admin. Handled by PreciousStones. on ID: " + field.getField().getId());
            return;
        }
        synchronized(this){
            final long id = field.getField().getId();
            final boolean wasDormant = isInDormant(field.getField().getLocation());
            if(activeFields.containsKey(id)){
                activeFields.remove(id);
                ExpensiveStones.infoLog("(Disable)Field was active before. on ID: " + id);
            }
            if(dormantFields.containsKey(field.getField().getLocation())){
                dormantFields.remove(field.getField().getLocation());
                ExpensiveStones.infoLog("(Disable)Field was dormant before. on ID: " + id);
            }
            if(!disabledFields.containsKey(id)){
                disabledFields.put(id, field);
            } else
                ExpensiveStones.infoLog("(Disable)Field was already disabled. on ID: " + id);
            
            field.setStatus(ESStorageManager.ES_DISABLED);
            field.setFieldOFF();
            field.setSignToProgress();
            if(wasDormant)
                storage.offerUpdatedField(field);
            else
                storage.offerStatusUpdate(field);
        }       
    }
    
    public void enableField(ExpensiveField field){
        if(field.isAdmin()){
            ExpensiveStones.infoLog("(Enable)Field is Admin. Handled by PreciousStones. on ID: " + field.getField().getId());
            return;
        }
        synchronized(this){
            final long id = field.getField().getId();
            final boolean wasDormant = isInDormant(field.getField().getLocation());
            if(dormantFields.containsKey(field.getField().getLocation())){
                dormantFields.remove(field.getField().getLocation());
                ExpensiveStones.infoLog("(Enable)Field was dormant before Enable. on ID: " + id);
            }
            if(disabledFields.containsKey(id)){
                disabledFields.remove(id);
                ExpensiveStones.infoLog("(Enable)Field was disabled before Enable. on ID: " + id);
            }
            if(!activeFields.containsKey(id)){
                activeFields.put(id, field);
            } else
                ExpensiveStones.infoLog("(Enable)Field was already active before Enable. on ID: " + id);

            field.setStatus(ESStorageManager.ES_ENABLED);
            field.setFieldON();
            field.setSignToOn();
            setupUpKeeper(field);
            if(wasDormant)
                storage.offerUpdatedField(field);
            else
                storage.offerStatusUpdate(field);
        } 
    }
    
    public void setAdminField(ExpensiveField field){
        if(field.isAdmin()){
            ExpensiveStones.infoLog("(Admin)Field is already Admin. Handled by PreciousStones. on ID: " + field.getField().getId());
            return;
        }
        synchronized(this){
            final long id = field.getField().getId();
            if(disabledFields.containsKey(id)){
                disabledFields.remove(id);
                ExpensiveStones.infoLog("(Admin)Field was disabled before OP. on ID: " + id);
            }
            if(dormantFields.containsKey(field.getField().getLocation())){
                dormantFields.remove(field.getField().getLocation());
                ExpensiveStones.infoLog("(Admin)Field was dormant before OP. on ID: " + id);
            }
            if(!activeFields.containsKey(id))
                activeFields.put(id, field);
            else
                ExpensiveStones.infoLog("(Admin)Field was enabled before OP. (prob: signeditor) on ID: " + id);
            
            field.setStatus(ESStorageManager.ES_DORMANT);
            field.setStatus(ESStorageManager.ES_ADMIN);
            field.setFieldON();
            field.setSignToOP();
            storage.offerUpdatedField(field);
        }
    }
    
    public void setDormantField(ExpensiveField field){
        if(field.isAdmin()){
            ExpensiveStones.infoLog("(Dormant)Field is Admin. Handled by PreciousStones. on ID: " + field.getField().getId());
            return;
        }
        synchronized(this){
            final long id = field.getField().getId();
            if(disabledFields.containsKey(id)){
                disabledFields.remove(id);
                ExpensiveStones.infoLog("(Dormant)Field was disabled before Dromant. on ID: " + id);
            }
            if(activeFields.containsKey(id)){
                activeFields.remove(id);
                ExpensiveStones.infoLog("(Dormant)Field was enabled before Dromant. on ID: " + id);
            }
            if(!dormantFields.containsKey(field.getField().getLocation())){
                dormantFields.put(field.getField().getLocation(), field);
                ExpensiveStones.infoLog("(Dormant)Field is set Dormant. on ID: " + id);
            } else 
                ExpensiveStones.infoLog("(Dormant)Field already dormant before Dromant. on ID: " + id);
            
            field.setStatus(ESStorageManager.ES_DORMANT);
            field.setFieldOFF();
            storage.offerUpdatedField(field);
        }
    }
    
    public void setupUpKeeper(ExpensiveField field){
        UpKeeper keeper = new UpKeeper(field);
        this.setTask(keeper.scheduleMe(), field);
    }
    
    //Checkers
    public boolean isInDisabled(long id){
        return disabledFields.containsKey(id);
    }
    
    public boolean isInDormant(Location location){
        return dormantFields.containsKey(location);
    }
    
    public boolean isKnownNonDormant(long id){
        if(disabledFields.containsKey(id) && activeFields.containsKey(id))
            ExpensiveStones.infoLog("(isKnown) Field found in both maps! on id: " + id);
        return disabledFields.containsKey(id) || activeFields.containsKey(id);
    }
    
    public boolean isKnown(Block block){
        if(isInDormant(block.getLocation()))
            return true;
        if(stones.getForceFieldManager().isField(block)){
            final long id = stones.getForceFieldManager().getField(block).getId();
            //TODO debug
            System.out.println("IsKnown: debug long: " + id);
            if(isKnownNonDormant(id)){
                System.out.println("IsKnown: Got A field!");
                return true;
            }
        }
        return false;
    }
    
    public boolean isExpensiveType(int type){
        return settings.containsKey(type);
    }
    
    //Getters
    public Map<Long, ExpensiveField> getActiveFields() {
        return Collections.unmodifiableMap(activeFields);
    }

    public Map<Long, ExpensiveField> getDisabledFields() {
        return Collections.unmodifiableMap(disabledFields);
    }
    
    public Map<Location, ExpensiveField> getDormantFields(){
        return Collections.unmodifiableMap(dormantFields);
    }

    public Map<Integer, ESFieldSettings> getSettings() {
        return Collections.unmodifiableMap(settings);
    }
    
    //Advanced Getters
    
    /**
     * Get all known fields by ID
     * @return 
     */
    public Map<Long, ExpensiveField> getKnownExpensiveFieldsByID(){
        Map<Long, ExpensiveField> allFields = disabledFields;
        allFields.putAll(activeFields);
        for(ExpensiveField field : dormantFields.values()){
            allFields.put(field.getField().getId(), field);
        }
        return Collections.unmodifiableMap(allFields);
    }
    
    /**
     * Get all known fields by Location.
     * Warning can be slow!
     * @return 
     */
    public Map<Location, ExpensiveField> getKnownExpensiveFieldsByLocation(){
        Map<Location, ExpensiveField> allFields = dormantFields;
        
        for(ExpensiveField activeField : activeFields.values())
            allFields.put(activeField.getField().getLocation(), activeField);
        for(ExpensiveField disabledField : disabledFields.values())
            allFields.put(disabledField.getField().getLocation(), disabledField);
        
        return Collections.unmodifiableMap(allFields);
    }
    
    /**
     * Gets Dormant Field, Null when not pressent.
     * @param block
     * @return 
     */
    public ExpensiveField getDormantField(Block block){
        if(isInDormant(block.getLocation()))
            return dormantFields.get(block.getLocation());
        return null;
    }
    
    public ExpensiveField getNonDormantField(Block block){
        if(stones.getForceFieldManager().getField(block) == null)
            return null;
        //TODO debugCode
        System.out.println("GetNonDormant(block) got a valid request!");
        long iD = stones.getForceFieldManager().getField(block).getId();
        System.out.println("GetNonDormant(block) id test : " + iD);
        if(isKnownNonDormant(iD)){
            System.out.println("GetNonDormant(block) Got a id-lock!");
            if(isInDisabled(iD)){
                System.out.println("GetNonDormant(block) Was disabled: " + disabledFields.get(iD));
                return disabledFields.get(iD);
            } else if(activeFields.containsKey(iD)) {
                System.out.println("GetNonDormant(block) Was enabled");
                return activeFields.get(iD);
            }
        }
        System.out.println("GetNonDormant(block): Still I haven't send any!");
        return null;
    }
    
    public ExpensiveField getExpensiveField(Block block){
        System.out.println("GetExpField(Block): GetExp Request posted!");
        if(isInDormant(block.getLocation()))
            return getDormantField(block);
        else if(isKnownNonDormant(stones.getForceFieldManager().getField(block).getId())){
            System.out.println("GetExpField(Block): result isKnownDormant: " + stones.getForceFieldManager().getField(block).getId());
            return getNonDormantField(block);
        }
        //TODO debugcode
        System.out.println("GetExpField(Block): No block selected!");
        return null;
    }
    
    //!! debug feature Keeping track of all Tasks
    public Map<Long, Long> getTaskLink() {
        return Collections.unmodifiableMap(taskLink);
    }
    
    public boolean setTask(long taskID, ExpensiveField field){
        long fieldID = field.getField().getId();
        if(!taskLink.containsKey(fieldID)){
            taskLink.put(fieldID, taskID);
            return true;
        }
        ExpensiveStones.infoLog("(TaskCheck) Field was already running! On ID: " + fieldID);
        return false;                    
    }
    
    public boolean removeTask(long taskID, ExpensiveField field){
        long fieldID = field.getField().getId();
        if(taskLink.containsKey(fieldID)){
            taskLink.remove(fieldID);
            return true;
        }
        ExpensiveStones.infoLog("(TaskCheck) Field was already removed! On ID: " + fieldID);
        return false;
    }
    
    public boolean isNonePresent(){
        return activeFields.isEmpty() && disabledFields.isEmpty() && dormantFields.isEmpty();
    }
    
}
