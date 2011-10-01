/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.implementation.spring.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.implementation.spring.model.SpringComponentType;
import org.fabric3.implementation.spring.model.SpringImplementation;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Loads a Spring component implementation in a composite.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class SpringImplementationLoader implements TypeLoader<SpringImplementation> {
    private SpringImplementationProcessor processor;
    private LoaderHelper loaderHelper;


    public SpringImplementationLoader(@Reference SpringImplementationProcessor processor, @Reference LoaderHelper loaderHelper) {
        this.processor = processor;
        this.loaderHelper = loaderHelper;
    }

    public SpringImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        ClassLoader classLoader = context.getClassLoader();
        updateClassLoader(classLoader);
        SpringImplementation implementation = new SpringImplementation();
        String locationAttr = reader.getAttributeValue(null, "location");
        if (locationAttr == null) {
            MissingAttribute failure = new MissingAttribute("The location attribute was not specified", reader);
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return implementation;
        }
        implementation.setLocation(locationAttr);
        loaderHelper.loadPolicySetsAndIntents(implementation, reader, context);

        LoaderUtil.skipToEndElement(reader);

        Source source = new UrlSource(classLoader.getResource(locationAttr));
        SpringComponentType type = processor.introspect(source, context);
        implementation.setComponentType(type);
        return implementation;

    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"location".equals(name) && !"requires".equals(name) && !"policySets".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

    /**
     * Make Spring classes available to the contribution classloader. This is required since user classes may extend Spring classes.
     *
     * @param classLoader the application classloader.
     */
    private void updateClassLoader(ClassLoader classLoader) {
        if (!(classLoader instanceof MultiParentClassLoader)) {
            return;
        }
        MultiParentClassLoader loader = (MultiParentClassLoader) classLoader;
        ClassLoader springClassLoader = getClass().getClassLoader();
        for (ClassLoader parent : loader.getParents()) {
            if (parent == springClassLoader) {
                return;
            }
        }
        loader.addParent(springClassLoader);
    }

}