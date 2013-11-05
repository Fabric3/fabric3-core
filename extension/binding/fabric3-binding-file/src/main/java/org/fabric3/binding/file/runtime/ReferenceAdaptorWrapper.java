package org.fabric3.binding.file.runtime;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.fabric3.api.binding.file.ReferenceAdapter;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.InstanceLifecycleException;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * Delegates to a backing adaptor component. Instance creation is done lazily so that the instance is not accessed during the deployment build phase before all
 * wires have been attached. For example, a file binding may be attached before the adaptor wires are attached.
 */
public class ReferenceAdaptorWrapper implements ReferenceAdapter {
    private AtomicComponent component;

    public ReferenceAdaptorWrapper(AtomicComponent component) {
        this.component = component;
    }

    public OutputStream createOutputStream(File file) throws IOException {
        return getInstance().createOutputStream(file);
    }

    private ReferenceAdapter getInstance() {
        try {
            Object instance = component.getInstance();
            if (!(instance instanceof ReferenceAdapter)) {
                String componentName = component.getName();
                throw new ServiceRuntimeException("File binding adaptor must implement " + ReferenceAdapter.class.getName() + ":" + componentName);
            }
            return (ReferenceAdapter) instance;
        } catch (InstanceLifecycleException e) {
            throw new ServiceRuntimeException(e);
        }
    }

}
