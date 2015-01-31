package org.fabric3.binding.rs.runtime.bytecode;

import javax.ws.rs.ext.ExceptionMapper;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.fabric3.api.host.ContainerException;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;

/**
 * Determines the exception mapper concrete type.
 *
 * Based on code from Jersey for consistency.
 */
public class RsReflectionHelper {

    @SuppressWarnings("unchecked")
    public static Class<? extends Throwable> getExceptionType(Class<? extends ExceptionMapper> mapperClass) throws ContainerException {
        final Class<?> exceptionType = getType(mapperClass);
        if (Throwable.class.isAssignableFrom(exceptionType)) {
            return (Class<? extends Throwable>) exceptionType;
        }

        throw new ContainerException("Cannot find exception mapper type:" + mapperClass);
    }

    /**
     * Get exception type for given exception mapper class.
     *
     * @param clazz class to get exception type for.
     * @return exception type for given class.
     */
    private static Class getType(Class<? extends ExceptionMapper> clazz) throws ContainerException {
        Class clazzHolder = clazz;

        while (clazzHolder != Object.class) {
            final Class type = getTypeFromInterface(clazzHolder, clazz);
            if (type != null) {
                return type;
            }

            clazzHolder = clazzHolder.getSuperclass();
        }

        throw new ContainerException("Cannot find exception mapper type:" + clazz);
    }

    /**
     * Iterate through interface hierarchy of {@code clazz} and get exception type for given class.
     *
     * @param clazz class to inspect.
     * @return exception type for given class or {@code null} if the class doesn't implement {@code ExceptionMapper}.
     */
    private static Class getTypeFromInterface(Class<?> clazz, Class<? extends ExceptionMapper> original) {
        final Type[] types = clazz.getGenericInterfaces();

        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                if (pt.getRawType() == ExceptionMapper.class || pt.getRawType() == ExtendedExceptionMapper.class) {
                    return getResolvedType(pt.getActualTypeArguments()[0], original, clazz);
                }
            } else if (type instanceof Class<?>) {
                clazz = (Class<?>) type;

                if (ExceptionMapper.class.isAssignableFrom(clazz)) {
                    return getTypeFromInterface(clazz, original);
                }
            }
        }

        return null;
    }

    private static Class getResolvedType(Type t, Class c, Class dc) {
        if (t instanceof Class) {
            return (Class) t;
        } else if (t instanceof TypeVariable) {
            final ClassTypePair ct = ReflectionHelper.resolveTypeVariable(c, dc, (TypeVariable) t);
            if (ct != null) {
                return ct.rawClass();
            } else {
                return null;
            }
        } else if (t instanceof ParameterizedType) {
            final ParameterizedType pt = (ParameterizedType) t;
            return (Class) pt.getRawType();
        } else {
            return null;
        }
    }
}
