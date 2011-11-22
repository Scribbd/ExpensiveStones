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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * A 'container'-class which is used for defining ExpensiveFields in addition to
 * the already existing PreciousFieldsSettings.
 * 
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ESFieldSettings {

	private int itemId;
	private String name;
	private Material upkeepMaterial;
	private int upkeepAmount;
	private Long upkeepPeriod;

	/**
	 * 
	 * @param itemId
	 *            The TypeId given from the block used as PreciousStone
	 * @param name
	 *            The name given from the PreciousStone
	 * @param upkeepMaterial
	 *            The Material used as fuel for the ExpensiveField
	 * @param upkeepAmount
	 *            The amount of the given Material needed to initialize the next
	 *            UpkeepPeriod.
	 * @param upkeepPeriod
	 *            The Period (in server-ticks) the ExpensiveStone can run on one
	 *            fuel-batch.
	 */
	public ESFieldSettings(int itemId, String name, Material upkeepMaterial,
			int upkeepAmount, Long upkeepPeriod) {
		this.itemId = itemId;
		this.name = name;
		this.upkeepMaterial = upkeepMaterial;
		this.upkeepAmount = upkeepAmount;
		this.upkeepPeriod = upkeepPeriod;
	}

	/**
	 * Get the amount needed to initialize the next cycle
	 * 
	 * @return integer with the amount the initiate the next UpkeepCycle
	 */
	public int getAmount() {
		return upkeepAmount;
	}

	/**
	 * Get the ItemId associated to a corresponding PreciousStone
	 * 
	 * @return integer with the ItemId of the used stone
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * Get the Material needed to initiate the next UpkeepCycle
	 * 
	 * @return Material needed to initiate the next UpkeepCycle
	 */
	public Material getMaterial() {
		return upkeepMaterial;
	}

	/**
	 * Gets the name of the PreciousStone
	 * 
	 * @return String with name of the PreciousStone
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get upkeep period in ticks before a stone needs a next batch of fuel
	 * 
	 * @return long with upkeep period.
	 */
	public long getUpkeepPeriod() {
		return upkeepPeriod;
	}

	/**
	 * Gets the itemStack needed to initiate the next UpkeepCylce
	 * 
	 * @return ItemStack needed for a next cycle.
	 */
	public ItemStack getUpkeepStack() {
		return new ItemStack(upkeepMaterial, upkeepAmount);
	}
}
