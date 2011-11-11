/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.scriblon.plugins.expensivestones;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ESFieldSettings {
    
    private int id;
    private String name;
    private Material material;
    private int amount;
    private Long upkeepPeriod;

    public ESFieldSettings(int id, String name, Material material, int amount, Long upkeepPeriod) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.amount = amount;
        this.upkeepPeriod = upkeepPeriod;
    }

    public int getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public Long getUpkeepPeriod() {
        return upkeepPeriod;
    }
    
    public ItemStack getUpkeepStack(){
        return new ItemStack(material, amount);
    }
}
