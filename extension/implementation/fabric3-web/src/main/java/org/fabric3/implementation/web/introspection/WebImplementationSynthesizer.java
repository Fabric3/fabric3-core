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
package org.fabric3.implementation.web.introspection;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.fabric3.api.host.HostNamespaces;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.implementation.web.model.WebComponentType;
import org.fabric3.implementation.web.model.WebImplementation;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Synthesizes a web implementation configuration if one is not explicitly defined in a composite for the current contribution.
 */
@EagerInit
public class WebImplementationSynthesizer implements ContributionServiceListener {

    public void onInstall(Contribution contribution) {
        if (!isWar(contribution)) {
            return;
        }

        if (hasImplementation(contribution)) {
            return;   // web component was explicitly configured in the contribution
        }

        // synthesize a web implementation
        URI uri = createWebUri(contribution);
        WebImplementation implementation = new WebImplementation(uri);

        // retrieve the component type introspected during contribution indexing or create one if no web artifacts resulted in it being generated
        WebComponentType componentType = getComponentType(contribution);
        implementation.setComponentType(componentType);

        // synthesize a deployable composite
        IndexHelper.indexImplementation(implementation, contribution);
        Composite composite = createComposite(implementation, contribution);
        contribution.getManifest().addDeployable(new Deployable(composite.getName()));
        contribution.addResource(createResource(contribution, composite));
    }

    public void onStore(Contribution contribution) {

    }

    public void onProcessManifest(Contribution contribution) {

    }

    public void onUpdate(Contribution contribution) {

    }

    public void onUninstall(Contribution contribution) {

    }

    public void onRemove(Contribution contribution) {

    }

    private boolean hasImplementation(Contribution contribution) {
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (element.getSymbol() instanceof WebImplementationSymbol) {
                    return true;
                }
            }
        }
        return false;
    }

    private WebComponentType getComponentType(Contribution contribution) {
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (element.getSymbol() instanceof WebComponentTypeSymbol) {
                    return (WebComponentType) element.getValue();
                }
            }
        }
        return new WebComponentType();
    }

    private Composite createComposite(WebImplementation implementation, Contribution contribution) {
        URI contributionUri = contribution.getUri();
        String localPart = createLocalPart(contributionUri);
        QName compositeName = new QName(HostNamespaces.SYNTHESIZED, localPart);
        Composite composite = new Composite(compositeName);
        composite.setContributionUri(contributionUri);
        Component<WebImplementation> component = new Component<>(localPart, implementation);
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

    private URI createWebUri(Contribution contribution) {
        String context = contribution.getManifest().getContext();
        if (context != null) {
            return URI.create(context);
        }
        return contribution.getUri();
    }

    private Resource createResource(Contribution contribution, Composite composite) {
        QNameSymbol symbol = new QNameSymbol(composite.getName());
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(symbol, composite);
        Resource resource = new Resource(contribution, null, Constants.COMPOSITE_CONTENT_TYPE);
        resource.addResourceElement(element);
        resource.setState(ResourceState.PROCESSED);
        return resource;
    }

    private boolean isWar(Contribution contribution) {
        URL location = contribution.getLocation();
        String sourceUrl = location.toString();
        if (sourceUrl.endsWith(".jar")) {
            // short-circuit common case
            return false;
        }
        if (!sourceUrl.endsWith(".war")) {
            // check if it is an exploded WAR
            boolean hasWebInf = false;
            if ("file".equals(location.getProtocol())) {
                try {
                    Path path = Paths.get(location.toURI());
                    File root = path.toFile();
                    if (!root.isDirectory()) {
                        return false;
                    }
                    File[] files = root.listFiles();
                    if (files == null) {
                        return false;
                    }
                    for (File file : files) {
                        if (file.isDirectory() && "WEB-INF".equals(file.getName())) {
                            hasWebInf = true;
                            break;
                        }
                    }
                } catch (URISyntaxException e) {
                    // should not happen
                    throw new AssertionError(e);
                }
            }
            if (!hasWebInf) {
                // not a WAR file
                return false;
            }
        }
        return true;
    }

}
