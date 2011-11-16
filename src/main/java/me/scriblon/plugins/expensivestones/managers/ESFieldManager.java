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
    private Map<Long, ExpensiveField> activeFields = Collections.synchronizedMap(new LinkedHashMap<Long, ExpensiveField>());
    private Map<Long, ExpensiveField> disabledFields = Collections.synchronizedMap(new LinkedHashMap<Long, ExpensiveField>());
    //TODO redesign this, bloated and not needen. (phead.vector.Field knows when it is dissabled.)
    //Might switch over to Map<Integer (status), Map<Long, ExpensiveField>>
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
        if(field.getStatus() == ESStorageManager.ES_DISABLED)
            disabledFields.put(field.getField().getId(), field);
        else{
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
            storage.offerDeletion(field);
        }
    }
    
    //Togglers
    public void disableField(ExpensiveField field){
        synchronized(this){
            Long id = field.getField().getId();
            if(activeFields.containsKey(id) && field.getStatus() != ESStorageManager.ES_ADMIN){
                activeFields.remove(id);
                disabledFields.put(id, field);
                if(!field.getField().isDisabled())
                    field.getField().setDisabled(true);
                else
                    ExpensiveStones.infoLog("Field was already dissabled! on ID: " + field.getField().getId());
                field.setStatus(ESStorageManager.ES_DISABLED);
            }
        }       
    }
    
    public void enableField(ExpensiveField field){
        synchronized(this){
            Long id = field.getField().getId();
            if(disabledFields.containsKey(id)){
                disabledFields.remove(id);
                activeFields.put(id, field);
                if(field.getField().isDisabled())
                    field.getField().setDisabled(false);
                else
                    ExpensiveStones.infoLog("Field was already enabled! on ID: " + field.getField().getId());
                field.setStatus(ESStorageManager.ES_ENABLED);
            }
        } 
    }
    
    public void adminESField(ExpensiveField field){
        synchronized(this){
            Long id = field.getField().getId();
            if(disabledFields.containsKey(id)){
                disabledFields.remove(id);
                ExpensiveStones.infoLog("Field was disabled before OP. on ID: " + field.getField().getId());
            }
            if(!activeFields.containsKey(id)){
                activeFields.put(id, field);
                ExpensiveStones.infoLog("Field was enabled before OP. (prob: signeditor) on ID: " + field.getField().getId());
            }
            if(field.getField().isDisabled()){
                field.getField().setDisabled(false);
                ExpensiveStones.infoLog("(PreciousStones)Field was disabled. on ID: " + field.getField().getId());
            } else
                ExpensiveStones.infoLog("(PreciousStones)Field was enabled. on ID: " + field.getField().getId());
            field.setStatus(ESStorageManager.ES_ADMIN);
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
