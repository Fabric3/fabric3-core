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
package org.fabric3.security.spring;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationManager;

import org.fabric3.spring.spi.ApplicationContextListener;

/**
 * Registers Spring Security authentication and access services so they can be referenced from component application contexts.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class SecurityApplicationContextListener implements ApplicationContextListener {
    private static final String AUTHENTICATION_ALIAS = "authenticationManager";
    private static final String ACCESS_ALIAS = "accessManager";

    private AuthenticationManager authManager;
    private AccessDecisionManager accessManager;

    public SecurityApplicationContextListener(@Reference AuthenticationManager authManager, @Reference AccessDecisionManager accessManager) {
        this.authManager = authManager;
        this.accessManager = accessManager;
    }

    public void onCreate(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory factory = context.getBeanFactory();
        factory.registerSingleton(AUTHENTICATION_ALIAS, authManager);
        factory.registerSingleton(ACCESS_ALIAS, accessManager);
    }

    public void onDispose(ConfigurableApplicationContext context) {
        // no-op
    }
}