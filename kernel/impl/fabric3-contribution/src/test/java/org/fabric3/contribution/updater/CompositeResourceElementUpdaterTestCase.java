/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.contribution.updater;

import java.net.URI;
import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.model.type.ModelObject;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Include;
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

        ComponentDefinition<CompositeImplementation> child = new ComponentDefinition<CompositeImplementation>("child");
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
        ResourceElement<QNameSymbol, Composite> referredElement = new ResourceElement<QNameSymbol, Composite>(referredSymbol, oldComposite);
        referredResource.addResourceElement(referredElement);
        contribution.addResource(referredResource);

        Resource referringResource = new Resource(contribution, null, "");
        QNameSymbol referringSymbol = new QNameSymbol(referringName);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(referringSymbol, referringComposite);
        referringResource.addResourceElement(element);

        contribution.addResource(referringResource);
    }
}
