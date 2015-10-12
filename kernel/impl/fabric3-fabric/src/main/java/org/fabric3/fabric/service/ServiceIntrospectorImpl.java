package org.fabric3.fabric.service;

import java.util.Collections;
import java.util.List;

import org.fabric3.api.node.ServiceIntrospector;
import org.fabric3.spi.introspection.service.ServiceIntrospectorExtension;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ServiceIntrospectorImpl implements ServiceIntrospector {

    @Reference(required = false)
    protected List<ServiceIntrospectorExtension> extensions = Collections.emptyList();

    public boolean exportsEndpoints(Class<?> clazz) {
        for (ServiceIntrospectorExtension extension : extensions) {
            if (extension.exportsEndpoints(clazz)) {
                return true;
            }
        }
        return false;
    }
}
