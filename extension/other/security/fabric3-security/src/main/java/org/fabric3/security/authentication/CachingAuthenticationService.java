/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.security.authentication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.SecuritySubject;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.json.JsonType;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.UsernamePasswordToken;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Performs authentication and populates the current work context and HTTP session with the authenticated subject.
 * <p/>
 * Authentication and logon are performed using an HTTP POST, using one of three content types:
 * <pre>
 * <ul>
 * <li>HTTP FORM data
 * <li>HTTP POST data encoded as JSON
 * <li>HTTP POST data encoded as XML/JAXB
 * </ul>
 *
 * <pre>
 * Logout is performed by performing an HTTP DELETE.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
@EagerInit
public class CachingAuthenticationService extends HttpServlet {
    private static final long serialVersionUID = -3247111411539759436L;

    private static final String FABRIC3_SUBJECT = "fabric3.subject";

    private final static String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private final static String APPLICATION_JSON = "application/json";
    private final static String APPLICATION_XML = "application/xml";

    private final static DataType JSON_TYPE = new JsonType(InputStream.class);
    private static final DataType XML_TYPE = new XSDType(String.class, new QName(XSDType.XSD_NS, "string"));
    private final static JavaType JAVA_TYPE = new JavaType(UsernamePasswordToken.class);

    private AuthenticationService authService;
    private ServletHost host;
    private AuthMonitor monitor;
    private boolean enabled = true;
    private boolean allowHttp;
    private TransformerRegistry registry;
    private Transformer<InputStream, UsernamePasswordToken> jsonTransformer;
    private Transformer<InputStream, UsernamePasswordToken> xmlTransformer;

    public CachingAuthenticationService(@Reference AuthenticationService authService,
                                        @Reference TransformerRegistry registry,
                                        @Reference ServletHost host,
                                        @Monitor AuthMonitor monitor) {
        this.authService = authService;
        this.registry = registry;
        this.host = host;
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Property(required = false)
    public void setAllowHttp(boolean allow) {
        this.allowHttp = allow;
    }

    @Init
    public void start() {
        if (!enabled) {
            return;
        }
        host.registerMapping("/fabric/security/token", this);
    }

    /**
     * Authenticates a client and caches the authenticated subject in the current session context.
     *
     * @param req  the request
     * @param resp the response
     * @throws ServletException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String protocol = req.getScheme();
        if (!allowHttp && !"https".equals(protocol)) {
            resp.setStatus(403);
            return;
        }
        try {
            String contentType = req.getContentType();
            UsernamePasswordToken token = null;
            if (contentType != null && contentType.contains(APPLICATION_FORM_URLENCODED)) {
                String username = req.getParameter("username");
                String password = req.getParameter("password");
                token = new UsernamePasswordToken(username, password);
            } else if (contentType != null && contentType.contains(APPLICATION_JSON)) {
                InputStream stream = req.getInputStream();
                ClassLoader loader = getClass().getClassLoader();
                token = getJsonTransformer().transform(stream, loader);
            } else if (contentType != null && contentType.contains(APPLICATION_XML)) {
                InputStream stream = req.getInputStream();
                ClassLoader loader = getClass().getClassLoader();
                token = getXmlTransformer().transform(stream, loader);
            }
            SecuritySubject subject = authService.authenticate(token);
            req.getSession().setAttribute(FABRIC3_SUBJECT, subject);
        } catch (TransformationException | IOException | AuthenticationException e) {
            monitor.error("Error authenticating", e);
        }
    }

    /**
     * Logs the current client out and removes the authenticated subject from the session context.
     *
     * @param req  the request
     * @param resp the response
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(FABRIC3_SUBJECT) != null) {
            session.removeAttribute(FABRIC3_SUBJECT);
            session.invalidate();
        }
    }

    @SuppressWarnings({"unchecked"})
    private Transformer<InputStream, UsernamePasswordToken> getJsonTransformer() throws TransformationException {
        if (jsonTransformer == null) {
            List<Class<?>> list = Collections.emptyList();
            this.jsonTransformer = (Transformer<InputStream, UsernamePasswordToken>) registry.getTransformer(JSON_TYPE, JAVA_TYPE, list, list);
            if (jsonTransformer == null) {
                throw new TransformationException("JSON databinding extension is not installed");
            }
        }
        return jsonTransformer;
    }

    @SuppressWarnings({"unchecked"})
    private Transformer<InputStream, UsernamePasswordToken> getXmlTransformer() throws TransformationException {
        if (xmlTransformer == null) {
            List<Class<?>> list = Collections.emptyList();
            this.xmlTransformer = (Transformer<InputStream, UsernamePasswordToken>) registry.getTransformer(XML_TYPE, JAVA_TYPE, list, list);
            if (xmlTransformer == null) {
                throw new TransformationException("JAXB databinding extension is not installed");
            }
        }
        return xmlTransformer;
    }

}
