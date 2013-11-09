/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.monitor.spi.event;

/**
 * Holds a parameter of a method invocation. Acts as a slot to avoid object creation when passing primitives since type Object results in autoboxing.
 */
public class ParameterEntry {
    public enum Slot {
        SHORT, INT, LONG, DOUBLE, FLOAT, CHAR, BOOLEAN, BYTE, OBJECT, NONE
    }

    private Slot slot = Slot.NONE;

    private short shortValue;
    private int intValue;
    private long longValue;
    private double doubleValue;
    private float floatValue;
    private char charValue;
    private boolean booleanValue;
    private byte byteValue;
    private Object objectValue;

    public void reset() {
        slot = Slot.NONE;
        shortValue = 0;
        intValue = 0;
        longValue = 0;
        doubleValue = 0;
        floatValue = 0;
        charValue = 0;
        booleanValue = false;
        byteValue = 0;
        objectValue = null;
    }

    public Slot getSlot() {
        return slot;
    }

    public short getShortValue() {
        if (slot != Slot.SHORT) {
            throw new IllegalStateException("Slot not a short: " + slot);
        }
        return shortValue;
    }

    public void setShortValue(short shortValue) {
        slot = Slot.SHORT;
        this.shortValue = shortValue;
    }

    public int getIntValue() {
        if (slot != Slot.INT) {
            throw new IllegalStateException("Slot not an int: " + slot);
        }
        return intValue;
    }

    public void setIntValue(int intValue) {
        slot = Slot.INT;
        this.intValue = intValue;
    }

    public long getLongValue() {
        if (slot != Slot.LONG) {
            throw new IllegalStateException("Slot not a long: " + slot);
        }
        return longValue;
    }

    public void setLongValue(long longValue) {
        slot = Slot.LONG;
        this.longValue = longValue;
    }

    public double getDoubleValue() {
        if (slot != Slot.DOUBLE) {
            throw new IllegalStateException("Slot not a double: " + slot);
        }
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        slot = Slot.DOUBLE;
        this.doubleValue = doubleValue;
    }

    public float getFloatValue() {
        if (slot != Slot.FLOAT) {
            throw new IllegalStateException("Slot not a float: " + slot);
        }
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        slot = Slot.FLOAT;
        this.floatValue = floatValue;
    }

    public char getCharValue() {
        if (slot != Slot.CHAR) {
            throw new IllegalStateException("Slot not a char: " + slot);
        }
        return charValue;
    }

    public void setCharValue(char charValue) {
        slot = Slot.CHAR;
        this.charValue = charValue;
    }

    public boolean getBooleanValue() {
        if (slot != Slot.BOOLEAN) {
            throw new IllegalStateException("Slot not a boolean: " + slot);
        }
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        slot = Slot.BOOLEAN;
        this.booleanValue = booleanValue;
    }

    public byte getByteValue() {
        if (slot != Slot.BYTE) {
            throw new IllegalStateException("Slot not a byte: " + slot);
        }
        return byteValue;
    }

    public void setByteValue(byte byteValue) {
        slot = Slot.BYTE;
        this.byteValue = byteValue;
    }

    public <T> T getObjectValue(Class<T> type) {
        if (slot != Slot.OBJECT) {
            throw new IllegalStateException("Slot not an Object: " + slot);
        }
        return type.cast(objectValue);
    }

    public void setObjectValue(Object objectValue) {
        slot = Slot.OBJECT;
        this.objectValue = objectValue;
    }
}
