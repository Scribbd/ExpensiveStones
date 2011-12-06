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

import java.util.logging.Level;
import java.util.logging.Logger;
import me.scriblon.plugins.expensivestones.managers.Configurator;
import me.scriblon.plugins.expensivestones.listeners.ESBlockListener;
import me.scriblon.plugins.expensivestones.listeners.ESCommandExecutor;
import me.scriblon.plugins.expensivestones.listeners.ESPlayerListener;
import me.scriblon.plugins.expensivestones.listeners.ESWorldListener;
import me.scriblon.plugins.expensivestones.managers.ESFieldManager;
import me.scriblon.plugins.expensivestones.managers.ESPowerManager;
import me.scriblon.plugins.expensivestones.managers.ESStorageManager;
import me.scriblon.plugins.expensivestones.tasks.UpDater;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ExpensiveStones a plugin which adds upkeep cost to configured PreciousStones.
 * Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ExpensiveStones extends JavaPlugin {

    private static final String prefix = "[ExpensiveStones] ";
    private static final Logger log = Logger.getLogger("Minecraft");
    // Mine
    private static ExpensiveStones expStones;
    // Listeners and Commanders
    private ESBlockListener esBlockListener;
    private ESPlayerListener esPlayerListener;
    private ESCommandExecutor esCommandEx;
    private ESWorldListener esWorldListener;
    private Configurator config;
    // Managers
    private ESFieldManager eSFieldManager;
    private ESStorageManager eSStorageManager;
    private ESPowerManager eSPowerManager;

    public void onDisable() {
        eSStorageManager.saveAll();
        infoLog("is now disabled!");
    }

    public void onEnable() {
        infoLog("Starting to load!");
        expStones = this;
        // Get basic information
        final PluginManager pm = this.getServer().getPluginManager();
        // initialize Managers
        eSStorageManager = new ESStorageManager();
        eSPowerManager = new ESPowerManager();
        eSFieldManager = new ESFieldManager();
        esWorldListener = new ESWorldListener();
        config = config = new Configurator(pm);
        // Control dependencies
        if (!config.isPSAvailable()) {
            infoLog("PreciousStones not available or disabled, disabling plugin!");
            pm.disablePlugin(this);
            return;
        }
        //Do the configuration
        config.configureStones();

        // Initialize listeners and executors
        esBlockListener = new ESBlockListener();
        esPlayerListener = new ESPlayerListener();
        esCommandEx = new ESCommandExecutor();
        //Register
        this.registerEvents(pm);
        this.registerCommands();
        // Register Saving thing
        this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new UpDater(), 300L, 300L);
        // Conclude
        infoLog("Load was succesfull!");
    }

    private void registerEvents(PluginManager pm) {
        pm.registerEvent(Type.BLOCK_PLACE, esBlockListener, Priority.Highest, this);
        pm.registerEvent(Type.BLOCK_BREAK, esBlockListener, Priority.Normal, this);
        pm.registerEvent(Type.SIGN_CHANGE, esBlockListener, Priority.Normal, this);
        pm.registerEvent(Type.REDSTONE_CHANGE, esBlockListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, esPlayerListener, Priority.Normal, this);
        pm.registerEvent(Type.WORLD_LOAD, esWorldListener, Priority.Normal, this);
    }

    private void registerCommands() {
        this.getCommand("ExpensiveStones").setExecutor(esCommandEx);
    }

    /**
     * Logs an message on the Info-level.
     * @param Message String with message
     */
    public static void infoLog(String Message) {
        log.log(Level.INFO, new StringBuilder().append(prefix).append(Message).toString());
    }

    /**
     * Gives the fieldmanager linked to ExpensiveStones
     * @return the FieldManger linked to ExpensiveStones
     */
    public ESFieldManager getESFieldManager() {
        return eSFieldManager;
    }

    /**
     * Gives the storagemanager linked to ExpensiveStones
     * @return the StorageManager linked to ExpensiveStones
     */
    public ESStorageManager getESStorageManager() {
        return eSStorageManager;
    }

    public ESPowerManager getESPowerManager() {
        return eSPowerManager;
    }
    
    public Configurator getConfigurator(){
        return config;
    }

    /**
     * Gets the logger
     * @return the logger Logger
     */
    public static Logger getLogger() {
        return log;
    }

    /**
     * Get an instance of this plugin
     * @return ExpensiveStones plugin
     */
    public static ExpensiveStones getInstance() {
        return expStones;
    }
}
