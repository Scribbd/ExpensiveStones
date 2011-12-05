/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.scriblon.plugins.expensivestones.listeners;

import java.util.List;
import me.scriblon.plugins.expensivestones.ExpensiveField;
import me.scriblon.plugins.expensivestones.ExpensiveStones;
import me.scriblon.plugins.expensivestones.managers.ESFieldManager;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.World;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

/**
 *
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ESWorldListener extends WorldListener{
    
    private final ExpensiveStones plugin;
    private final PreciousStones stones;
    
    public ESWorldListener(){
        plugin = ExpensiveStones.getInstance();
        stones = PreciousStones.getInstance();
    }
    
    @Override
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();

        if(stones.getSettingsManager().isBlacklistedWorld(world))
        {
            return;
        }
        //TODO make method in the right class
        List<ExpensiveField> fields = plugin.getESStorageManager().getExpensiveFields(world.getName());
        final ESFieldManager manager = plugin.getESFieldManager();
        for(ExpensiveField field : fields)
            ;
    }
    
    
    
}
