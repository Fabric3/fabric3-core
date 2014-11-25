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
package org.fabric3.binding.rs.runtime.bytecode;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.IOException;

import junit.framework.TestCase;
import org.fabric3.binding.rs.runtime.provider.ProxyExceptionMapper;
import org.objectweb.asm.Type;

/**
 *
 */
public class ProviderGeneratorImplTestCase extends TestCase {
    private ProviderGeneratorImpl generator;

    public void testGenerate() throws Exception {
        Class clazz = generator.generate(TestClass.class, TestProvider.class, null);
        TestClass instance = (TestClass) clazz.newInstance();
        assertEquals("test", instance.invoke());
    }

    public void testGenerateGenerics() throws Exception {
        String exceptionName = Type.getInternalName(IOException.class);
        String signature = "<E:L" + exceptionName + ";>L" + Type.getInternalName(ProxyExceptionMapper.class) + "<L" + exceptionName + ";>;L"
                           + Type.getInternalName(ExceptionMapper.class) + "<TE;>;";

        Class clazz = generator.generate(TestMapper.class, TestMapper.class, signature);
        TestMapper instance = (TestMapper) clazz.newInstance();
        assertEquals(ProxyExceptionMapper.class, instance.getClass().getGenericSuperclass());
    }

    public void testAnnotationCopied() throws Exception {
        Class<? extends TestClass> clazz = generator.generate(TestClass.class, TestPriorityProvider.class, null);
        assertTrue(clazz.isAnnotationPresent(Priority.class));
        assertEquals(Priorities.AUTHENTICATION, clazz.getAnnotation(Priority.class).value());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        generator = new ProviderGeneratorImpl();
    }
}
