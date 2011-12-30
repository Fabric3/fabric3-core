package org.fabric3.cache.infinispan.util;

import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

/**
 * @version $Rev$ $Date$
 */
public class XmlHelper {

    private XmlHelper() {
    }

    /**
     * Transforms a DOM node to a String.
     *
     * @param node the node
     * @return the transformed XML representation
     * @throws TransformerException if a transformation error occurs.
     */
    public static String transform(Node node) throws TransformerException {
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        Source source = new DOMSource(node);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, result);
        return writer.toString();
    }
}
