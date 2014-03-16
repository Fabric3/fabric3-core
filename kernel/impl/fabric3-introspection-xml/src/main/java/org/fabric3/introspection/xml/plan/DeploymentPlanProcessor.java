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