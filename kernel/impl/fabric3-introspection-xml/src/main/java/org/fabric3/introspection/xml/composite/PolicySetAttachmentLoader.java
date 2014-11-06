package org.fabric3.introspection.xml.composite;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class PolicySetAttachmentLoader extends AbstractExtensibleTypeLoader<QName> {
    private static final QName QNAME = new QName(Constants.SCA_NS, "policySetAttachment");
    private LoaderHelper helper;

    public PolicySetAttachmentLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper helper) {
        super(registry);
        this.helper = helper;
        addAttributes("name");
    }

    public QName getXMLType() {
        return QNAME;
    }

    public QName load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String nameAttribute = reader.getAttributeValue(null, "name");
        if (nameAttribute == null) {
            MissingAttribute error = new MissingAttribute("Missing name attribute", reader.getLocation());
            context.addError(error);
            return null;
        }
        try {
            return helper.createQName(nameAttribute, reader);
        } catch (InvalidPrefixException e) {
            InvalidValue error = new InvalidValue("Invalid policy name", reader.getLocation());
            context.addError(error);
            return null;
        }

    }
}
