package org.fabric3.contribution.updater;

import java.net.URI;
import java.util.Collections;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Include;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 * @version $Rev$ $Date$
 */
public class CompositeResourceElementUpdaterTestCase extends TestCase {
    private CompositeResourceElementUpdater updater;
    private Composite newComposite;
    private Composite referringComposite;
    private Contribution contribution;

    public void testImplementationUpdate() throws Exception {
        updater.update(newComposite, contribution, Collections.<Contribution>emptySet());
        for (ComponentDefinition child : referringComposite.getDeclaredComponents().values()) {
            Composite composite = (Composite) child.getImplementation().getComponentType();
            assertEquals(newComposite, composite);
        }
        for (Include include : referringComposite.getIncludes().values()) {
            assertEquals(newComposite, include.getIncluded());
        }

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        updater = new CompositeResourceElementUpdater();
        QName name = new QName("test", "referred");
        Composite oldComposite = new Composite(name);
        newComposite = new Composite(name);
        referringComposite = new Composite(new QName("test", "referring"));

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
        Resource resource = new Resource(contribution, null, "");
        QNameSymbol symbol = new QNameSymbol(name);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol, referringComposite);
        resource.addResourceElement(element);
        contribution.addResource(resource);
    }
}
