/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml.composite;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.StoreException;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.ElementLoadFailure;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Loader that handles an &lt;implementation.composite&gt; element.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ImplementationCompositeLoader extends AbstractExtensibleTypeLoader<CompositeImplementation> {
    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();
    private static final QName IMPL = new QName(Constants.SCA_NS, "implementation.composite");

    static {
        ATTRIBUTES.put("name", "name");
        ATTRIBUTES.put("scdlLocation", "scdlLocation");
        ATTRIBUTES.put("scdlResource", "scdlResource");
        ATTRIBUTES.put("requires", "requires");
    }

    private final MetaDataStore store;

    public ImplementationCompositeLoader(@Reference LoaderRegistry registry, @Reference MetaDataStore store) {
        super(registry);
        this.store = store;
    }

    public QName getXMLType() {
        return IMPL;
    }

    public CompositeImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        assert CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(reader.getName());
        validateAttributes(reader, introspectionContext);
        // read name now b/c the reader skips ahead
        String nameAttr = reader.getAttributeValue(null, "name");
        String scdlLocation = reader.getAttributeValue(null, "scdlLocation");
        String scdlResource = reader.getAttributeValue(null, "scdlResource");
        LoaderUtil.skipToEndElement(reader);

        ClassLoader cl = introspectionContext.getClassLoader();
        CompositeImplementation impl = new CompositeImplementation();
        URI contributionUri = introspectionContext.getContributionUri();
        URL url;
        if (scdlLocation != null) {
            try {
                url = new URL(introspectionContext.getSourceBase(), scdlLocation);
            } catch (MalformedURLException e) {
                MissingComposite failure = new MissingComposite("Composite file not found: " + scdlLocation, reader);
                introspectionContext.addError(failure);
                return impl;
            }
            IntrospectionContext childContext = new DefaultIntrospectionContext(contributionUri, cl, url);
            Composite composite;
            try {
                composite = registry.load(url, Composite.class, childContext);
                if (childContext.hasErrors()) {
                    introspectionContext.addErrors(childContext.getErrors());
                }
                if (childContext.hasWarnings()) {
                    introspectionContext.addWarnings(childContext.getWarnings());
                }
                if (composite == null) {
                    // error loading composite, return
                    return null;
                }

            } catch (LoaderException e) {
                ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, reader);
                introspectionContext.addError(failure);
                return null;
            }
            impl.setName(composite.getName());
            impl.setComponentType(composite);

            return impl;
        } else if (scdlResource != null) {
            url = cl.getResource(scdlResource);
            if (url == null) {
                MissingComposite failure = new MissingComposite("Composite file not found: " + scdlResource, reader);
                introspectionContext.addError(failure);
                return impl;
            }
            IntrospectionContext childContext = new DefaultIntrospectionContext(contributionUri, cl, url);
            Composite composite;
            try {
                composite = registry.load(url, Composite.class, childContext);
                if (childContext.hasErrors()) {
                    introspectionContext.addErrors(childContext.getErrors());
                }
                if (childContext.hasWarnings()) {
                    introspectionContext.addWarnings(childContext.getWarnings());
                }
                if (composite == null) {
                    // error loading composite, return
                    return null;
                }
            } catch (LoaderException e) {
                ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, reader);
                introspectionContext.addError(failure);
                return null;
            }
            impl.setName(composite.getName());
            impl.setComponentType(composite);
            return impl;
        } else {
            if (nameAttr == null || nameAttr.length() == 0) {
                MissingAttribute failure = new MissingAttribute("Missing name attribute", reader);
                introspectionContext.addError(failure);
                return null;
            }
            QName name = LoaderUtil.getQName(nameAttr, introspectionContext.getTargetNamespace(), reader.getNamespaceContext());

            try {
                QNameSymbol symbol = new QNameSymbol(name);
                ResourceElement<QNameSymbol, Composite> element = store.resolve(contributionUri, Composite.class, symbol, introspectionContext);
                if (element == null) {
                    String id = name.toString();
                    MissingComposite failure = new MissingComposite("Composite with qualified name not found: " + id, reader);
                    introspectionContext.addError(failure);
                    return impl;
                }
                impl.setComponentType(element.getValue());
                return impl;
            } catch (StoreException e) {
                ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, reader);
                introspectionContext.addError(failure);
                return null;

            }
        }

    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.containsKey(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
