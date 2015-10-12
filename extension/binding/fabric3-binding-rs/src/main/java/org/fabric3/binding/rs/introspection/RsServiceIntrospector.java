package org.fabric3.binding.rs.introspection;

import javax.ws.rs.Path;
import java.lang.annotation.Annotation;

import org.fabric3.spi.introspection.service.ServiceIntrospectorExtension;

/**
 * Introspects services for REST annotations.
 */
public class RsServiceIntrospector implements ServiceIntrospectorExtension {
    public boolean exportsEndpoints(Class<?> clazz) {
        for (Annotation classAnnotation : clazz.getAnnotations()) {
            if (classAnnotation instanceof Path) {
                return true;
            }
        }
        return false;
    }
}
