package org.fabric3.implementation.pojo.spi.reflection;

import java.lang.reflect.InvocationTargetException;

/**
 * Invokes a destination method on a consumer.
 */
public interface ConsumerInvoker {

    /**
     * Invokes the consumer method on the given instance passing the given event.
     *
     * @param obj the target object
     * @param event the event
     * @return the return value
     * @throws IllegalAccessException    if there is an access violation invoking th target
     * @throws IllegalArgumentException  if there is a parameter error
     * @throws InvocationTargetException if the target throws an exception
     */
    public Object invoke(Object obj, Object event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

}
