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
package org.fabric3.fabric.runtime.bootstrap;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.fabric3.fabric.xml.DocumentLoader;
import org.fabric3.fabric.xml.DocumentLoaderImpl;
import org.fabric3.host.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.implementation.system.model.SystemImplementation;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.spi.introspection.validation.InvalidCompositeException;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;

/**
 * Bootstrapper that initializes a runtime by reading a system SCDL file.
 *
 * @version $Rev$ $Date$
 */
public class ScdlBootstrapperImpl extends AbstractBootstrapper implements ScdlBootstrapper {

    private DocumentLoader documentLoader;

    private URL scdlLocation;
    private URL systemConfig;
    private InputSource systemConfigSource;

    public ScdlBootstrapperImpl() {
        this.documentLoader = new DocumentLoaderImpl();
    }

    public void setScdlLocation(URL scdlLocation) {
        this.scdlLocation = scdlLocation;
    }

    public void setSystemConfig(URL systemConfig) {
        this.systemConfig = systemConfig;
    }

    public void setSystemConfig(InputSource source) {
        this.systemConfigSource = source;
    }

    protected Composite loadSystemComposite(Contribution contribution,
                                            ClassLoader bootClassLoader,
                                            ImplementationProcessor<SystemImplementation> processor) throws InitializationException {
        try {
            // load the system composite
            Loader loader = BootstrapLoaderFactory.createLoader(processor, getXmlFactory());
            URI contributionUri = contribution.getUri();
            IntrospectionContext introspectionContext = new DefaultIntrospectionContext(contributionUri, bootClassLoader, scdlLocation);
            Composite composite = loader.load(scdlLocation, Composite.class, introspectionContext);
            if (introspectionContext.hasErrors()) {
                QName name = composite.getName();
                List<ValidationFailure> errors = introspectionContext.getErrors();
                List<ValidationFailure> warnings = introspectionContext.getWarnings();
                throw new InvalidCompositeException(name, errors, warnings);
            }

            addContributionUri(contributionUri, composite);
            addResource(contribution, composite);
            return composite;
        } catch (ContributionException e) {
            throw new InitializationException(e);
        } catch (LoaderException e) {
            throw new InitializationException(e);
        }
    }

    protected Document loadSystemConfig() throws InitializationException {
        if (systemConfigSource == null && systemConfig == null) {
            // no system configuration specified, create a default one
            return createDefaultConfigProperty();
        }
        Document document;
        try {
            if (systemConfigSource != null) {
                // load from an external URL
                document = documentLoader.load(systemConfigSource, true);
            } else {
                // load from an external URL
                document = documentLoader.load(systemConfig, true);
            }
        } catch (IOException e) {
            throw new InitializationException(e);
        } catch (SAXException e) {
            throw new InitializationException(e);
        }
        // all properties have a root <values> element, append the existing root to it. The existing root will be taken as a property <value>.
        Element oldRoot = document.getDocumentElement();
        Element newRoot = document.createElement("values");
        document.removeChild(oldRoot);
        document.appendChild(newRoot);
        newRoot.appendChild(oldRoot);
        return document;
    }


    /**
     * Creates a default configuration domain property.
     *
     * @return a document representing the configuration domain property
     */
    protected Document createDefaultConfigProperty() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document document = factory.newDocumentBuilder().newDocument();
            Element root = document.createElement("values");
            document.appendChild(root);
            Element config = document.createElement("config");
            root.appendChild(config);
            return document;
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Adds the contibution URI to a component and its children if it is a composite.
     *
     * @param contributionUri the contribution URI
     * @param composite       the composite
     */
    private void addContributionUri(URI contributionUri, Composite composite) {
        composite.setContributionUri(contributionUri);
        for (ComponentDefinition<?> definition : composite.getComponents().values()) {
            Implementation<?> implementation = definition.getImplementation();
            if (CompositeImplementation.class.isInstance(implementation)) {
                CompositeImplementation compositeImplementation = CompositeImplementation.class.cast(implementation);
                Composite componentType = compositeImplementation.getComponentType();
                addContributionUri(contributionUri, componentType);
            } else {
                definition.setContributionUri(contributionUri);
            }
        }
    }

    /**
     * Adds the composite as a resource to the contribution.
     *
     * @param contribution the contribution
     * @param composite    the composite
     */
    private void addResource(Contribution contribution, Composite composite) {
        Resource resource = new Resource(scdlLocation, Constants.COMPOSITE_CONTENT_TYPE);
        QName compositeName = composite.getName();
        QNameSymbol symbol = new QNameSymbol(compositeName);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol);
        resource.addResourceElement(element);
        contribution.addResource(resource);
    }


}
