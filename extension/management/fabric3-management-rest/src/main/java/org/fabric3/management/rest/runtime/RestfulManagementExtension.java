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
package org.fabric3.management.rest.runtime;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementExtension;
import org.fabric3.spi.model.type.java.ManagementInfo;
import org.fabric3.spi.model.type.java.ManagementOperationInfo;
import org.fabric3.spi.model.type.java.Signature;
import org.fabric3.spi.model.type.json.JsonType;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.transform.TransformationException;

/**
 * @version $Rev$ $Date$
 */
public class RestfulManagementExtension implements ManagementExtension {
    private static final JsonType<?> JSON_INPUT_TYPE = new JsonType<Object>(InputStream.class, Object.class);
    private static final JsonType<?> JSON_OUTPUT_TYPE = new JsonType<Object>(byte[].class, Object.class);
    private static final QName XSD_ANY = new QName(XSDType.XSD_NS, "anyType");
    private static final DataType<?> XSD_INPUT_TYPE = new XSDType(InputStream.class, XSD_ANY);
    private static final DataType<?> XSD_OUTPUT_TYPE = new XSDType(byte[].class, XSD_ANY);

    private static final String MANAGEMENT_PATH = "/management/*";
    private static final String EMPTY_PATH = "";
    private static final String ROOT_PATH = "/";

    private Method rootResourceMethod;
    private ServletHost servletHost;
    private ManagementServlet managementServlet;
    private TransformerPairService pairService;

    public RestfulManagementExtension(@Reference TransformerPairService pairService, @Reference ServletHost servletHost) {
        this.pairService = pairService;
        this.servletHost = servletHost;
        managementServlet = new ManagementServlet();
    }

    @Init()
    public void init() throws NoSuchMethodException {
        rootResourceMethod = ResourceInvoker.class.getMethod("invoke", HttpServletRequest.class);
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
            List<Method> methods = new ArrayList<Method>();
            boolean rootResourcePathOverride = false;
            List<ManagedArtifactMapping> getMappings = new ArrayList<ManagedArtifactMapping>();
            for (ManagementOperationInfo operationInfo : info.getOperations()) {
                Signature signature = operationInfo.getSignature();
                Method method = signature.getMethod(clazz);
                methods.add(method);
            }
            TransformerPair jsonPair = pairService.getTransformerPair(methods, JSON_INPUT_TYPE, JSON_OUTPUT_TYPE);
            TransformerPair jaxbPair = pairService.getTransformerPair(methods, XSD_INPUT_TYPE, XSD_OUTPUT_TYPE);
            for (ManagementOperationInfo operationInfo : info.getOperations()) {
                Signature signature = operationInfo.getSignature();
                Method method = signature.getMethod(clazz);
                String path = operationInfo.getPath();
                if (ROOT_PATH.equals(path)) {
                    // TODO support override of root resource path
                    rootResourcePathOverride = true;
                }
                ManagedArtifactMapping mapping = createMapping(root, path, method, objectFactory, jsonPair, jaxbPair);
                if (Verb.GET == mapping.getVerb()) {
                    getMappings.add(mapping);
                }
                managementServlet.register(mapping);
            }
            if (!rootResourcePathOverride) {
                createRootResource(root, getMappings);
            }
        } catch (ClassNotFoundException e) {
            throw new ManagementException(e);
        } catch (NoSuchMethodException e) {
            throw new ManagementException(e);
        } catch (TransformationException e) {
            throw new ManagementException(e);
        }
    }

    public void export(String name, String group, String description, Object instance) throws ManagementException {
        String root = "/" + name;
        try {
            List<Method> methods = Arrays.asList(instance.getClass().getMethods());
            TransformerPair jsonPair = pairService.getTransformerPair(methods, JSON_INPUT_TYPE, JSON_OUTPUT_TYPE);
            TransformerPair jaxbPair = pairService.getTransformerPair(methods, XSD_INPUT_TYPE, XSD_OUTPUT_TYPE);

            for (Method method : methods) {
                ManagementOperation annotation = method.getAnnotation(ManagementOperation.class);
                if (annotation != null) {

                    ManagedArtifactMapping mapping = createMapping(root, EMPTY_PATH, method, instance, jsonPair, jaxbPair);
                    managementServlet.register(mapping);
                }
            }
        } catch (TransformationException e) {
            throw new ManagementException(e);
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
     * @param jsonPair the transformer pair for deserializing JSON requests and serializing responses as JSON
     * @param jaxbPair the transformer pair for deserializing XML-based requests and serializing responses as XML
     * @return the mapping
     */
    private ManagedArtifactMapping createMapping(String root,
                                                 String path,
                                                 Method method,
                                                 Object instance,
                                                 TransformerPair jsonPair,
                                                 TransformerPair jaxbPair) {
        String methodName = method.getName();
        String pathInfo;
        if (path.length() == 0) {
            pathInfo = root + MethodHelper.convertToPath(methodName);
        } else {
            pathInfo = root + path;
        }
        Verb verb = MethodHelper.convertToVerb(methodName);
        return new ManagedArtifactMapping(pathInfo, verb, method, instance, jsonPair, jaxbPair);
    }

    /**
     * Creates a root resource that aggreggates information from sub-resources.
     *
     * @param root     the root path
     * @param mappings the sub-resource mappings
     * @throws ManagementException if an error occurs creating the root resource
     */
    private void createRootResource(String root, List<ManagedArtifactMapping> mappings) throws ManagementException {
        try {
            ResourceInvoker invoker = new ResourceInvoker(mappings);
            List<Method> methods = new ArrayList<Method>();
            for (ManagedArtifactMapping mapping : mappings) {
                methods.add(mapping.getMethod());
            }
            TransformerPair jsonPair = pairService.getTransformerPair(methods, JSON_INPUT_TYPE, JSON_OUTPUT_TYPE);
            TransformerPair jaxbPair = pairService.getTransformerPair(methods, XSD_INPUT_TYPE, XSD_OUTPUT_TYPE);
            ManagedArtifactMapping mapping = new ManagedArtifactMapping(root, Verb.GET, rootResourceMethod, invoker, jsonPair, jaxbPair);
            managementServlet.register(mapping);
        } catch (TransformationException e) {
            throw new ManagementException(e);
        }
    }

}
