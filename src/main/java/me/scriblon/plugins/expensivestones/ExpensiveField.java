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

import me.scriblon.plugins.expensivestones.managers.ESStorageManager;
import me.scriblon.plugins.expensivestones.utils.BlockUtil;
import me.scriblon.plugins.expensivestones.utils.ChestUtil;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

/**
 * A class deciated to the ExpensiveField combo. Which is a fieldblock, a chest
 * and a sign.
 * 
 * @author Coen Meulenkamp (Scriblon, ~theJaf) <coenmeulenkamp at gmail.com>
 */
public class ExpensiveField {
    // Essentials

    private int status;
    // basics
    private Sign sign;
    private Location signLocation;
    private Chest chest;
    private Location chestLocation;
    private Field field;
    // extended
    private ESFieldSettings settings;

    /**
     * Constructors based the blocks gotten from the listener. Gets the location
     * from the blocks.
     * 
     * @param sign
     *            Block which represents the sign
     * @param chest
     *            Block which represents the chest
     * @param field
     *            the original PreciousStone.Field now transformed into a
     *            ExpensiveField
     */
    public ExpensiveField(Block sign, Block chest, Field field) {
        if (isCorrectValues(sign, chest)) {
            this.status = ESStorageManager.ES_ENABLED;

            this.sign = (Sign) sign.getState();
            this.signLocation = sign.getLocation();
            this.chest = (Chest) chest.getState();
            this.chestLocation = chest.getLocation();
            this.field = field;
            this.settings = ExpensiveStones.getInstance().getESFieldManager().getESFieldSetting(field.getTypeId());
        }
    }

    /**
     * Constructor for the configurator-class where the Fields are loaded from
     * the database. It will get the blocks from the locations.
     * 
     * @param status
     *            The status in which the field was in on last save
     * @param signLocation
     *            The location of the sign of this Field
     * @param chestLocation
     *            The location of the chest of this Field
     * @param fieldLocation
     *            The location of the fieldBlock
     */
    public ExpensiveField(int status, Location signLocation,
            Location chestLocation, Location fieldLocation) {
        this.status = status;
        if (status != ESStorageManager.ES_DORMANT) {
            this.signLocation = signLocation;
            this.chestLocation = chestLocation;
            this.sign = (Sign) signLocation.getBlock().getState();
            this.chest = (Chest) chestLocation.getBlock().getState();
        } else {
            this.signLocation = null;
            this.chestLocation = null;
            this.sign = null;
            this.chest = null;
        }
        this.field = PreciousStones.getInstance().getForceFieldManager().getField(fieldLocation.getBlock());
        this.settings = ExpensiveStones.getInstance().getESFieldManager().getESFieldSetting(field.getTypeId());
    }

    /**
     * To add dormant (new) Fields. Also disables field automatically!
     * 
     * @param field
     *            The Field which has to be registered and dormanted
     */
    public ExpensiveField(Field field) {
        this.field = field;
        status = ESStorageManager.ES_DORMANT;
        settings = ExpensiveStones.getInstance().getESFieldManager().getESFieldSetting(field.getTypeId());

        field.setDisabled(true);

        sign = null;
        signLocation = null;
        chest = null;
        chestLocation = null;
    }

    /**
     * Gives the chest of this ExpensiveField
     * 
     * @return Chest of this ExpensiveField
     */
    public Chest getChest() {
        return chest;
    }

    /**
     * Gives the Field of this ExpensiveField
     * 
     * @return Field of this ExpensiveField
     */
    public Field getField() {
        return field;
    }

    /**
     * Gives the Sign of this ExpensiveField
     * 
     * @return Sign of the ExpensiveField
     */
    public Sign getSign() {
        return sign;
    }

    /**
     * Gives the Location of the chest of this ExpensiveField
     * 
     * @return Location of the chest of the ExpensiveField
     */
    public Location getChestLocation() {
        return chestLocation;
    }

    /**
     * Gives the Location of the sign of this ExpensiveField
     * 
     * @return Location of the sign of the ExpensiveField
     */
    public Location getSignLocation() {
        return signLocation;
    }

    /**
     * Gives the status this ExpensiveField is in
     * 
     * @return integer with the status of this Field
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets a new status on the ExpensiveField
     * 
     * @param status
     *            The new Status of this field
     * @return boolean if the status was successfully changed
     */
    public boolean setStatus(int status) {
        if (status == ESStorageManager.ES_ENABLED
                || status == ESStorageManager.ES_ADMIN
                || status == ESStorageManager.ES_DISABLED) {
            this.status = status;
            return true;
        } else if (status == ESStorageManager.ES_DORMANT) {
            this.status = status;
            sign = null;
            signLocation = null;
            chest = null;
            chestLocation = null;

        }
        return false;
    }

    /**
     * Get the ExpensiveFieldSettings linked to this ExpensiveField
     * 
     * @return ESFieldSettings linked to this ExpensiveField
     */
    public ESFieldSettings getSettings() {
        return settings;
    }

    // Sign commands
    /**
     * Sets a new sign
     * 
     * @param sign
     *            the new sign to set
     */
    public void setSign(Block sign) {
        this.sign = (Sign) sign;
    }

    /**
     * Sets 4rth line of sign to admin
     */
    public void setSignToOP() {
        sign.setLine(3, "<ADMIN>");
        sign.update(true);
    }

    /**
     * Sets 4rth line of sign to disabling 'in progress'
     */
    public void setSignToProgress() {
        sign.setLine(3, "<DISABLING>");
        sign.update(true);
    }

    /**
     * Sets 4rth line of sign to disabled
     */
    public void setSignToOff() {
        sign.setLine(3, "<DISABLED>");
        sign.update(true);
    }

    /**
     * Sets 4rth line of sign to enabled
     */
    public void setSignToOn() {
        sign.setLine(3, "<ENABLED>");
        sign.update(true);
    }

    public void setSignToPowered() {
        sign.setLine(3, "<POWERED>");
        sign.update(true);
    }

    /**
     * Sets 4rth line of sign to depleted
     */
    public void setSignToDepleted() {
        sign.setLine(3, "<DEPLETED>");
        sign.update(true);
    }

    /**
     * Sets 4rth line of sign to broken Means either an internal error occurred
     * concerning this field or the link is broken between stone and its
     * components
     */
    public void setError() {
        sign.setLine(3, "<BROKEN>");
        sign.update(true);
    }

    // Chest commands
    /**
     * Does the upkeeping on the chest
     */
    public void doUpkeeping() {
        ChestUtil.removeInventoryItems(chest.getInventory(),
                settings.getUpkeepStack());
        chest.update();
    }

    /**
     * Checks if chest has the required materials for the next UpkeepCycle
     * @return boolean if chest has required contents.
     */
    public boolean chestHasReqContent() {
        return ChestUtil.hasInventoryReqContent(chest.getInventory(),
                settings.getUpkeepStack());
    }

    // Checkers
    /**
     * Checks if the field is set to admin
     * @return boolean if field is an admin-field
     */
    public boolean isAdmin() {
        return status == ESStorageManager.ES_ADMIN;
    }

    /**
     * Checks if the field is set to dormant
     * @return boolean if field is a dormant-field
     */
    public boolean isDormant() {
        return status == ESStorageManager.ES_DORMANT;
    }

    /**
     * Checks if the field is set to enabled
     * @return boolean if field is an enabled-field
     */
    public boolean isActive() {
        return status == ESStorageManager.ES_ENABLED;
    }

    /**
     * Checks if the field is set to disabled
     * @return boolean if field is a disabled-field
     */
    public boolean isDisabled() {
        return status == ESStorageManager.ES_DISABLED;
    }

    // Field Togglers
    /**
     * Switches the field on
     */
    public void setFieldON() {
        field.setDisabled(false);
    }

    /**
     * Switches the field off
     */
    public void setFieldOFF() {
        field.setDisabled(true);
    }

    /**
     * returns if field is disabled
     * @return boolean if field is disabled
     */
    public boolean isFieldDisabled() {
        return field.isDisabled();
    }

    // Pirvates
    /**
     * Checks if block and chest are of the right material.
     */
    private boolean isCorrectValues(Block sign, Block chest) {
        if (BlockUtil.isSign(sign)) {
            if (BlockUtil.isChest(chest)) {
                return true;
            }
        }
        return false;
    }
}
