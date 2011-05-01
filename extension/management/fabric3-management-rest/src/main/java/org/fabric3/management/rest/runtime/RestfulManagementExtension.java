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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;

import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.Role;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.host.runtime.ParseException;
import org.fabric3.management.rest.framework.DynamicResourceService;
import org.fabric3.management.rest.spi.ResourceHost;
import org.fabric3.management.rest.spi.ResourceListener;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementExtension;
import org.fabric3.spi.model.type.java.ManagementInfo;
import org.fabric3.spi.model.type.java.ManagementOperationInfo;
import org.fabric3.spi.model.type.java.OperationType;
import org.fabric3.spi.model.type.java.Signature;
import org.fabric3.spi.model.type.json.JsonType;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.transform.TransformationException;

/**
 * Responsible for exporting components and instances as management resources.
 * <p/>
 * As part of this process, a fully-navigable management resource hierarchy will be dynamically created. For example, if a component is exported to
 * /runtime/foo/bar and a /runtime/foo resource is not configured, one will be created dynamically with a link to runtime/foo/bar. If a configured
 * resource is later exported, any previously generated dynamic resource will be overriden.
 *
 * @version $Rev$ $Date$
 */
public class RestfulManagementExtension implements ManagementExtension {
    private static final JsonType<?> JSON_INPUT_TYPE = new JsonType<Object>(InputStream.class, Object.class);
    private static final JsonType<?> JSON_OUTPUT_TYPE = new JsonType<Object>(byte[].class, Object.class);

    private static final String EMPTY_PATH = "";
    private static final String ROOT_PATH = "/";

    private TransformerPairService pairService;

    private Method rootResourceMethod;
    private Method dynamicGetResourceMethod;

    private ResourceHost resourceHost;
    private ManagementSecurity security = ManagementSecurity.DISABLED;

    private List<ResourceListener> listeners = new ArrayList<ResourceListener>();
    private Map<String, ResourceMapping> dynamicResources = new ConcurrentHashMap<String, ResourceMapping>();

    public RestfulManagementExtension(@Reference TransformerPairService pairService,
                                      @Reference Marshaller marshaller,
                                      @Reference ResourceHost resourceHost) {
        this.pairService = pairService;
        this.resourceHost = resourceHost;
    }

    @Property(required = false)
    public void setSecurity(String level) throws ParseException {
        try {
            security = ManagementSecurity.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid management security setting:" + level);
        }
    }

    /**
     * Setter to support re-injection of optional listeners.
     *
     * @param listeners the listeners
     */
    @Reference(required = false)
    public void setListeners(List<ResourceListener> listeners) {
        this.listeners = listeners;
    }

    @Init()
    public void init() throws NoSuchMethodException {
        rootResourceMethod = ResourceInvoker.class.getMethod("invoke", HttpServletRequest.class);
        dynamicGetResourceMethod = DynamicResourceService.class.getMethod("getResource", HttpServletRequest.class);
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
            List<ResourceMapping> getMappings = new ArrayList<ResourceMapping>();

            String identifier = componentUri.toString();

            boolean rootResourcePathOverride = false;
            for (ManagementOperationInfo operationInfo : info.getOperations()) {
                // calculate if a root resource needs to be created
                String path = operationInfo.getPath();
                if (ROOT_PATH.equals(path)) {
                    rootResourcePathOverride = true;
                }
            }
            for (ManagementOperationInfo operationInfo : info.getOperations()) {
                Signature signature = operationInfo.getSignature();
                Method method = signature.getMethod(clazz);
                String path = operationInfo.getPath();
                OperationType type = operationInfo.getOperationType();
                Verb verb = getVerb(method, type);

                Set<Role> roles = operationInfo.getRoles();
                if (roles.isEmpty()) {
                    // No roles specified for operation. Default to read/write roles specified on the class. 
                    if (Verb.GET == verb) {
                        roles = info.getReadRoles();
                    } else {
                        roles = info.getWriteRoles();
                    }
                }
                TransformerPair pair = pairService.getTransformerPair(Collections.singletonList(method), JSON_INPUT_TYPE, JSON_OUTPUT_TYPE);
                ResourceMapping mapping = createMapping(identifier, root, path, method, verb, objectFactory, pair, roles);
                if (Verb.GET == mapping.getVerb()) {
                    getMappings.add(mapping);
                }

                createDynamicResources(mapping, root, rootResourcePathOverride);

                if (dynamicResources.remove(mapping.getPath()) != null) {
                    resourceHost.unregisterPath(mapping.getPath(), mapping.getVerb());
                }
                resourceHost.register(mapping);
                notifyExport(path, mapping);
            }
            if (!rootResourcePathOverride) {
                createRootResource(identifier, root, getMappings);
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
        String root = "/runtime/" + name;
        try {

            Set<Role> readRoles = new HashSet<Role>();
            Set<Role> writeRoles = new HashSet<Role>();
            parseRoles(instance, readRoles, writeRoles);

            boolean rootResourcePathOverride = false;
            List<ResourceMapping> getMappings = new ArrayList<ResourceMapping>();

            List<Method> methods = Arrays.asList(instance.getClass().getMethods());
            for (Method method : methods) {
                ManagementOperation opAnnotation = method.getAnnotation(ManagementOperation.class);
                if (opAnnotation == null) {
                    continue;
                }
                String path = opAnnotation.path();
                if (ROOT_PATH.equals(path)) {
                    rootResourcePathOverride = true;
                }

            }
            for (Method method : methods) {
                Set<Role> roles;
                ManagementOperation opAnnotation = method.getAnnotation(ManagementOperation.class);
                if (opAnnotation != null) {
                    OperationType type = OperationType.valueOf(opAnnotation.type().toString());
                    Verb verb = getVerb(method, type);
                    String[] rolesAllowed = opAnnotation.rolesAllowed();
                    if (rolesAllowed.length == 0) {
                        if (Verb.GET == verb) {
                            roles = readRoles;
                        } else {
                            roles = writeRoles;
                        }
                    } else {
                        roles = new HashSet<Role>();
                        for (String roleName : rolesAllowed) {
                            roles.add(new Role(roleName));
                        }
                    }

                    TransformerPair pair = pairService.getTransformerPair(Collections.singletonList(method), JSON_INPUT_TYPE, JSON_OUTPUT_TYPE);
                    ResourceMapping mapping = createMapping(name, root, EMPTY_PATH, method, verb, instance, pair, roles);

                    if (Verb.GET == mapping.getVerb()) {
                        getMappings.add(mapping);
                    }

                    createDynamicResources(mapping, root, rootResourcePathOverride);

                    resourceHost.register(mapping);
                    notifyExport(mapping.getRelativePath(), mapping);

                }
            }
            if (!rootResourcePathOverride) {
                createRootResource(name, root, getMappings);
            }
        } catch (TransformationException e) {
            throw new ManagementException(e);
        }
    }

    public void remove(URI componentUri, ManagementInfo info) throws ManagementException {
        String identifier = componentUri.toString();
        resourceHost.unregister(identifier);
        for (ResourceListener listener : listeners) {
            listener.onRootResourceRemove(identifier);
            listener.onSubResourceRemove(identifier);
        }
    }

    public void remove(String name, String group) throws ManagementException {
        resourceHost.unregister(name);
        for (ResourceListener listener : listeners) {
            listener.onRootResourceRemove(name);
            listener.onSubResourceRemove(name);
        }
    }

    /**
     * Returns the HTTP verb for the operation.
     *
     * @param method the method name
     * @param type   the operation type
     * @return the HTTP verb
     */
    private Verb getVerb(Method method, OperationType type) {
        String methodName = method.getName();
        if (OperationType.UNDEFINED == type) {
            return MethodHelper.convertToVerb(methodName);
        } else {
            return Verb.valueOf(type.toString());
        }
    }

    /**
     * Notifies listeners of a root or sub- resource export event
     *
     * @param path    the resource path
     * @param mapping the resource mapping
     */
    private void notifyExport(String path, ResourceMapping mapping) {
        if (ROOT_PATH.equals(path)) {
            for (ResourceListener listener : listeners) {
                listener.onRootResourceExport(mapping);
            }
        } else {
            for (ResourceListener listener : listeners) {
                listener.onSubResourceExport(mapping);
            }
        }
    }


    /**
     * Creates a managed artifact mapping.
     *
     * @param identifier the identifier used to group a set of mappings during deployment and undeployment
     * @param root       the root path for the artifact
     * @param path       the relative path of the operation. The path may be blank, in which case one will be calculated from the method name
     * @param method     the management operation
     * @param verb       the HTTP verb the operation uses
     * @param instance   the artifact
     * @param pair       the transformer pair for deserializing JSON requests and serializing responses
     * @param roles      the roles required to invoke the operation
     * @return the mapping
     */
    private ResourceMapping createMapping(String identifier,
                                          String root,
                                          String path,
                                          Method method,
                                          Verb verb,
                                          Object instance,
                                          TransformerPair pair,
                                          Set<Role> roles) {
        String methodName = method.getName();
        String rootPath;
        if (path.length() == 0) {
            path = MethodHelper.convertToPath(methodName);
        }
        path = path.toLowerCase();
        if (ROOT_PATH.equals(path)) {
            // if the path is for the root resource, there is no sub-path
            rootPath = root.toLowerCase();
        } else {
            rootPath = root.toLowerCase() + "/" + path;
        }
        return new ResourceMapping(identifier, rootPath, path, verb, method, instance, pair, roles);
    }

    /**
     * Creates a root resource that aggreggates information from sub-resources.
     *
     * @param identifier the identifier used to group a set of mappings during deployment and undeployment
     * @param root       the root path
     * @param mappings   the sub-resource mappings   @throws ManagementException if an error occurs creating the root resource
     * @throws ManagementException if there is an error creating the mapping
     */
    private void createRootResource(String identifier, String root, List<ResourceMapping> mappings) throws ManagementException {
        try {
            ResourceInvoker invoker = new ResourceInvoker(mappings, security);
            List<Method> methods = new ArrayList<Method>();
            for (ResourceMapping mapping : mappings) {
                methods.add(mapping.getMethod());
            }
            TransformerPair pair = pairService.getTransformerPair(methods, JSON_INPUT_TYPE, JSON_OUTPUT_TYPE);
            root = root.toLowerCase();
            Set<Role> roles = Collections.emptySet();
            ResourceMapping mapping = new ResourceMapping(identifier, root, root, Verb.GET, rootResourceMethod, invoker, pair, roles);
            ResourceMapping previous = dynamicResources.remove(root);
            if (previous != null) {
                resourceHost.unregisterPath(previous.getPath(), Verb.GET);
            }
            resourceHost.register(mapping);
            for (ResourceListener listener : listeners) {
                listener.onRootResourceExport(mapping);
            }
            createDynamicResources(mapping, root, false);
        } catch (TransformationException e) {
            throw new ManagementException(e);
        }
    }

    /**
     * Parses read and write roles specified on an {@link Management} annotation.
     *
     * @param instance   the instance containing the annotation
     * @param readRoles  the collection of read roles to populate
     * @param writeRoles the collection of write roles to populate
     */
    private void parseRoles(Object instance, Set<Role> readRoles, Set<Role> writeRoles) {
        Management annotation = instance.getClass().getAnnotation(Management.class);
        if (annotation != null) {
            String[] readRoleNames = annotation.readRoles();
            for (String roleName : readRoleNames) {
                readRoles.add(new Role(roleName));
            }
            String[] writeRoleNames = annotation.writeRoles();
            for (String roleName : writeRoleNames) {
                writeRoles.add(new Role(roleName));
            }
        }

        // set default roles if none specified
        if (readRoles.isEmpty()) {
            readRoles.add(new Role(Management.FABRIC3_ADMIN_ROLE));
            readRoles.add(new Role(Management.FABRIC3_OBSERVER_ROLE));
        }
        if (writeRoles.isEmpty()) {
            writeRoles.add(new Role(Management.FABRIC3_ADMIN_ROLE));
        }
    }

    /**
     * Creates parent resources dynamically for the given mapping if they do not already exist.
     *
     * @param mapping            the mapping
     * @param rootResourcePath   the root resource path for this hierarchy
     * @param createRootResource true if a dynamic root resource should be dynamically created
     * @throws ManagementException if there was an error creating parent resources
     */
    private void createDynamicResources(ResourceMapping mapping, String rootResourcePath, boolean createRootResource) throws ManagementException {
        ResourceMapping previous = dynamicResources.remove(mapping.getPath());
        if (previous != null) {
            // A dynamic resource service was already registered. Remove it since it is being replaced by a configured resource service.
            resourceHost.unregisterPath(previous.getPath(), previous.getVerb());
        } else {
            List<ResourceMapping> dynamicMappings = createDynamicResourceMappings(mapping, rootResourcePath, createRootResource);
            for (ResourceMapping dynamicMapping : dynamicMappings) {
                // add the resources as listeners first as parents need to be notified of children in order to generate links during registration
                listeners.add((ResourceListener) dynamicMapping.getInstance());
            }
            for (ResourceMapping dynamicMapping : dynamicMappings) {
                if (!resourceHost.isPathRegistered(dynamicMapping.getPath(), dynamicMapping.getVerb())) {
                    resourceHost.register(dynamicMapping);
                    notifyExport(dynamicMapping.getRelativePath(), dynamicMapping);
                }
            }
        }
    }

    /**
     * Creates a collection of parent resources dynamically for the given mapping if they do not already exist.
     *
     * @param mapping            the mapping
     * @param rootResourcePath   the root resource path for this hierarchy
     * @param createRootResource true if a dynamic root resource should be dynamically created
     * @return the an ordered collection of resources starting with the top-most resource in the hierarchy
     */
    private List<ResourceMapping> createDynamicResourceMappings(ResourceMapping mapping, String rootResourcePath, boolean createRootResource) {
        String path = mapping.getPath();
        List<ResourceMapping> mappings = new ArrayList<ResourceMapping>();
        while (path != null) {
            String current = PathHelper.getParentPath(path);
            if (path.equals(current)) {
                // reached the path hierarchy root or the top
                break;
            } else if (dynamicResources.containsKey(current)) {
                break;
            } else if (resourceHost.isPathRegistered(current, Verb.GET)) {
                break;
            }
            path = current;
            try {
                if (!createRootResource && current.equals(rootResourcePath)) {
                    continue; // skip creating the root resource since one is provided
                }
                DynamicResourceService resourceService = new DynamicResourceService(current);
                List<Method> list = Collections.singletonList(dynamicGetResourceMethod);
                TransformerPair pair = pairService.getTransformerPair(list, JSON_INPUT_TYPE, JSON_OUTPUT_TYPE);
                ResourceMapping dynamicMapping = new ResourceMapping(current,
                                                                     current,
                                                                     ROOT_PATH,
                                                                     Verb.GET,
                                                                     dynamicGetResourceMethod,
                                                                     resourceService,
                                                                     pair,
                                                                     mapping.getRoles());
                mappings.add(dynamicMapping);
                dynamicResources.put(dynamicMapping.getPath(), dynamicMapping);
            } catch (TransformationException e) {
                throw new AssertionError(e);
            }
        }
        // reverse the collection to register the top-most parent first
        Collections.reverse(mappings);
        return mappings;
    }


}
