package org.fabric3.implementation.pojo.spi.reflection;

import java.lang.reflect.InvocationTargetException;

/**
 * Invokes an operation on a target instance.
 */
public interface TargetInvoker {

    public Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

}
