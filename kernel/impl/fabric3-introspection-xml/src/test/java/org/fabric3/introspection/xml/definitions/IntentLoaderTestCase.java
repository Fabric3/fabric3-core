/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.introspection.xml.definitions;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.model.type.definitions.Intent;
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
