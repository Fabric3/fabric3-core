package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.xml.XmlProcessorRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.w3c.dom.Document;

/**
 *
 */
public class ConfigProcessorTestCase extends TestCase {
    private static final String XML =
            "<config xmlns='urn:fabric3.org' name='test' targetNamespace='urn:foo.com'><foo xmlns='urn:test'><bar/></foo></config>";

    private ConfigProcessor processor;
    private XmlProcessorRegistry registry;
    private LoaderHelper loaderHelper;
    private Contribution contribution;
    private IntrospectionContext context;

    private XMLInputFactory factory;

    public void testConfigProcessor() throws Exception {
        EasyMock.expect(loaderHelper.loadPropertyValues(EasyMock.isA(XMLStreamReader.class))).andReturn(EasyMock.createMock(Document.class));
        EasyMock.replay(registry, loaderHelper);

        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();

        processor.processContent(contribution, reader, context);
        Composite composite = (Composite) contribution.getResources().get(0).getResourceElements().get(0).getValue();
        assertNotNull(composite);
        QName name = composite.getName();
        assertNotNull(name);
        ContributionManifest manifest = contribution.getManifest();
        assertFalse(manifest.getDeployables().isEmpty());
        assertFalse(manifest.getExports().isEmpty());

        EasyMock.verify(registry, loaderHelper);
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(XmlProcessorRegistry.class);
        loaderHelper = EasyMock.createMock(LoaderHelper.class);
        processor = new ConfigProcessor(registry, loaderHelper);

        contribution = new Contribution(URI.create("test"));
        contribution.setManifest(new ContributionManifest());

        context = new DefaultIntrospectionContext();

        factory = XMLInputFactory.newInstance();

    }
}
