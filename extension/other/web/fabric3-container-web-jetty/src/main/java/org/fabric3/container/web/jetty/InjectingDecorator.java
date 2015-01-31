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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.container.web.jetty;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.objectfactory.Injector;

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
                } catch (ContainerException e) {
                    throw new ServletException(e);
                }
            }
        }
    }


}
