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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.management.rest.Constants;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.management.rest.spi.ManagedArtifactMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;

/**
 * Responsible for dispatching requests to a managed artifact.
 *
 * @version $Rev$ $Date$
 */
public class ManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 5554150494161533656L;

    private Map<String, ManagedArtifactMapping> getMappings = new ConcurrentHashMap<String, ManagedArtifactMapping>();
    private Map<String, ManagedArtifactMapping> postMappings = new ConcurrentHashMap<String, ManagedArtifactMapping>();
    private Map<String, ManagedArtifactMapping> putMappings = new ConcurrentHashMap<String, ManagedArtifactMapping>();
    private Map<String, ManagedArtifactMapping> deleteMappings = new ConcurrentHashMap<String, ManagedArtifactMapping>();

    /**
     * Registers a mapping, making the managed resource available via HTTP.
     *
     * @param mapping the mapping
     * @throws DuplicateArtifactNameException if a managed resource has already been registered for the path
     */
    public void register(ManagedArtifactMapping mapping) throws DuplicateArtifactNameException {
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

    /**
     * Removes a mapping and the associated managed resource.
     *
     * @param mapping the mapping
     */
    public void unRegister(ManagedArtifactMapping mapping) {
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(Verb.GET, request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(Verb.POST, request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(Verb.DELETE, request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(Verb.PUT, request, response);
    }

    private void register(ManagedArtifactMapping mapping, Map<String, ManagedArtifactMapping> mappings) throws DuplicateArtifactNameException {
        String path = mapping.getPath();
        if (mappings.containsKey(path)) {
            throw new DuplicateArtifactNameException("Artifact already registered at: " + path);
        }
        mappings.put(path, mapping);
//        System.out.println("--->" + path);
    }

    private void handle(Verb verb, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo().toLowerCase();
        ManagedArtifactMapping mapping;
        if (verb == Verb.GET) {
            mapping = getMappings.get(pathInfo);
        } else if (verb == Verb.POST) {
            mapping = postMappings.get(pathInfo);
        } else if (verb == Verb.PUT) {
            mapping = putMappings.get(pathInfo);
        } else {
            mapping = deleteMappings.get(pathInfo);
        }

        if (mapping == null) {
            response.setStatus(404);
            response.getWriter().print("Management resource not found");
            return;
        }
        if (!securityCheck(mapping, response)) {
            return;
        }
        Object[] params = null;
        Class<?>[] types = mapping.getMethod().getParameterTypes();
        if (types.length > 0) {
            // avoid derserialization if the method does not take parameters
            params = deserialize(request, mapping);
        }
        Object ret = invoke(mapping, params, request);
        if (ret != null) {
            serialize(ret, request, response, mapping);
        }
    }

    private boolean securityCheck(ManagedArtifactMapping mapping, HttpServletResponse response) {
        return true;
    }

    private Object[] deserialize(HttpServletRequest request, ManagedArtifactMapping mapping) throws IOException {
        Class<?>[] types = mapping.getMethod().getParameterTypes();
        if (types.length == 1 && HttpServletRequest.class.isAssignableFrom(types[0])) {
            // if the parameter is HttpServletRequest, short-circuit deserialization
            return new Object[]{request};
        }
        Transformer<InputStream, Object> transformer;
        if (Constants.APPLICATION_XML.equals(request.getContentType())) {
            transformer = mapping.getJaxbPair().getDeserializer();
        } else {
            // default to JSON
            transformer = mapping.getJsonPair().getDeserializer();
        }

        ClassLoader loader = mapping.getInstance().getClass().getClassLoader();
        ServletInputStream stream = request.getInputStream();
        try {
            return new Object[]{transformer.transform(stream, loader)};
        } catch (TransformationException e) {
            throw new IOException(e);
        }
    }

    private void serialize(Object payload, HttpServletRequest request, HttpServletResponse response, ManagedArtifactMapping mapping)
            throws IOException {
//        String[] contentTypes;
//        String contentTypeValue = request.getHeader("Accept");
//        if (contentTypeValue == null) {
//            contentTypes = new String[]{Constants.APPLICATION_JSON};
//        } else {
//            contentTypes = contentTypeValue.split(",");
//        }
        ClassLoader loader = mapping.getInstance().getClass().getClassLoader();
        try {
            Resource resource;
            if (payload instanceof Resource) {
                resource = (Resource) payload;
            } else {
                URL url = new URL(request.getRequestURL().toString());
                SelfLink link = new SelfLink(url);
                resource = new Resource(link);
                resource.setProperty("value", payload);
            }
            byte[] output = mapping.getJsonPair().getSerializer().transform(resource, loader);
            response.getOutputStream().write(output);
        } catch (TransformationException e) {
            throw new IOException(e);
        }
    }

    private Object invoke(ManagedArtifactMapping mapping, Object[] params, HttpServletRequest request) throws IOException {
        WorkContext workContext = new WorkContext();
        WorkContext old = WorkContextTunnel.setThreadWorkContext(workContext);
        try {
            Object instance = mapping.getInstance();
            if (instance instanceof ObjectFactory) {
                instance = ((ObjectFactory) instance).getInstance();
            }
            return mapping.getMethod().invoke(instance, params);
        } catch (IllegalAccessException e) {
            // TODO return error
            throw new IOException("Error invoking operation: " + mapping.getMethod(), e);
        } catch (InvocationTargetException e) {
            // TODO return error
            Throwable target = e.getTargetException();
            target.printStackTrace();
            throw new IOException(target.getMessage(), target);
        } catch (ObjectCreationException e) {
            // TODO return error
            throw new IOException("Error invoking operation: " + mapping.getMethod(), e);
        } finally {
            WorkContextTunnel.setThreadWorkContext(old);
        }
    }


}
