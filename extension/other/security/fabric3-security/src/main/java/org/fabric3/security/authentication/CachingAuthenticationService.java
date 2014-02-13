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
package org.fabric3.security.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.SecuritySubject;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.json.JsonType;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.UsernamePasswordToken;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

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
 * <p/>
 * <pre>
 * Logout is performed by performing an HTTP DELETE.
 */
@EagerInit
public class CachingAuthenticationService extends HttpServlet {
    private static final long serialVersionUID = -3247111411539759436L;

    private static final String FABRIC3_SUBJECT = "fabric3.subject";

    private final static String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private final static String APPLICATION_JSON = "application/json";
    private final static String APPLICATION_XML = "application/xml";

    private final static DataType<?> JSON_TYPE = new JsonType<Class<String>>(InputStream.class, String.class);
    private static final DataType<?> XML_TYPE = new XSDType(String.class, new QName(XSDType.XSD_NS, "string"));
    private final static JavaClass<UsernamePasswordToken> JAVA_TYPE = new JavaClass<UsernamePasswordToken>(UsernamePasswordToken.class);

    private AuthenticationService authService;
    private ServletHost host;
    private AuthMonitor monitor;
    private boolean enabled = true;
    private boolean allowHttp;
    private String mapping = "/fabric/security/token";
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
    public void start() throws TransformationException {
        if (!enabled) {
            return;
        }
        host.registerMapping(mapping, this);
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
