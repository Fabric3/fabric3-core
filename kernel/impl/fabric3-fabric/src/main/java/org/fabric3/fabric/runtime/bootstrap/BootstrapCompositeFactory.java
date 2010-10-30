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
 */
package org.fabric3.fabric.runtime.bootstrap;

import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.fabric.xml.XMLFactoryImpl;
import org.fabric3.host.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
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
import org.fabric3.spi.xml.XMLFactory;

/**
 * Creates the initial system composite that is deployed to the runtime domain.
 *
 * @version $Rev$ $Date$
 */
public class BootstrapCompositeFactory {
    private static final XMLFactory XML_FACTORY = new XMLFactoryImpl();

    private BootstrapCompositeFactory() {
    }

    public static Composite createSystemComposite(URL compositeUrl,
                                                  Contribution contribution,
                                                  ClassLoader bootClassLoader,
                                                  ImplementationProcessor<SystemImplementation> processor) throws InitializationException {
        try {
            // load and introspect the system composite XML
            Loader loader = BootstrapLoaderFactory.createLoader(processor, XML_FACTORY);
            URI contributionUri = contribution.getUri();
            IntrospectionContext introspectionContext = new DefaultIntrospectionContext(contributionUri, bootClassLoader, compositeUrl);
            Source source = new UrlSource(compositeUrl);
            Composite composite = loader.load(source, Composite.class, introspectionContext);
            if (introspectionContext.hasErrors()) {
                QName name = composite.getName();
                List<ValidationFailure> errors = introspectionContext.getErrors();
                List<ValidationFailure> warnings = introspectionContext.getWarnings();
                throw new InvalidCompositeException(name, errors, warnings);
            }

            addContributionUri(contributionUri, composite);
            addResource(contribution, composite, compositeUrl);
            return composite;
        } catch (ContributionException e) {
            throw new InitializationException(e);
        } catch (LoaderException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Adds the contribution URI to the system composite and its children.
     *
     * @param contributionUri the contribution URI
     * @param composite       the composite
     */
    private static void addContributionUri(URI contributionUri, Composite composite) {
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
     * @param scdlLocation the location of the composite file
     */
    private static void addResource(Contribution contribution, Composite composite, URL scdlLocation) {
        Source source = new UrlSource(scdlLocation);
        Resource resource = new Resource(contribution, source, Constants.COMPOSITE_CONTENT_TYPE);
        QName compositeName = composite.getName();
        QNameSymbol symbol = new QNameSymbol(compositeName);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol, composite);
        resource.addResourceElement(element);
        resource.setProcessed(true);
        contribution.addResource(resource);
    }

}