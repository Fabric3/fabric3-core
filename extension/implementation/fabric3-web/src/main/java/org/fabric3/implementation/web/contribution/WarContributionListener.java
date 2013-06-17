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
package org.fabric3.implementation.web.contribution;

import java.net.URI;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.host.Namespaces;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.stream.Source;
import org.fabric3.implementation.web.model.WebComponentType;
import org.fabric3.implementation.web.model.WebImplementation;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 * Listens for WAR contributions and adds a synthesized web component to allow contributions not to specify a web component in a contribution. A
 * synthesized component is added if the contribution is a WAR and no composites are contained in it.
 */
@EagerInit
public class WarContributionListener implements ContributionServiceListener {

    public void onInstall(Contribution contribution) {
        String sourceUrl = contribution.getLocation().toString();
        if (!sourceUrl.endsWith(".war")) {
            // not a WAR file
            return;
        }
        ContributionManifest manifest = contribution.getManifest();
        if (!manifest.getDeployables().isEmpty()) {
            return;
        }

        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (element.getValue() instanceof Composite) {
                    // a composite was defined, return;
                    return;
                }
            }
        }
        // no composites were defined, synthesize one
        Composite composite = createComposite(contribution);

        Source source = contribution.getSource();
        Resource resource = createResource(contribution, composite, source);
        contribution.addResource(resource);

        QName name = composite.getName();
        Deployable deployable = new Deployable(name);
        manifest.addDeployable(deployable);
    }

    public void onStore(Contribution contribution) {
        // no-op
    }

    public void onProcessManifest(Contribution contribution) {
        // no-op
    }

    public void onUpdate(Contribution contribution) {
        // no-op
    }

    public void onUninstall(Contribution contribution) {
        // no-op
    }

    public void onRemove(Contribution contribution) {
        // no-op
    }

    private Composite createComposite(Contribution contribution) {
        URI contributionUri = contribution.getUri();
        String localPart = createLocalPart(contributionUri);
        QName compositeName = new QName(Namespaces.SYNTHESIZED, localPart);
        Composite composite = new Composite(compositeName);
        composite.setContributionUri(contributionUri);

        WebComponentType componentType = new WebComponentType();
        WebImplementation impl = new WebImplementation();
        impl.setComponentType(componentType);

        ComponentDefinition<WebImplementation> component = new ComponentDefinition<WebImplementation>(localPart, impl);
        component.setContributionUri(contributionUri);
        composite.add(component);
        return composite;
    }

    private String createLocalPart(URI contributionUri) {
        String localPart = contributionUri.toString();
        int index = localPart.lastIndexOf(".");
        if (index > 0) {
            // strip suffixes
            localPart = localPart.substring(0, index);
        }
        return localPart;
    }

    private Resource createResource(Contribution contribution, Composite composite, Source source) {
        QNameSymbol symbol = new QNameSymbol(composite.getName());
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol, composite);
        Resource resource = new Resource(contribution, source, Constants.COMPOSITE_CONTENT_TYPE);
        resource.addResourceElement(element);
        resource.setState(ResourceState.PROCESSED);
        return resource;
    }

}
