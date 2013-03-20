package org.fabric3.implementation.pojo.spi.reflection;

import java.lang.reflect.InvocationTargetException;

/**
 * Invokes an operation on a target service instance.
 */
public interface ServiceInvoker {

    /**
     * Invoke the target service instance.
     *
     * @param obj  the target object
     * @param args the parameters
     * @return the return value
     * @throws IllegalAccessException    if there is an access violation invoking th target
     * @throws IllegalArgumentException  if there is a parameter error
     * @throws InvocationTargetException if the target throws an exception
     */
    public Object invoke(Object obj, Object args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

}
