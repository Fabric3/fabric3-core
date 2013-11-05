package org.fabric3.binding.file.runtime;

import java.io.File;
import java.io.IOException;

import org.fabric3.api.binding.file.InvalidDataException;
import org.fabric3.api.binding.file.ServiceAdapter;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.InstanceLifecycleException;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * Delegates to a backing adaptor component. Instance creation is done lazily so that the instance is not accessed during the deployment build phase before all
 * wires have been attached. For example, a file binding may be attached before the adaptor wires are attached.
 */
public class ServiceAdaptorWrapper implements ServiceAdapter {
    private AtomicComponent component;

    public ServiceAdaptorWrapper(AtomicComponent component) {
        this.component = component;
    }

    public Object[] beforeInvoke(File file) throws InvalidDataException {
        return getInstance().beforeInvoke(file);
    }

    public void afterInvoke(File file, Object[] payload) throws IOException {
        getInstance().afterInvoke(file, payload);
    }

    public void error(File file, File errorDirectory, Exception e) throws IOException {
        getInstance().error(file, errorDirectory, e);
    }

    public void delete(File file) throws IOException {
        getInstance().delete(file);
    }

    public void archive(File file, File archiveDirectory) throws IOException {
        getInstance().archive(file, archiveDirectory);
    }

    private ServiceAdapter getInstance() {
        try {
            Object instance = component.getInstance();
            if (!(instance instanceof ServiceAdapter)) {
                String componentName = component.getName();
                throw new ServiceRuntimeException("File binding adaptor must implement " + ServiceAdapter.class.getName() + ":" + componentName);
            }
            return (ServiceAdapter) instance;
        } catch (InstanceLifecycleException e) {
            throw new ServiceRuntimeException(e);
        }
    }
}
