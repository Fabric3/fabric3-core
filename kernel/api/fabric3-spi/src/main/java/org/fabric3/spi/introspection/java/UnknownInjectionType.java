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
package org.fabric3.spi.introspection.java;

import java.lang.reflect.Member;

import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 * Denotes an unknown InjectableAttributeType.
 */
public class UnknownInjectionType extends JavaValidationFailure {
    private InjectionSite site;
    private InjectableType type;
    private String clazz;

    public UnknownInjectionType(InjectionSite site, InjectableType type, String clazz, Member member, ComponentType componentType) {
        super(member, componentType);
        this.site = site;
        this.type = type;
        this.clazz = clazz;
    }

    public String getImplementationClass() {
        return clazz;
    }

    public String getMessage() {
        if (site instanceof FieldInjectionSite) {
            FieldInjectionSite field = (FieldInjectionSite) site;
            return "Unknown injection type " + type + " on field " + field.getName() + " in class " + clazz;
        } else if (site instanceof MethodInjectionSite) {
            MethodInjectionSite method = (MethodInjectionSite) site;
            return "Unknown injection type " + type + " on method " + method.getSignature() + " in class " + clazz;
        } else if (site instanceof ConstructorInjectionSite) {
            ConstructorInjectionSite ctor = (ConstructorInjectionSite) site;
            return "Unknown injection type " + type + " on constructor " + ctor.getSignature() + " in class " + clazz;
        } else {
            return "Unknown injection type " + type + " found in class " + clazz;
        }
    }
}
