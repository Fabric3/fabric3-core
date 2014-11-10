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
package org.fabric3.spi.model.type.java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

/**
 *
 */
public class MethodInjectionSiteTestCase extends TestCase {

    public void testExternalizable() throws Exception {
        MethodInjectionSite site = new MethodInjectionSite(Foo.class.getMethod("test", String.class), 0);

        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bas);
        out.writeObject(site);

        ByteArrayInputStream bis = new ByteArrayInputStream(bas.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);

        MethodInjectionSite deserialized = (MethodInjectionSite) in.readObject();
        assertEquals(site.getSignature(), deserialized.getSignature());
    }

    public static class Foo {
        public void test(String test) {
        }
    }
}
