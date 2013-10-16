package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;

import org.fabric3.api.host.Namespaces;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.xml.XmlProcessor;
import org.fabric3.spi.contribution.xml.XmlProcessorRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 * Processes XML config contributions. Config XML contributions are used to deploy properties to the domain that may be referenced by components in
 * other contributions. This provides an accessible mechanism for deploying configuration separate from application code archives.
 * <p/>
 * Config files must use the <code>config</code> element as the document root. The <code>config</code> element has two attributes, name and
 * targetNamespace. As show in the following example, name identifies the property name and targetNamespaces identifies the namespace the config
 * contribution exports:
 * <p/>
 * <pre>
 * &lt;config xmlns="urn:fabric3.org"
 *       name="MyProperty"
 *       targetNamespace="urn:foo.com"&gt;
 *
 *   &lt;foo xmlns="urn:foo.com"&gt;
 *       &lt;bar/&gt;
 *   &lt;/foo&gt;
 * &lt;/config&gt;
 * </pre>
 * <p/>
 * The exported namespace can be imported by another contribution that depends on the property to guarantee the runtime performs contribution
 * ordering.
 * <p/>
 * The implementation works by parsing the contents of the config file into a property which is added to a synthetic deployable composite. When the
 * composite is deployed, the property will be added to the domain where it can be referenced.
 */
@EagerInit
public class ConfigProcessor implements XmlProcessor {
    private static final QName TYPE = new QName(Namespaces.F3, "config");
    private LoaderHelper loaderHelper;
    private XmlProcessorRegistry processorRegistry;

    public ConfigProcessor(@Reference XmlProcessorRegistry processorRegistry, @Reference LoaderHelper loaderHelper) {
        this.processorRegistry = processorRegistry;
        this.loaderHelper = loaderHelper;
    }

    @Init
    public void init() {
        processorRegistry.register(this);
    }

    @Destroy
    public void destroy() {
        processorRegistry.unregister(TYPE);
    }

    public QName getType() {
        return TYPE;
    }

    public void processContent(Contribution contribution, XMLStreamReader reader, IntrospectionContext context) throws InstallException {
        try {
            String localName = reader.getAttributeValue(null, "name");
            String targetNamespace = reader.getAttributeValue(null, "targetNamespace");

            Document value = loaderHelper.loadPropertyValues(reader);

            QName name = new QName(targetNamespace, "F3Synthetic" + localName);

            Property property = new Property(localName);
            property.setDefaultValue(value);
            Composite composite = new Composite(name);

            composite.add(property);

            Resource resource = new Resource(contribution, null, "application/xml");
            QNameSymbol symbol = new QNameSymbol(name);
            ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol);
            element.setValue(composite);
            resource.addResourceElement(element);
            resource.setState(ResourceState.PROCESSED);

            contribution.addResource(resource);

            ContributionManifest manifest = contribution.getManifest();

            Deployable deployable = new Deployable(name);
            manifest.addDeployable(deployable);

            QNameExport export = new QNameExport(targetNamespace);
            manifest.addExport(export);
        } catch (XMLStreamException e) {
            throw new InstallException(e);
        }
    }

}
