/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.management.rest;

import java.lang.reflect.Method;
import java.net.URI;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementExtension;
import org.fabric3.spi.model.type.java.ManagementInfo;
import org.fabric3.spi.model.type.java.ManagementOperationInfo;
import org.fabric3.spi.model.type.java.Signature;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.transform.TransformerFactory;

/**
 * @version $Rev$ $Date$
 */
public class RestfulManagementExtension implements ManagementExtension {
    private static final String MANAGEMENT_PATH = "/management/*";
    private static final String EMPTY_PATH = "";
    private ServletHost servletHost;
    private ManagementServlet managementServlet;
    private TransformerFactory transformerFactory;

    public RestfulManagementExtension(@Reference TransformerFactory transformerFactory, @Reference ServletHost servletHost) {
        this.transformerFactory = transformerFactory;
        this.servletHost = servletHost;
        managementServlet = new ManagementServlet();
    }

    @Init()
    public void init() {
        servletHost.registerMapping(MANAGEMENT_PATH, managementServlet);
    }

    @Destroy()
    public void destroy() {
        servletHost.unregisterMapping(MANAGEMENT_PATH);
    }

    public String getType() {
        return "fabric3.rest";
    }

    public void export(URI componentUri, ManagementInfo info, ObjectFactory<?> objectFactory, ClassLoader classLoader) throws ManagementException {
        String root = info.getPath();
        if (root.length() == 0) {
            root = componentUri.getPath();
        }
        try {
            Class<?> clazz = classLoader.loadClass(info.getManagementClass());
            for (ManagementOperationInfo operationInfo : info.getOperations()) {
                Signature signature = operationInfo.getSignature();
                Method method = signature.getMethod(clazz);
                String path = operationInfo.getPath();
                ManagedArtifactMapping mapping = createMapping(root, path, method, objectFactory);
                managementServlet.register(mapping);
            }
        } catch (ClassNotFoundException e) {
            throw new ManagementException(e);
        } catch (NoSuchMethodException e) {
            throw new ManagementException(e);
        }
    }

    public void export(String name, String group, String description, Object instance) throws ManagementException {
        String root = "/" + name;
        for (Method method : instance.getClass().getMethods()) {
            ManagementOperation annotation = method.getAnnotation(ManagementOperation.class);
            if (annotation != null) {
                ManagedArtifactMapping mapping = createMapping(root, EMPTY_PATH, method, instance);
                managementServlet.register(mapping);
            }
        }
    }

    public void remove(URI componentUri, ManagementInfo info) throws ManagementException {

    }

    public void remove(String name, String group) throws ManagementException {

    }

    /**
     * Creates a managed artifact mapping.
     *
     * @param root     the root path for the artifact
     * @param path     the relative path of the operation. The path may be blank, in which case one will be calculated from the method name
     * @param method   the management operation
     * @param instance the artifact
     * @return the mapping
     */
    private ManagedArtifactMapping createMapping(String root, String path, Method method, Object instance) {
        String methodName = method.getName();
        String pathInfo;
        if (path.length() == 0) {
            pathInfo = root + MethodHelper.convertToPath(methodName);
        } else {
            pathInfo = root + path;
        }
        Verb verb = MethodHelper.convertToVerb(methodName);
        return new ManagedArtifactMapping(pathInfo, verb, method, instance);
    }


}
