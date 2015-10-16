package org.fabric3.resource.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.fabric3.resource.provision.PhysicalApplicationResource;
import org.fabric3.spi.container.builder.ResourceBuilder;
import org.oasisopen.sca.annotation.Service;

@Service({ApplicationResourceRegistry.class, ResourceBuilder.class})
public class ApplicationResourceBuilder implements ResourceBuilder<PhysicalApplicationResource>, ApplicationResourceRegistry {
    private Map<String, Supplier> resources = new HashMap<>();

    public void build(PhysicalApplicationResource definition) {
        String name = definition.getName();
        Supplier<?> resource = definition.getSupplier();
        resources.put(name, resource);
    }

    public void remove(PhysicalApplicationResource definition) {
        resources.remove(definition.getName());
    }

    public Supplier<?> getResourceFactory(String name) {
        return resources.get(name);
    }
}
