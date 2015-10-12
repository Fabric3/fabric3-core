package org.fabric3.fabric.service;

import java.lang.annotation.Annotation;

import org.fabric3.api.annotation.model.Binding;
import org.fabric3.spi.introspection.service.ServiceIntrospectorExtension;

/**
 * Introspects remote services for annotations decorated with the {@link Binding} annotation.
 */
public class BindingServiceIntrospector implements ServiceIntrospectorExtension {

    public boolean exportsEndpoints(Class<?> clazz) {
        for (Annotation classAnnotation : clazz.getAnnotations()) {
            for (Annotation metaAnnotation : classAnnotation.annotationType().getAnnotations()) {
                if (metaAnnotation instanceof Binding) {
                    return true;

                } else {
                    for (Annotation parentMetaAnnotations : metaAnnotation.annotationType().getAnnotations()) {
                        if (parentMetaAnnotations instanceof Binding) {
                            return true;
                        }
                    }
                }
            }

        }
        return false;
    }
}
