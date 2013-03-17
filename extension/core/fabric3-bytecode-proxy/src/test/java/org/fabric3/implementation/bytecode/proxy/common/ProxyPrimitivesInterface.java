package org.fabric3.implementation.bytecode.proxy.common;

/**
 *
 */
public interface ProxyPrimitivesInterface {

    boolean getBooleanPrimitive(boolean param);

    byte getBytePrimitive(byte param);

    short getShortPrimitive(short param);

    int getIntPrimitive(int param);

    long getLongPrimitive(long param);

    float getFloatPrimitive(float param);

    double getDoublePrimitive(double param);

    int[] getIntArray(int[] params);

}
