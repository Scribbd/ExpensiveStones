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
import java.util.Map;
import me.scriblon.plugins.expensivestones.ESFieldSettings;
import me.scriblon.plugins.expensivestones.ExpensiveField;
import me.scriblon.plugins.expensivestones.ExpensiveStones;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

/**
 * For adding, deletion and modification off fields.
 * @author 5894913
 */
public class ESFieldManager {
    
    private PreciousStones stones;
    private ESStorageManager storage;
    
    private Map<Integer, ESFieldSettings> settings = Collections.synchronizedMap(new LinkedHashMap<Integer, ESFieldSettings>());
    
    public ESFieldManager(){
        stones = PreciousStones.getInstance();
        storage = ExpensiveStones.getInstance().getESStorageManager();
    }
    
    public ESFieldSettings getESFieldSetting(int iD){
        if(settings.containsKey(iD))
            return settings.get(iD);
        return null;
    }
    
    //Adders
    public void addESField(ExpensiveField field){
        
    }
    //Deleters
    public void removeESField(ExpensiveField field){
        
    }
    
    //Togglers
    public void disableESField(ExpensiveField field){
        
    }
    
    public void enableESField(ExpensiveField field){
        
    }
    
    public void adminESField(ExpensiveField field){
        
    }
}
