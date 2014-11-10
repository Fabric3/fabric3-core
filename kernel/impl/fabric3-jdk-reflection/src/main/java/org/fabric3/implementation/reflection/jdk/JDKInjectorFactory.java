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
package org.fabric3.implementation.reflection.jdk;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.fabric3.implementation.pojo.spi.reflection.InjectorFactory;
import org.fabric3.spi.container.objectfactory.Injector;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * The default runtime reflection factory extension that uses JDK reflection.
 */
public class JDKInjectorFactory implements InjectorFactory {

    public boolean isDefault() {
        return true;
    }

    public Injector<?> createInjector(Member member, ObjectFactory<?> parameterFactory) {
        if (member instanceof Field) {
            return new FieldInjector((Field) member, parameterFactory);
        } else if (member instanceof Method) {
            return new MethodInjector((Method) member, parameterFactory);
        } else {
            throw new AssertionError("Unsupported type: " + member);
        }
    }

}
