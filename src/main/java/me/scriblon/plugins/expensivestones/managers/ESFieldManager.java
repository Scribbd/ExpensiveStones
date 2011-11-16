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
 * For adding, deletion and modification off fields.
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
        if(field.isDisabled())
            disabledFields.put(field.getField().getId(), field);
        else if(field.isDormant()){
        ;
        } else {
            activeFields.put(field.getField().getId(), field);
        }
        if(newField && field.getSign() != null){
            //TODO dormant fields project
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
            storage.offerDeletion(field);
        }
    }
    
    //Togglers
    public void disableField(ExpensiveField field){
        if(field.isAdmin()){
            ExpensiveStones.infoLog("(PreciousStones:Disable)Field is Admin. Handled by PreciousStones. on ID: " + field.getField().getId());
            return;
        }
        synchronized(this){
            Long id = field.getField().getId();
            if(activeFields.containsKey(id)){
                activeFields.remove(id);
                ExpensiveStones.infoLog("(PreciousStones:Disable)Field was active before. on ID: " + field.getField().getId());
            }
            if(dormantFields.containsKey(field.getField().getLocation())){
                dormantFields.remove(field.getField().getLocation());
                ExpensiveStones.infoLog("(PreciousStones:Disable)Field was dormant before. on ID: " + field.getField().getId());
            }
            if(!disabledFields.containsKey(id)){
                disabledFields.put(id, field);
            } else
                ExpensiveStones.infoLog("(PreciousStones:Disable)Field was already disabled. on ID: " + field.getField().getId());
            
            field.setStatus(ESStorageManager.ES_DISABLED);
            field.setFieldOFF();
            storage.offerStatusUpdate(field);
        }       
    }
    
    public void enableField(ExpensiveField field){
        if(field.isAdmin()){
            ExpensiveStones.infoLog("(PreciousStones:Enable)Field is Admin. Handled by PreciousStones. on ID: " + field.getField().getId());
            return;
        }
        synchronized(this){
            Long id = field.getField().getId();
            if(dormantFields.containsKey(field.getField().getLocation())){
                dormantFields.remove(field.getField().getLocation());
                ExpensiveStones.infoLog("(PreciousStones:Enable)Field was dormant before Enable. on ID: " + field.getField().getId());
            }
            if(disabledFields.containsKey(id)){
                disabledFields.remove(id);
                ExpensiveStones.infoLog("(PreciousStones:Enable)Field was disabled before Enable. on ID: " + field.getField().getId());
            }
            if(!activeFields.containsKey(id)){
                activeFields.put(id, field);
            } else
                ExpensiveStones.infoLog("(PreciousStones:Enable)Field was already active before Enable. on ID: " + field.getField().getId());

            field.setStatus(ESStorageManager.ES_ENABLED);
            field.setFieldON();
            storage.offerStatusUpdate(field);
        } 
    }
    
    public void setAdminField(ExpensiveField field){
        if(field.isAdmin()){
            ExpensiveStones.infoLog("(PreciousStones:Admin)Field is already Admin. Handled by PreciousStones. on ID: " + field.getField().getId());
            return;
        }
        synchronized(this){
            Long id = field.getField().getId();
            if(disabledFields.containsKey(id)){
                disabledFields.remove(id);
                ExpensiveStones.infoLog("(PreciousStones:Admin)Field was disabled before OP. on ID: " + field.getField().getId());
            }
            if(dormantFields.containsKey(field.getField().getLocation())){
                dormantFields.remove(field.getField().getLocation());
                ExpensiveStones.infoLog("(PreciousStones:Admin)Field was dormant before OP. on ID: " + field.getField().getId());
            }
            if(!activeFields.containsKey(id))
                activeFields.put(id, field);
            else
                ExpensiveStones.infoLog("(PreciousStones:Admin)Field was enabled before OP. (prob: signeditor) on ID: " + field.getField().getId());
            
            field.setStatus(ESStorageManager.ES_ADMIN);
            field.setFieldON();
            storage.offerStatusUpdate(field);
        }
    }
    
    public void setDormantField(ExpensiveField field){
        if(field.isAdmin()){
            ExpensiveStones.infoLog("(PreciousStones:Dormant)Field is Admin. Handled by PreciousStones. on ID: " + field.getField().getId());
            return;
        }
        synchronized(this){
            Long id = field.getField().getId();
            if(disabledFields.containsKey(id)){
                disabledFields.remove(id);
                ExpensiveStones.infoLog("(PreciousStones:Dorm)Field was disabled before Dromant. on ID: " + field.getField().getId());
            }
            if(activeFields.containsKey(id)){
                activeFields.remove(id);
                ExpensiveStones.infoLog("(PreciousStones:Dorm)Field was enabled before Dromant. on ID: " + field.getField().getId());
            }
            if(!dormantFields.containsKey(field.getField().getLocation())){
                dormantFields.put(field.getField().getLocation(), field);
                ExpensiveStones.infoLog("(PreciousStones:Dorm)Field was disabled before Dromant. on ID: " + field.getField().getId());
            } else 
                ExpensiveStones.infoLog("(PreciousStones:Dorm)Field already dormant before Dromant. on ID: " + field.getField().getId());
            
            field.setStatus(ESStorageManager.ES_DORMANT);
            field.setFieldOFF();
            storage.offerUpdatedField(field);
        }
    }
    
    public void setupUpKeeper(ExpensiveField field){
        UpKeeper keeper = new UpKeeper(field);
        this.setTask(keeper.scheduleMeFreeTick(), field);
    }
    
    //Checkers
    public boolean isInDisabled(long id){
        return disabledFields.containsKey(id);
    }
    
    public boolean isKnown(long id){
        if(disabledFields.containsKey(id) && activeFields.containsKey(id))
            ExpensiveStones.infoLog("(isKnown) Field found in both maps! on id: " + id);
        return disabledFields.containsKey(id) || activeFields.containsKey(id);
    }
    
    public boolean isKnown(Block block){
        return isKnown(getExpensiveField(block).getField().getId());
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

    public Map<Integer, ESFieldSettings> getSettings() {
        return Collections.unmodifiableMap(settings);
    }
    
    //Advanced Getters
    public Map<Long, ExpensiveField> getKnownExpensiveFields(){
        Map<Long, ExpensiveField> allFields = disabledFields;
        allFields.putAll(activeFields);
        return Collections.unmodifiableMap(allFields);
    }
    
    public ExpensiveField getExpensiveField(Block block){
        ExpensiveField output = null;
        //TODO HELUP!
        return output;
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
    
    
}
