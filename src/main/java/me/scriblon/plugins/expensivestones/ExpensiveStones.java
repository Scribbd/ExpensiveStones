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

package me.scriblon.plugins.expensivestones;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.scriblon.plugins.expensivestones.managers.Configurator;
import me.scriblon.plugins.expensivestones.listeners.ESBlockListener;
import me.scriblon.plugins.expensivestones.listeners.ESCommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ExpensiveStones extends JavaPlugin {
    private final String prefix =  "[" + getDescription().getFullName() + "] ";
    private final Logger log = this.getServer().getLogger();
    
    private ESBlockListener esBlockListener;
    private ESCommandExecutor esCommandEx;
    
    public void onDisable() {
        infoLog("is now disabled!");
    }

    public void onEnable() {
        infoLog("Starting to load!");
        // Get basic information
        final PluginManager pm = this.getServer().getPluginManager();
        // Control dependencies
        Configurator config = new Configurator(pm, log);
        if(!config.isPSAvailable()){
            infoLog("PreciousStones not available, disabling plugin!");
            pm.disablePlugin(this);
            return;
        }
        //Do the configuration
        config.configureStones();
        // Initialize listeners and executors
        esBlockListener = new ESBlockListener();
        esCommandEx = new ESCommandExecutor();
        //Register
        this.registerEvents(pm);
        this.registerCommands();
        // Conclude
        infoLog("Load was succesfull!");
    }
    
    private void registerEvents(PluginManager pm){
        pm.registerEvent(Type.BLOCK_PLACE, esBlockListener, Priority.Highest, this);
        pm.registerEvent(Type.BLOCK_BREAK, esBlockListener, Priority.Highest, this);
        pm.registerEvent(Type.SIGN_CHANGE, esBlockListener, Priority.Normal, this);
    }
    
    private void registerCommands(){
        this.getCommand("es").setExecutor(esCommandEx);
    }
    
    private void infoLog(String Message){
        log.log(Level.INFO, new StringBuilder().append(prefix).append(Message).toString());
    }
}
