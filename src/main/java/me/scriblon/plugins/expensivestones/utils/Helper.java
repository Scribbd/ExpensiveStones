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

/**
 * To help in other classes, inspired by the PreciousStoneHelper... Only lighter and fit for its function.
 * @author 5894913
 */
public class Helper {
    
	/**
	 * Tests and converts boolean.
	 * @param o Object subjected to the test
	 * @return either true if the object is boolean and contains a true, or false when it isn't.
	 */
    public static boolean convertBoolean(Object o){
        if(o instanceof Boolean)
            return (Boolean) o;
        return false;
    }
    
    /**
     * Tests if object is an integer
     * @param o Object subjected to the test
     * @return true when object is integer
     */
    public static boolean isInteger(Object o){
        return o instanceof Integer;
    }
    
    /**
     * Tests if object is a String
     * @param o Object subjected to the test
     * @return true when object is integer
     */
    public static boolean isString(Object o){
        return o instanceof String;
    }
    
    /**
     * Converts integers to longs.
     * @param v Integer to be converted
     * @return the integer converted to long
     */
    public static long convertInteger(int v){
        return (long) v;
    }
    
    public static boolean isLong(String s){
        try{
            Long.parseLong(s);
        } catch(Exception e) {
            return false;
        }
        return true;
    }
    
    /**
     * Escapes single quotes
     * 1 on 1 copy of ExpensiveStones.
     * Credits to the team of PreciousStones
     * @param str
     * @return
     */
    public static String escapeQuotes(String str)
    {
        if (str == null)
        {
            return "";
        }
        str = str.replace("'", "''");
        return str;
    }
}
