package org.fabric3.contribution.introspector;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 *
 */
@EagerInit
public class CompositeReferenceIntrospectorTestCase extends TestCase {
    private CompositeReferenceIntrospector introspector;
    private ResourceElement<QNameSymbol, Composite> referred;

    public void testIncludeReferences() throws Exception {
        QName name = new QName("foo", "referrer");
        QNameSymbol symbol = new QNameSymbol(name);
        Composite composite = new Composite(name);
        ResourceElement<QNameSymbol, Composite> refers = new ResourceElement<>(symbol, composite);

        Include include = new Include();
        include.setName(new QName("included"));
        include.setIncluded(referred.getValue());
        composite.add(include);

        assertTrue(introspector.references(referred, refers));
    }

    public void testImplementationReferences() throws Exception {
        QName name = new QName("foo", "referrer");
        QNameSymbol symbol = new QNameSymbol(name);
        Composite composite = new Composite(name);
        ResourceElement<QNameSymbol, Composite> refers = new ResourceElement<>(symbol, composite);

        ComponentDefinition<CompositeImplementation> child = new ComponentDefinition<>("referrer");
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(referred.getValue());
        child.setImplementation(implementation);
        composite.add(child);

        assertTrue(introspector.references(referred, refers));

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        introspector = new CompositeReferenceIntrospector();

        QName name = new QName("foo", "referred");
        QNameSymbol symbol = new QNameSymbol(name);
        Composite composite = new Composite(name);
        referred = new ResourceElement<>(symbol, composite);
    }
}
