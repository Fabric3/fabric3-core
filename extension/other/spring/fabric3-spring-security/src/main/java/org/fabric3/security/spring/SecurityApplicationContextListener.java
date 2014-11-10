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
 */
package org.fabric3.security.spring;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationManager;

import org.fabric3.spring.spi.ApplicationContextListener;

/**
 * Registers Spring Security authentication and access services so they can be referenced from component application contexts.
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