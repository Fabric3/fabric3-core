/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
