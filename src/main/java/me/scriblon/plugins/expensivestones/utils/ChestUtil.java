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
package me.scriblon.plugins.expensivestones.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ChestUtil {
    /**
     * Credit goes to bergerkiller http://forums.bukkit.org/members/bergerkiller.96957/
     * @param inv
     * @param item 
     */
    public static void removeInventoryItems(Inventory inv, ItemStack item) {
        removeInventoryItems(inv, item.getType(), item.getAmount());
    }
    
    /**
     * Credit goes to bergerkiller http://forums.bukkit.org/members/bergerkiller.96957/
     * @param inv
     * @param type
     * @param amount 
     */
    public static void removeInventoryItems(Inventory inv, Material type, int amount) {
        for (ItemStack is : inv.getContents()) {
            if (is != null && is.getType() == type) {
                int newamount = is.getAmount() - amount;
                if (newamount > 0) {
                    is.setAmount(newamount);
                    break;
                } else {
                    inv.remove(is);
                    amount = -newamount;
                    if (amount == 0) break;
                }
            }
        }
    }
    
    public static boolean hasInventoryReqContent(Inventory inv, ItemStack item){
        return hasInventoryReqContent(inv, item.getType(), item.getAmount());
    }
    
    public static boolean hasInventoryReqContent(Inventory inv, Material type, int amount){
        int total = 0;
        for(ItemStack is : inv.getContents()){
            if (is != null && is.getType() == type) {
                total = +is.getAmount();
            }
            if(total >= amount){
                return true;
            }
        }
        return false;
    }
}
