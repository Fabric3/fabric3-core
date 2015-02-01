package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 *
 */
public class CompositeContributionProcessorTestCase extends TestCase {
    private static final String XML
            = "<composite xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' name='test' targetNamespace='urn:foo.com'></composite>";

    private CompositeContributionProcessor processor;
    private LoaderRegistry registry;

    private Contribution contribution;
    private IntrospectionContext context;

    private XMLInputFactory factory;

    public void testConfigProcessor() throws Exception {
        Composite composite = new Composite(new QName("foo", "bar"));
        EasyMock.expect(registry.load((Source) EasyMock.isNull(), EasyMock.eq(Composite.class), EasyMock.isA(IntrospectionContext.class))).andReturn(composite);
        EasyMock.replay(registry);

        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();

        processor.process(contribution, context);

        ContributionManifest manifest = contribution.getManifest();
        assertFalse(manifest.getDeployables().isEmpty());
        assertFalse(manifest.getExports().isEmpty());

        EasyMock.verify(registry);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(LoaderRegistry.class);
        processor = new CompositeContributionProcessor(registry, null);

        contribution = new Contribution(URI.create("test"));
        contribution.setManifest(new ContributionManifest());

        context = new DefaultIntrospectionContext();

        factory = XMLInputFactory.newInstance();

    }
}
