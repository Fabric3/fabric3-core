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
package org.fabric3.contribution.updater;

import java.net.URI;
import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 *
 */
public class CompositeResourceElementUpdaterTestCase extends TestCase {
    private CompositeResourceElementUpdater updater;
    private Composite oldComposite;
    private Composite newComposite;
    private Composite referringComposite;
    private Contribution contribution;

    public void testUpdate() throws Exception {
        Set<ModelObject> set = updater.update(newComposite, contribution, Collections.<Contribution>emptySet());
        assertEquals(2, set.size());
        assertTrue(set.contains(oldComposite));
        assertTrue(set.contains(referringComposite));

        for (ComponentDefinition child : referringComposite.getDeclaredComponents().values()) {
            Composite composite = (Composite) child.getImplementation().getComponentType();
            assertEquals(newComposite, composite);
        }
        for (Include include : referringComposite.getIncludes().values()) {
            assertEquals(newComposite, include.getIncluded());
        }
    }

    public void testRemove() throws Exception {
        Set<ModelObject> set = updater.remove(oldComposite, contribution, Collections.<Contribution>emptySet());
        assertEquals(2, set.size());
        assertTrue(set.contains(oldComposite));
        assertTrue(set.contains(referringComposite));

        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (element.getValue() == oldComposite) {
                    fail("Failed to remove old composite");
                }
            }
        }
        for (ComponentDefinition child : referringComposite.getDeclaredComponents().values()) {
            Composite composite = (Composite) child.getImplementation().getComponentType();
            assertNotSame(oldComposite, composite);
            assertTrue(composite.isPointer());
        }
        for (Include include : referringComposite.getIncludes().values()) {
            Composite composite = include.getIncluded();
            assertNotSame(oldComposite, composite);
            assertTrue(composite.isPointer());
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        updater = new CompositeResourceElementUpdater();
        QName referredName = new QName("test", "referred");
        oldComposite = new Composite(referredName);
        newComposite = new Composite(referredName);
        QName referringName = new QName("test", "referring");
        referringComposite = new Composite(referringName);

        ComponentDefinition<CompositeImplementation> child = new ComponentDefinition<>("child");
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(oldComposite);
        child.setImplementation(implementation);
        referringComposite.add(child);

        Include include = new Include();
        include.setName(new QName("test", "included"));
        include.setIncluded(oldComposite);
        referringComposite.add(include);

        contribution = new Contribution(URI.create("contribution"));

        Resource referredResource = new Resource(contribution, null, "");
        QNameSymbol referredSymbol = new QNameSymbol(referredName);
        ResourceElement<QNameSymbol, Composite> referredElement = new ResourceElement<>(referredSymbol, oldComposite);
        referredResource.addResourceElement(referredElement);
        contribution.addResource(referredResource);

        Resource referringResource = new Resource(contribution, null, "");
        QNameSymbol referringSymbol = new QNameSymbol(referringName);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(referringSymbol, referringComposite);
        referringResource.addResourceElement(element);

        contribution.addResource(referringResource);
    }
}
