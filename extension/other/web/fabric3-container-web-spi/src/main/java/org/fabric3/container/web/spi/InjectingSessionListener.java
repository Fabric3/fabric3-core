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
package org.fabric3.container.web.spi;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.List;

import org.fabric3.api.host.Fabric3RuntimeException;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.objectfactory.Injector;

/**
 * Injects reference proxies into an HTTP session when it is created.
 */
public class InjectingSessionListener implements HttpSessionListener {
    private List<Injector<HttpSession>> injectors;

    public InjectingSessionListener(List<Injector<HttpSession>> injectors) {
        this.injectors = injectors;
    }

    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        for (Injector<HttpSession> injector : injectors) {
            try {
                injector.inject(session);
            } catch (ContainerException e) {
                throw new Fabric3RuntimeException(e);
            }
        }
    }

    public void sessionDestroyed(HttpSessionEvent se) {

    }
}
