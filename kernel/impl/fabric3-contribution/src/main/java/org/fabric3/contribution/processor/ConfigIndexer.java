package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.host.Namespaces;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.xml.XmlIndexer;
import org.fabric3.spi.contribution.xml.XmlIndexerRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * No-op indexer for config contributions (which do not require indexing).
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ConfigIndexer implements XmlIndexer {
    private static final QName TYPE = new QName(Namespaces.F3, "config");
    private XmlIndexerRegistry registry;

    @Init
    public void init() {
        registry.register(this);
    }

    @Destroy
    public void destroy() {
        registry.unregister(TYPE);
    }

    public ConfigIndexer(@Reference XmlIndexerRegistry registry) {
        this.registry = registry;
    }


    public QName getType() {
        return TYPE;
    }

    public void index(Resource resource, XMLStreamReader reader, IntrospectionContext context) throws InstallException {
        // no-op
    }
}
