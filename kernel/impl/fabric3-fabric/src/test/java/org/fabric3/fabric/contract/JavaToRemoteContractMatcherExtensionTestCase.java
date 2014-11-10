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
package org.fabric3.fabric.contract;

import java.util.Collections;

import junit.framework.TestCase;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.type.remote.RemoteServiceContract;

/**
 *
 */
public class JavaToRemoteContractMatcherExtensionTestCase extends TestCase {
    private JavaToRemoteContractMatcherExtension extension = new JavaToRemoteContractMatcherExtension();

    public void testIsAssignableFromSameClass() throws Exception {
        JavaServiceContract javaContract = new JavaServiceContract(Foo.class);
        RemoteServiceContract remoteContract = new RemoteServiceContract(Foo.class.getName(), Collections.singletonList(Object.class.getName()));
        assertTrue(extension.isAssignableFrom(javaContract, remoteContract, false).isAssignable());
    }

    public void testIsNotAssignable() throws Exception {
        JavaServiceContract javaContract = new JavaServiceContract(Foo.class);
        RemoteServiceContract remoteContract = new RemoteServiceContract(Baz.class.getName(), Collections.singletonList(Object.class.getName()));
        assertFalse(extension.isAssignableFrom(javaContract, remoteContract, false).isAssignable());
    }

    public void testIsAssignableFromSuperClass() throws Exception {
        JavaServiceContract javaContract = new JavaServiceContract(Foo.class);
        RemoteServiceContract remoteContract = new RemoteServiceContract(Bar.class.getName(), Collections.singletonList(Foo.class.getName()));
        assertTrue(extension.isAssignableFrom(javaContract, remoteContract, false).isAssignable());
    }

    public void testIsNotAssignableFromSubClass() throws Exception {
        JavaServiceContract javaContract = new JavaServiceContract(Bar.class);
        RemoteServiceContract remoteContract = new RemoteServiceContract(Foo.class.getName(), Collections.singletonList(Foo.class.getName()));
        assertFalse(extension.isAssignableFrom(javaContract, remoteContract, false).isAssignable());
    }

    private interface Foo {

    }

    private interface Bar extends Foo {

    }

    private interface Baz {

    }
}
