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
package org.fabric3.implementation.pojo.generator;

import java.util.Map;

import junit.framework.TestCase;

import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.api.model.type.java.Signature;

/**
 *
 */
public class GenerationHelperImplTestCase extends TestCase {

    private GenerationHelper helper;
    private ImplementationManagerDefinition managerDefinition;
    private InjectingComponentType componentType;
    private Injectable intProp;
    private Injectable stringProp;

    public void testSimpleConstructor() {
        Signature constructor = new Signature("Test", "int", "String");
        ConstructorInjectionSite intSite = new ConstructorInjectionSite(constructor, 0);
        ConstructorInjectionSite stringSite = new ConstructorInjectionSite(constructor, 1);
        componentType.setConstructor(constructor);
        componentType.addInjectionSite(intSite, intProp);
        componentType.addInjectionSite(stringSite, stringProp);
        helper.processInjectionSites(componentType, managerDefinition);
        Map<InjectionSite, Injectable> mapping = managerDefinition.getConstruction();
        assertEquals(intProp, mapping.get(intSite));
        assertEquals(stringProp, mapping.get(stringSite));
        assertTrue(managerDefinition.getPostConstruction().isEmpty());
        assertTrue(managerDefinition.getReinjectables().isEmpty());
    }

    protected void setUp() throws Exception {
        super.setUp();

        helper = new GenerationHelperImpl();
        componentType = new InjectingComponentType(null);
        managerDefinition = new ImplementationManagerDefinition();

        intProp = new Injectable(InjectableType.PROPERTY, "intProp");
        stringProp = new Injectable(InjectableType.PROPERTY, "stringProp");
    }

}
