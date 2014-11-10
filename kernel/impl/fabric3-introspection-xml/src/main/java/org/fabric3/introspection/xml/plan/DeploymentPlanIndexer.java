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
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.xml.XmlIndexer;
import org.fabric3.spi.contribution.xml.XmlIndexerRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.model.plan.DeploymentPlan;

import static org.fabric3.introspection.xml.plan.DeploymentPlanConstants.PLAN;
import static org.fabric3.introspection.xml.plan.DeploymentPlanConstants.PLAN_NAMESPACE;

/**
 * Indexes a deployment plan.
 */
@EagerInit
public class DeploymentPlanIndexer implements XmlIndexer {
    private XmlIndexerRegistry registry;

    public DeploymentPlanIndexer(@Reference XmlIndexerRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public QName getType() {
        return PLAN;
    }

    public void index(Resource resource, XMLStreamReader reader, IntrospectionContext context) {
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            context.addError(new MissingAttribute("Deployment plan name not specified", startLocation));
            return;
        }
        QName planQName = new QName(PLAN_NAMESPACE, name);
        QNameSymbol symbol = new QNameSymbol(planQName);
        ResourceElement<QNameSymbol, DeploymentPlan> element = new ResourceElement<>(symbol);
        resource.addResourceElement(element);
    }

}