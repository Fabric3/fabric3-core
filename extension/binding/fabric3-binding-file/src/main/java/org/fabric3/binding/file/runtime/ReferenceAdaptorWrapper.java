package org.fabric3.binding.file.runtime;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.file.api.ReferenceAdapter;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.invocation.WorkContext;

/**
 * Delegates to a backing adaptor component. Instance creation is done lazily so that the instance is not accessed during the deployment build phase
 * before all wires have been attached. For example, a file binding may be attached before the adaptor wires are attached.
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
        WorkContext context = new WorkContext();
        try {
            Object instance = component.getInstance(context);
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
