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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.management.rest.Constants;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;

/**
 * @version $Rev$ $Date$
 */
public class MarshallerImpl implements Marshaller {
    private ManagementMonitor monitor;

    public MarshallerImpl(@Monitor ManagementMonitor monitor) {
        this.monitor = monitor;
    }

    public Object[] deserialize(Verb verb, HttpServletRequest request, ResourceMapping mapping) throws ResourceException {
        Object[] params = null;
        Class<?>[] types = mapping.getMethod().getParameterTypes();
        if (types.length > 0) {
            // skip deserialization if the method does not take parameters
            if (verb == Verb.GET || verb == Verb.DELETE) {
                params = deserializeUrlParams(request, mapping);
            } else if (verb == Verb.PUT | verb == Verb.POST) {
                params = deserializeInputStream(request, mapping);
            }
        }
        return params;
    }

    public void serialize(Object value, ResourceMapping mapping, HttpServletRequest request, HttpServletResponse response) throws ResourceException {
        ClassLoader loader = mapping.getInstance().getClass().getClassLoader();
        try {
            Resource resource;
            if (value instanceof Resource) {
                resource = (Resource) value;
                if (resource.getSelfLink() == null) {
                    // set the resource self-link if it has not been
                    URL url = new URL(request.getRequestURL().toString());
                    SelfLink link = new SelfLink(url);
                    resource.setSelfLink(link);
                }
            } else {
                URL url = new URL(request.getRequestURL().toString());
                SelfLink link = new SelfLink(url);
                resource = new Resource(link);
                resource.setProperty("value", value);
            }
            byte[] output = mapping.getPair().getSerializer().transform(resource, loader);
            response.getOutputStream().write(output);
        } catch (TransformationException e) {
            Method method = mapping.getMethod();
            monitor.error("Error serializing response for " + method, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (MalformedURLException e) {
            monitor.error("Error creating URL for " + request.getRequestURL(), e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            monitor.error("Error opening response output stream", e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Deserializes resource parameters from a request URL.
     *
     * @param mapping the resource mapping
     * @param request the current request
     * @return the deserialized request body
     * @throws ResourceException if an error handling the request occurs
     */
    private Object[] deserializeUrlParams(HttpServletRequest request, ResourceMapping mapping) throws ResourceException {
        Method method = mapping.getMethod();
        Class<?>[] types = method.getParameterTypes();
        if (types.length == 1 && HttpServletRequest.class.isAssignableFrom(types[0])) {
            // if the parameter is HttpServletRequest, short-circuit deserialization
            return new Object[]{request};
        } else if (types.length == 1) {
            // parameters are encoded in the request URL
            StringBuffer requestUrl = request.getRequestURL();
            int pos = requestUrl.lastIndexOf("/");
            if (pos < 0) {
                throw new ResourceException(HttpStatus.BAD_REQUEST, "Expected parameterized URL: " + requestUrl);
            }
            String value = requestUrl.substring(pos + 1);
            if (value.length() == 0) {
                throw new ResourceException(HttpStatus.BAD_REQUEST, "Parameter must be specified: " + requestUrl);
            }
            try {
                Object deserialized = deserialize(value, method);
                return new Object[]{deserialized};
            } catch (IOException e) {
                monitor.error("Error deserializing parameters for " + method, e);
                throw new ResourceException(HttpStatus.BAD_REQUEST, "Invalid or unsupported parameter type");
            }
        } else {
            throw new ResourceException(HttpStatus.BAD_REQUEST, "Multiple parameter types not supported: " + method);
        }
    }

    /**
     * Deserializes resource parameters from a request stream.
     *
     * @param mapping the resource mapping
     * @param request the current request
     * @return the deserialized request body
     * @throws ResourceException if an error handling the request occurs
     */
    private Object[] deserializeInputStream(HttpServletRequest request, ResourceMapping mapping) throws ResourceException {
        Method method = mapping.getMethod();
        Class<?>[] types = method.getParameterTypes();
        if (types.length == 1 && HttpServletRequest.class.isAssignableFrom(types[0])) {
            // if the parameter is HttpServletRequest, short-circuit deserialization
            return new Object[]{request};
        } else if (types.length == 1) {
            Transformer<InputStream, Object> transformer;
            if (Constants.APPLICATION_JSON.equals(request.getContentType())) {
                transformer = mapping.getPair().getDeserializer();
            } else {
                throw new ResourceException(HttpStatus.BAD_REQUEST, "Content type not supported: " + request.getContentType());
            }

            try {
                ClassLoader loader = mapping.getInstance().getClass().getClassLoader();
                ServletInputStream stream = request.getInputStream();
                return new Object[]{transformer.transform(stream, loader)};
            } catch (TransformationException e) {
                monitor.error("Error deserializing parameters for " + method, e);
                throw new ResourceException(HttpStatus.BAD_REQUEST, "Invalid or unsupported parameter type");
            } catch (IOException e) {
                monitor.error("Error opening request input stream", e);
                throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading request");
            }
        } else {
            throw new ResourceException(HttpStatus.BAD_REQUEST, "Multiple parameter types not supported: " + method);
        }
    }

    Object deserialize(String value, Method method) throws IOException {
        if (method.getParameterTypes().length != 1) {
            throw new IOException("Invalid number of parameters: " + method);
        }
        Class<?> type = method.getParameterTypes()[0];
        if (String.class.equals(type)) {
            return value;
        } else if (Integer.class.equals(type) || Integer.TYPE.equals(type)) {
            return Integer.parseInt(value);
        } else if (Long.class.equals(type) || Long.TYPE.equals(type)) {
            return Long.parseLong(value);
        } else if (Double.class.equals(type) || Double.TYPE.equals(type)) {
            return Double.parseDouble(value);
        } else if (Short.class.equals(type) || Short.TYPE.equals(type)) {
            return Short.parseShort(value);
        } else if (Float.class.equals(type) || Float.TYPE.equals(type)) {
            return Float.parseFloat(value);
        }
        throw new IOException("Unsupported parameter type: " + method);
    }
}
