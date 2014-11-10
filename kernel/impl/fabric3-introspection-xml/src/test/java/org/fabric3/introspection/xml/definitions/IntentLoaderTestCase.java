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
package org.fabric3.introspection.xml.definitions;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 * Loader for definitions.
 */
public class IntentLoaderTestCase extends TestCase {
    private String VALID_INTENT =
            "<definitions xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' xmlns:sca='http://docs.oasis-open.org/ns/opencsa/sca/200912'>" +
                    "   <intent name='serverAuthentication' constrains='sca:binding' intentType='interaction'>\n" +
                    "        <description>" +
                    "            Communication through the binding requires that the server is authenticated by the client\n" +
                    "        </description>" +
                    "        <qualifier name='transport' default='true'/>" +
                    "        <qualifier name='message'/>" +
                    "    </intent>" +
                    "</definitions";

    private String INVALID_MULTIPLE_DEFAULTS_INTENT =
            "<definitions xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' xmlns:sca='http://docs.oasis-open.org/ns/opencsa/sca/200912'>" +
                    "<intent name='serverAuthentication' constrains='sca:binding' intentType='interaction'>\n" +
                    "        <description>" +
                    "            Communication through the binding requires that the server is authenticated by the client\n" +
                    "        </description>" +
                    "        <qualifier name='transport' default='true'/>" +
                    "        <qualifier name='message' default='true'/>" +
                    "    </intent>" +
                    "</definitions";

    private String INVALID_DUPLICATE_NAMES_INTENT =
            "<definitions xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' xmlns:sca='http://docs.oasis-open.org/ns/opencsa/sca/200912'>" +
                    "<intent name='serverAuthentication' constrains='sca:binding' intentType='interaction'>\n" +
                    "        <description>" +
                    "            Communication through the binding requires that the server is authenticated by the client\n" +
                    "        </description>" +
                    "        <qualifier name='transport' default='true'/>" +
                    "        <qualifier name='transport' />" +
                    "    </intent>" +
                    "</definitions";

    private String INVALID_PROFILE_INTENT_NAME =
            "<definitions xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' xmlns:sca='http://docs.oasis-open.org/ns/opencsa/sca/200912'>" +
                    "   <intent name='serverAuthentication.foo' constrains='sca:binding' intentType='interaction'>\n" +
                    "        <description>" +
                    "            Communication through the binding requires that the server is authenticated by the client\n" +
                    "        </description>" +
                    "        <qualifier name='transport' default='true'/>" +
                    "        <qualifier name='message'/>" +
                    "    </intent>" +
                    "</definitions";

    private IntentLoader loader;
    private XMLInputFactory factory;
    private IntrospectionContext context;

    public void testParse() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(VALID_INTENT.getBytes()));
        reader.nextTag();
        reader.nextTag();
        Intent intent = loader.load(reader, context);
        assertEquals(2, intent.getQualifiers().size());
        assertEquals("serverAuthentication", intent.getName().getLocalPart());
        assertEquals("binding", intent.getConstrains().getLocalPart());
        assertTrue(context.getErrors().isEmpty());
    }

    public void testMultipleDefaults() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(INVALID_MULTIPLE_DEFAULTS_INTENT.getBytes()));
        reader.nextTag();
        reader.nextTag();
        loader.load(reader, context);
        assertTrue(context.getErrors().get(0) instanceof DuplicateDefaultIntent);
    }

    public void testDuplicateQualifiedNames() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(INVALID_DUPLICATE_NAMES_INTENT.getBytes()));
        reader.nextTag();
        reader.nextTag();
        loader.load(reader, context);
        assertTrue(context.getErrors().get(0) instanceof DuplicateQualifiedName);
    }

    public void testInvalidProfileIntentName() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(INVALID_PROFILE_INTENT_NAME.getBytes()));
        reader.nextTag();
        reader.nextTag();
        loader.load(reader, context);
        assertTrue(context.getErrors().get(0) instanceof InvalidValue);
    }

    protected void setUp() throws Exception {
        super.setUp();
        factory = XMLInputFactory.newInstance();
        context = new DefaultIntrospectionContext();
        LoaderHelper loaderHelper = new DefaultLoaderHelper();
        loader = new IntentLoader(loaderHelper);
    }


}
