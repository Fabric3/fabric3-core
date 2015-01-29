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
package org.fabric3.introspection.java.annotation;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.PolicyAware;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.oasisopen.sca.annotation.Confidentiality;
import org.oasisopen.sca.annotation.Requires;
import static org.oasisopen.sca.annotation.Confidentiality.CONFIDENTIALITY_MESSAGE;

/**
 *
 */
public class PolicyAnnotationProcessorImplTestCase extends TestCase {
    private PolicyAnnotationProcessorImpl processor = new PolicyAnnotationProcessorImpl();

    public void testUnQualified() throws Exception {
        Confidentiality annotation = TestClass.class.getAnnotation(Confidentiality.class);
        IntrospectionContext ctx = new DefaultIntrospectionContext();

        PolicyAware modelObject = EasyMock.createMock(PolicyAware.class);
        EasyMock.replay(modelObject);

        processor.process(annotation, modelObject, ctx);
        EasyMock.verify(modelObject);
    }

    public void testQualified() throws Exception {
        Confidentiality annotation = TestQualifiedClass.class.getAnnotation(Confidentiality.class);
        IntrospectionContext ctx = new DefaultIntrospectionContext();

        PolicyAware modelObject = EasyMock.createMock(PolicyAware.class);
        EasyMock.replay(modelObject);

        processor.process(annotation, modelObject, ctx);
        EasyMock.verify(modelObject);
    }

    @Confidentiality
    @Requires("{namespace}foo")
    private class TestClass {

    }

    @Confidentiality(CONFIDENTIALITY_MESSAGE)
    private class TestQualifiedClass {

    }

}