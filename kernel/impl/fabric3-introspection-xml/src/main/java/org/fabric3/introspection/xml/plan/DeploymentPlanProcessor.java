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
package org.fabric3.introspection.xml.plan;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoader;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoaderRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.model.plan.DeploymentPlan;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.fabric3.introspection.xml.plan.DeploymentPlanConstants.PLAN;
import static org.fabric3.introspection.xml.plan.DeploymentPlanConstants.PLAN_NAMESPACE;

/**
 * Processes a deployment plan.
 */
@EagerInit
public class DeploymentPlanProcessor implements XmlResourceElementLoader {
    private static final QName DEPLOYABLE_MAPPING = new QName(org.fabric3.api.Namespaces.F3, "mapping");

    private XmlResourceElementLoaderRegistry registry;

    public DeploymentPlanProcessor(@Reference XmlResourceElementLoaderRegistry registry) {
        this.registry = registry;
    }

    public QName getType() {
        return PLAN;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public void load(XMLStreamReader reader, Resource resource, IntrospectionContext context) throws InstallException {
        Location startLocation = reader.getLocation();
        try {
            QName qname = reader.getName();
            assert PLAN.equals(qname);
            String planName = reader.getAttributeValue(null, "name");
            if (planName == null) {
                // this won't happen as it is checked in the indexer
                context.addError(new MissingAttribute("Deployment plan name not specified", startLocation));
                return;
            }
            DeploymentPlan plan = new DeploymentPlan(planName);
            while (true) {
                switch (reader.next()) {
                case START_ELEMENT:
                    qname = reader.getName();
                    if (DEPLOYABLE_MAPPING.equals(qname)) {
                        if (!processDeployableMapping(plan, reader, context)) {
                            return;
                        }
                    }
                    break;
                case END_ELEMENT:
                    QName name = reader.getName();
                    if (PLAN.equals(name)) {
                        updatePlan(resource, plan);
                        return;
                    }
                    // update indexed elements with the loaded definitions
                }
            }
        } catch (XMLStreamException e) {
            throw new InstallException("Error processing contribution: " + context.getContributionUri(), e);
        }


    }

    /**
     * Parses a deployable mapping
     *
     * @param plan    the deployment plan
     * @param reader  the XML reader
     * @param context the validation context
     * @return true if the mapping was processed successfully, false if there was a validation error
     */
    private boolean processDeployableMapping(DeploymentPlan plan, XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        String deployableName = reader.getAttributeValue(null, "deployable");
        if (deployableName == null) {
            context.addError(new MissingAttribute("Deployable name not specified in mapping", location));
            return false;
        }
        QName deployable = LoaderUtil.getQName(deployableName, null, reader.getNamespaceContext());
        String zoneName = reader.getAttributeValue(null, "zone");
        if (zoneName == null) {
            context.addError(new MissingAttribute("Zone not specified in mapping", location));
            return false;
        }
        plan.addDeployableMapping(deployable, zoneName);
        return true;
    }

    /**
     * Updates the deployment plan ResourceElement with the parsed DeploymentPlan.
     *
     * @param resource the plan resource to update
     * @param plan     the deployment plan
     */
    private void updatePlan(Resource resource, DeploymentPlan plan) {
        String name = plan.getName();
        QName planQName = new QName(PLAN_NAMESPACE, name);
        QNameSymbol symbol = new QNameSymbol(planQName);
        boolean found = false;
        for (ResourceElement element : resource.getResourceElements()) {
            if (element.getSymbol().equals(symbol)) {
                element.setValue(plan);
                found = true;
                break;
            }
        }
        if (!found) {
            // this is a programming error if this happens as the indexer did not set the resource element properly
            throw new AssertionError("Deployment plan not found: " + name);
        }
        resource.setState(ResourceState.PROCESSED);
    }


}