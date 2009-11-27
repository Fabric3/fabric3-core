/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.implementation.web.runtime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.oasisopen.sca.ServiceRuntimeException;
import org.osoa.sca.CallableReference;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.RequestContext;
import org.osoa.sca.ServiceReference;

import org.fabric3.container.web.spi.WebRequestTunnel;
import org.fabric3.host.Fabric3RuntimeException;
import org.fabric3.spi.ObjectCreationException;

/**
 * Implementation of ComponentContext for Web components.
 *
 * @version $Rev$ $Date$
 */
public class WebComponentContext implements ComponentContext {
    private final WebComponent<?> component;

    public WebComponentContext(WebComponent<?> component) {
        this.component = component;
    }

    public String getURI() {
        try {
            return component.getUri().toString();
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B, R extends CallableReference<B>> R cast(B target) throws IllegalArgumentException {
        try {
            return (R) component.cast(target);
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> B getService(Class<B> interfaze, String referenceName) {
        try {
            return interfaze.cast(getSession().getAttribute(referenceName));
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    // method is a proposed spec change
    public <B> B createService(Class<B> interfaze, String referenceName) {
        try {
            return component.getService(interfaze, referenceName);
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        } catch (ObjectCreationException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public <B> ServiceReference<B> getServiceReference(Class<B> interfaze, String referenceName) {
        try {
            return ServiceReference.class.cast(getSession().getAttribute(referenceName));
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> B getProperty(Class<B> type, String propertyName) {
        try {
            return component.getProperty(type, propertyName);
        } catch (ObjectCreationException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        } catch (Fabric3RuntimeException e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface) {
        return null;
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface, String serviceName) {
        return null;
    }

    public RequestContext getRequestContext() {
        return null;
    }

    private HttpSession getSession() {
        HttpServletRequest request = WebRequestTunnel.getRequest();
        if (request == null) {
            throw new ServiceRuntimeException("HTTP request not bound. Check filter configuration.");
        }
        return request.getSession(true);  // force creation of session 
    }


}
