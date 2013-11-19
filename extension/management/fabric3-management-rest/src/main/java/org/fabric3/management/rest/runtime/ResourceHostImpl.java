/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.Role;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.ParseException;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.model.Response;
import org.fabric3.management.rest.spi.DuplicateResourceNameException;
import org.fabric3.management.rest.spi.ResourceHost;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.spi.federation.topology.MessageException;
import org.fabric3.spi.federation.topology.ParticipantTopologyService;
import org.fabric3.spi.federation.topology.ZoneChannelException;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.BasicAuthenticator;
import org.fabric3.spi.security.NoCredentialsException;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ResourceHostImpl extends HttpServlet implements ResourceHost {
    private static final long serialVersionUID = 5554150494161533656L;

    private static final String RESOURCE_CHANNEL = "resourceChannel";
    private static final String MANAGEMENT_PATH = "/management/*";

    private Marshaller marshaller;
    private ServletHost servletHost;
    private BasicAuthenticator authenticator;
    private ManagementMonitor monitor;

    private ParticipantTopologyService topologyService;

    private ManagementSecurity security = ManagementSecurity.DISABLED;
    private Set<Role> roles = new HashSet<Role>();
    private boolean disableHttp;

    private Map<String, ResourceMapping> getMappings = new ConcurrentHashMap<String, ResourceMapping>();
    private Map<String, ResourceMapping> postMappings = new ConcurrentHashMap<String, ResourceMapping>();
    private Map<String, ResourceMapping> putMappings = new ConcurrentHashMap<String, ResourceMapping>();
    private Map<String, ResourceMapping> deleteMappings = new ConcurrentHashMap<String, ResourceMapping>();

    private Map<String, List<ResourceMapping>> registered = new ConcurrentHashMap<String, List<ResourceMapping>>();

    public ResourceHostImpl(@Reference Marshaller marshaller,
                            @Reference ServletHost servletHost,
                            @Reference BasicAuthenticator authenticator,
                            @Monitor ManagementMonitor monitor) {
        this.marshaller = marshaller;
        this.servletHost = servletHost;
        this.authenticator = authenticator;
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setSecurity(String level) throws ParseException {
        try {
            security = ManagementSecurity.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid management security setting:" + level);
        }
    }

    @Property(required = false)
    public void setRoles(String rolesAttribute) {
        String[] rolesString = rolesAttribute.split(",");
        for (String s : rolesString) {
            roles.add(new Role(s.trim()));
        }
    }

    @Property(required = false)
    public void setDisableHttp(boolean disableHttp) {
        this.disableHttp = disableHttp;
    }

    @Reference(required = false)
    public void setTopologyService(ParticipantTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Init
    public void start() throws ZoneChannelException {
        servletHost.registerMapping(MANAGEMENT_PATH, this);
        if (topologyService != null) {
            ResourceReplicationHandler handler = new ResourceReplicationHandler(this, monitor);
            topologyService.openChannel(RESOURCE_CHANNEL, null, handler);
        }
        if (ManagementSecurity.DISABLED == security) {
            monitor.securityDisabled();
        }
        if (!disableHttp) {
            monitor.httpEnabled();
        }
    }

    @Destroy()
    public void stop() throws ZoneChannelException {
        servletHost.unregisterMapping(MANAGEMENT_PATH);
        if (topologyService != null) {
            topologyService.closeChannel(RESOURCE_CHANNEL);
        }
    }

    public void init() {
    }

    public boolean isPathRegistered(String path, Verb verb) {
        if (verb == Verb.GET) {
            return getMappings.containsKey(path);
        } else if (verb == Verb.POST) {
            return postMappings.containsKey(path);
        } else if (verb == Verb.PUT) {
            return putMappings.containsKey(path);
        } else {
            return verb == Verb.DELETE && deleteMappings.containsKey(path);
        }
    }

    public void register(ResourceMapping mapping) throws DuplicateResourceNameException {
        Verb verb = mapping.getVerb();
        if (verb == Verb.GET) {
            register(mapping, getMappings);
        } else if (verb == Verb.POST) {
            register(mapping, postMappings);
        } else if (verb == Verb.PUT) {
            register(mapping, putMappings);
        } else if (verb == Verb.DELETE) {
            register(mapping, deleteMappings);
        }
    }

    public void unregister(String identifier) {
        List<ResourceMapping> list = registered.remove(identifier);
        if (list == null) {
            return;
        }
        for (ResourceMapping mapping : list) {
            String path = mapping.getPath();
            Verb verb = mapping.getVerb();
            if (verb == Verb.GET) {
                getMappings.remove(path);
            } else if (verb == Verb.POST) {
                postMappings.remove(path);
            } else if (verb == Verb.PUT) {
                putMappings.remove(path);
            } else if (verb == Verb.DELETE) {
                deleteMappings.remove(path);
            }
        }
    }

    public void unregisterPath(String path, Verb verb) {
        ResourceMapping mapping;
        if (verb == Verb.GET) {
            mapping = getMappings.remove(path);
        } else if (verb == Verb.POST) {
            mapping = postMappings.remove(path);
        } else if (verb == Verb.PUT) {
            mapping = putMappings.remove(path);
        } else {
            mapping = deleteMappings.remove(path);
        }
        if (mapping != null) {
            String identifier = mapping.getIdentifier();
            List<ResourceMapping> mappings = registered.get(identifier);
            mappings.remove(mapping);
        }
    }

    public void dispatch(String path, Verb verb, Object[] params) {
        ResourceMapping mapping = resolveMapping(verb, path);
        if (mapping == null) {
            // this should not happen
            monitor.error("Mapping not found during zone broadcast: " + path);
            return;
        }
        WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
        try {
            invoke(mapping, params, false, workContext);
        } catch (ResourceException e) {
            monitor.error("Error replicating resource request: " + mapping.getMethod(), e);
        } finally {
            workContext.reset();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        handle(Verb.GET, request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        handle(Verb.POST, request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        handle(Verb.DELETE, request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        handle(Verb.PUT, request, response);
    }

    /**
     * Registers a resource mapping with the servlet.
     *
     * @param mapping  the resource mapping
     * @param mappings the resource mappings for an HTTP verb
     * @throws DuplicateResourceNameException if a resource for the path is already registered
     */
    private void register(ResourceMapping mapping, Map<String, ResourceMapping> mappings) throws DuplicateResourceNameException {
        String path = mapping.getPath();
        if (mappings.containsKey(path)) {
            throw new DuplicateResourceNameException("Resource already registered at: " + path);
        }
        String identifier = mapping.getIdentifier();
        List<ResourceMapping> registered = getRegistered(identifier);
        registered.add(mapping);
        mappings.put(path, mapping);
    }

    private List<ResourceMapping> getRegistered(String name) {
        List<ResourceMapping> list = registered.get(name);
        if (list == null) {
            list = new ArrayList<ResourceMapping>();
            registered.put(name, list);
        }
        return list;
    }

    /**
     * Resolves the resource mapping for a request and handles it. An exact path match will be attempted first when resolving the mapping and, if not found,
     * resolution will be done using the parent path. For example, a parameterized path such as /messages/message/1 will first attempt to resolve using the full
     * path and if not found will resolve using /messages/message.
     *
     * @param verb     the HTTP verb
     * @param request  the current request
     * @param response the current response
     */
    private void handle(Verb verb, HttpServletRequest request, HttpServletResponse response) {
        if (disableHttp && !request.isSecure()) {
            response.setStatus(HttpStatus.FORBIDDEN.getCode());
            try {
                response.getWriter().write("Forbidden. Only HTTPS access is allowed");
            } catch (IOException e) {
                monitor.error("Error writing response");
            }
            return;
        }
        if (request.getPathInfo() == null) {
            response.setStatus(HttpStatus.NOT_FOUND.getCode());
            return;
        }
        String pathInfo = parsePathInfo(request);
        ResourceMapping mapping = resolveMapping(verb, pathInfo);
        if (mapping == null) {
            response.setStatus(404);
            try {
                response.getWriter().print("Management resource not found");
            } catch (IOException e) {
                monitor.error("Error writing response", e);
            }
            return;
        }
        WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();

        try {
            if (!securityCheck(mapping, request, response, workContext)) {
                return;
            }

            Object[] params = marshaller.deserialize(verb, request, mapping);
            Object value = invoke(mapping, params, true, workContext);
            respond(value, mapping, request, response);
        } catch (ResourceException e) {
            respondError(e, mapping, response);
        } finally {
            workContext.reset();
        }
    }

    /**
     * Parses the path info so it can be used to resolve the requested resource.
     *
     * @param request the current request
     * @return the parsed path info
     */
    private String parsePathInfo(HttpServletRequest request) {
        String pathInfo = request.getPathInfo().toLowerCase();
        if (pathInfo.endsWith("/")) {
            // strip trailing '/'
            pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
        }
        if (pathInfo.startsWith("/management/")) {
            // strip the leading '/management' path as some servlet containers (e.g. Tomcat) include it
            pathInfo = pathInfo.substring(11);
        }
        return pathInfo;
    }

    /**
     * Resolves the resource mapping for a verb/path pair
     *
     * @param verb the HTTP verb
     * @param path the HTTP path
     * @return the resource mapping or null if not found
     */
    private ResourceMapping resolveMapping(Verb verb, String path) {
        ResourceMapping mapping;
        if (verb == Verb.GET) {
            mapping = resolve(path, getMappings);
        } else if (verb == Verb.POST) {
            mapping = resolve(path, postMappings);
        } else if (verb == Verb.PUT) {
            mapping = resolve(path, putMappings);
        } else {
            mapping = resolve(path, deleteMappings);
        }
        return mapping;
    }

    /**
     * Resolves a mapping by walking a path hierarchy and matching against registered mappings. For example, resolution of the path /foo/bar/baz will be done in
     * the following order: /foo/bar/baz; /foo/bar; and /foo.
     *
     * @param path     the path
     * @param mappings the registered mappings
     * @return a mating mapping or null
     */
    private ResourceMapping resolve(String path, Map<String, ResourceMapping> mappings) {
        // flag to allow exact matching on non-parameterized mappings. Only true for the first iteration, when an exact match is attempted
        boolean start = true;
        while (path != null) {
            ResourceMapping mapping = mappings.get(path);
            if (mapping != null && (start || mapping.isParameterized())) {
                return mapping;
            }
            start = false;
            String current = PathHelper.getParentPath(path);
            if (path.equals(current)) {
                // reached the path hierarchy root
                break;
            }
            path = current;
        }
        return null;
    }

    /**
     * checks the current client has credentials to execute the management operation if security is enabled.
     *
     * @param mapping     the resource mapping
     * @param request     the current request
     * @param response    the response
     * @param workContext the current work context
     * @return true if the clients has the required credentials
     */
    private boolean securityCheck(ResourceMapping mapping, HttpServletRequest request, HttpServletResponse response, WorkContext workContext) {
        if (ManagementSecurity.DISABLED == security) {
            return true;
        }
        try {
            authenticator.authenticate(request, workContext);
        } catch (NoCredentialsException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.getCode());
            response.setHeader("WWW-Authenticate", "Basic realm=\"fabric3\"");
            return false;
        } catch (AuthenticationException e) {
            setUnauthorizedResponse(response);
            return false;
        }
        if (ManagementSecurity.AUTHORIZATION == security) {
            // check access to management interface
            if (!checkSubjectHasRole(workContext, roles)) {
                setUnauthorizedResponse(response);
                return false;
            }

            // check access to the specific operation
            if (!checkSubjectHasRole(workContext, mapping.getRoles())) {
                setUnauthorizedResponse(response);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the current subject has a role in the set of provided roles if the latter is not empty.
     *
     * @param workContext the current work context
     * @param roles       the roles to check
     * @return true if the current subject has a role
     */
    private boolean checkSubjectHasRole(WorkContext workContext, Set<Role> roles) {
        if (roles.isEmpty()) {
            return true;
        }
        boolean authorized = false;
        for (Role role : workContext.getSubject().getRoles()) {
            if (roles.contains(role)) {
                authorized = true;
                break;
            }
        }
        return authorized;
    }

    /**
     * Constructs an HTTP unauthorized response.
     *
     * @param response the response
     */
    private void setUnauthorizedResponse(HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.getCode());
        try {
            response.getWriter().write("Unauthorized");
        } catch (IOException e) {
            monitor.error("Error writing response", e);
        }
    }

    /**
     * Invokes a resource.
     *
     * @param mapping     the resource mapping
     * @param params      the deserialized request parameters
     * @param replicate   true if the request should be replicated
     * @param workContext the current work context
     * @return a return value or null
     * @throws ResourceException if an error invoking the resource occurs
     */
    private Object invoke(ResourceMapping mapping, Object[] params, boolean replicate, WorkContext workContext) throws ResourceException {
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            Object instance = mapping.getInstance();
            if (instance instanceof ObjectFactory) {
                instance = ((ObjectFactory) instance).getInstance();
            }
            Thread.currentThread().setContextClassLoader(instance.getClass().getClassLoader());
            Object ret = mapping.getMethod().invoke(instance, params);
            if (replicate) {
                replicate(mapping, params);
            }
            return ret;
        } catch (IllegalAccessException e) {
            monitor.error("Error invoking operation: " + mapping.getMethod(), e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof ResourceException) {
                // resource exceptions are returned to the client
                throw (ResourceException) target;
            }
            monitor.error("Error invoking operation: " + mapping.getMethod(), e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ObjectCreationException e) {
            monitor.error("Error invoking operation: " + mapping.getMethod(), e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (MessageException e) {
            monitor.error("Error replicating operation: " + mapping.getMethod(), e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    /**
     * Replicates a request to all participants in a zone.
     *
     * @param mapping the request mapping
     * @param params  the request parameters
     * @throws MessageException if there is a replication error
     */
    private void replicate(ResourceMapping mapping, Object[] params) throws MessageException {
        if (topologyService != null && mapping.isReplicate() && mapping.getVerb() != Verb.GET) {
            // only replicate if running on a participant and request is not HTTP GET 
            ReplicationEnvelope envelope;
            if (params.length > 0 && params[0] instanceof HttpServletRequest) {
                HttpServletRequest oldRequest = (HttpServletRequest) params[0];
                ReplicatedHttpServletRequest request = copyRequest(oldRequest);
                envelope = new ReplicationEnvelope(mapping.getPath(), mapping.getVerb(), new Object[]{request});
            } else {
                envelope = new ReplicationEnvelope(mapping.getPath(), mapping.getVerb(), params);
            }
            topologyService.sendAsynchronous(RESOURCE_CHANNEL, envelope);
        }
    }

    /**
     * Returns a response to the client.
     *
     * @param value    the return value
     * @param mapping  the resource mapping
     * @param request  the current request
     * @param response the current response
     * @throws ResourceException if an error sending the response
     */
    private void respond(Object value, ResourceMapping mapping, HttpServletRequest request, HttpServletResponse response) throws ResourceException {
        response.setContentType("application/json");
        if (value instanceof Response) {
            Response resourceResponse = (Response) value;
            for (Map.Entry<String, String> entry : resourceResponse.getHeaders().entrySet()) {
                response.setHeader(entry.getKey(), entry.getValue());
            }
            response.setStatus(resourceResponse.getStatus().getCode());
            Object entity = resourceResponse.getEntity();
            if (entity != null) {
                marshaller.serialize(entity, mapping, request, response);
            }
        } else if (value != null) {
            marshaller.serialize(value, mapping, request, response);
        }
    }

    /**
     * Returns an error response to the client.
     *
     * @param e        the error
     * @param mapping  the resource mapping
     * @param response the current response
     */
    private void respondError(ResourceException e, ResourceMapping mapping, HttpServletResponse response) {
        response.setContentType("application/json");
        for (Map.Entry<String, String> entry : e.getHeaders().entrySet()) {
            response.setHeader(entry.getKey(), entry.getValue());
        }
        response.setStatus(e.getStatus().getCode());
        try {
            String message = e.getMessage();
            Object entity = e.getEntity();
            if (entity != null) {
                // transform the error entity
                Transformer<Object, byte[]> transformer = mapping.getPair().getSerializer();
                byte[] serialized = transformer.transform(entity, entity.getClass().getClassLoader());
                response.getOutputStream().write(serialized);
            } else {
                if (message != null) {
                    // transform the error message
                    Transformer<Object, byte[]> transformer = mapping.getPair().getSerializer();
                    byte[] serialized = transformer.transform(message, this.getClass().getClassLoader());
                    response.getOutputStream().write(serialized);
                }
            }
        } catch (IOException ex) {
            monitor.error("Cannot write error response", ex);
            monitor.error("Response was ", e);
        } catch (TransformationException ex) {
            monitor.error("Cannot serialize error response", ex);
            monitor.error("Response was ", e);
        }
    }

    /**
     * Copies the current request to a serializable representation used during replication.
     *
     * @param request the request
     * @return the copied request
     */
    private ReplicatedHttpServletRequest copyRequest(HttpServletRequest request) {
        ReplicatedHttpServletRequest newRequest = new ReplicatedHttpServletRequest();
        newRequest.setLocalAddr(request.getLocalAddr());
        newRequest.setContentType(request.getContentType());
        newRequest.setMethod(request.getMethod());
        newRequest.setPort(request.getLocalPort());
        newRequest.setProtocol(request.getProtocol());
        newRequest.setRequestUri(request.getRequestURI());
        newRequest.setRequestUrl(request.getRequestURL());
        newRequest.setScheme(request.getScheme());
        newRequest.setServerName(request.getServerName());
        newRequest.setServletPath(request.getServletPath());
        return newRequest;
    }

}
