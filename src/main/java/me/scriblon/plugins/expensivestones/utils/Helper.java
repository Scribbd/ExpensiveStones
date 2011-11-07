/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.scriblon.plugins.expensivestones.utils;

/**
 * To help in other classes, inspired by the PreciousStoneHelper... Only lighter and fit for its function.
 * @author 5894913
 */
public class Helper {
    
    public static boolean isBoolean(Object o){
        if(o instanceof Boolean)
            return (Boolean) o;
        return false;
    }
}
