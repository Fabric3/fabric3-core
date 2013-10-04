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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.container.web.jetty;

import java.util.EventListener;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import org.fabric3.spi.container.objectfactory.Injector;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;

/**
 * Injects a servlet or filter with reference proxies, properties, and the component context.
 */
public class InjectingDecorator implements ServletContextHandler.Decorator {
    private Map<String, List<Injector<?>>> injectorMappings;

    public InjectingDecorator(Map<String, List<Injector<?>>> injectorMappings) {
        this.injectorMappings = injectorMappings;
    }


    public <T extends Filter> T decorateFilterInstance(T filter) throws ServletException {
        inject(filter);
        return filter;
    }

    public <T extends Servlet> T decorateServletInstance(T servlet) throws ServletException {
        inject(servlet);
        return servlet;
    }

    public <T extends EventListener> T decorateListenerInstance(T listener) throws ServletException {
        inject(listener);
        return listener;
    }

    public void decorateFilterHolder(FilterHolder filter) throws ServletException {
        inject(filter);

    }

    public void decorateServletHolder(ServletHolder servlet) throws ServletException {
        inject(servlet);
    }

    public void destroyServletInstance(Servlet s) {

    }

    public void destroyFilterInstance(Filter f) {

    }

    public void destroyListenerInstance(EventListener f) {

    }

    @SuppressWarnings({"unchecked"})
    private void inject(Object instance) throws ServletException {
        List<Injector<?>> injectors = injectorMappings.get(instance.getClass().getName());
        if (injectors != null) {
            for (Injector injector : injectors) {
                try {
                    injector.inject(instance);
                } catch (ObjectCreationException e) {
                    throw new ServletException(e);
                }
            }
        }
    }


}
