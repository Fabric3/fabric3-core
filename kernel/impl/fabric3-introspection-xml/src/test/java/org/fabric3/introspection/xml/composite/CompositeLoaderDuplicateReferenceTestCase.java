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
package org.fabric3.introspection.xml.composite;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.api.host.contribution.ArtifactValidationFailure;
import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 *
 */
public class CompositeLoaderDuplicateReferenceTestCase extends TestCase {
    public static final String PROP_NAME = "notThere";
    private String XML = "<composite xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' "
            + "targetNamespace='http://example.com' name='composite'>"
            + "<reference name='ref' promote='component'/>"
            + "<reference name='ref' promote='component'/>"
            + "</composite>";

    private CompositeLoader loader;
    private XMLStreamReader reader;
    private IntrospectionContext context;

    /**
     * Verifies an exception is thrown if an attempt is made to configure a property twice.
     *
     * @throws Exception on test failure
     */
    public void testDuplicateProperty() throws Exception {
        loader.load(reader, context);
        ValidationFailure failure = context.getErrors().get(0);
        assertTrue(failure instanceof ArtifactValidationFailure);
        assertTrue(((ArtifactValidationFailure) failure).getFailures().get(0) instanceof DuplicatePromotedReference);
    }

    protected void setUp() throws Exception {
        super.setUp();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        context = new DefaultIntrospectionContext();
        LoaderHelper loaderHelper = new DefaultLoaderHelper();
        loader = new CompositeLoader(null, null, new CompositeReferenceLoader(null, loaderHelper), null, null, loaderHelper);
        reader = factory.createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();

    }

}