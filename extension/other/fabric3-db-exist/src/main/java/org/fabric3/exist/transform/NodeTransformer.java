/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.exist.transform;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.exist.memtree.DocumentBuilderReceiver;
import org.exist.memtree.MemTreeBuilder;
import org.exist.util.serializer.DOMStreamer;
import org.exist.util.serializer.SerializerPool;
import org.exist.xquery.Cardinality;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/** */
public class NodeTransformer extends AbstractTransformer<Node, Sequence> {


    DocumentBuilder documentBuilder;

    public NodeTransformer() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        documentBuilder = factory.newDocumentBuilder();
    }

    //TODO Do I need to create a deep clone or is returning the node sufficient?
    public Node transformFrom(Sequence value, Class<?> type, XQueryContext ctx) throws XPathException {
        try {
            if (value.getItemCount() > 0) {
                if (type.equals(Document.class)) {
                    Document doc = documentBuilder.newDocument();
                    Node node =doc.importNode((Node) value.itemAt(0), false);
                    doc.appendChild(node);
                    return doc;
                } else {
                    //Document doc = documentBuilder.newDocument();
                    //Node node =doc.importNode((Node) value.itemAt(0), true);
                    Node node = (Node) value.itemAt(0);
                    return node;
                }
            }
        return null;
        } catch (Exception e) {
            throw new XPathException(e);
        }
    }

    public Sequence transformTo(Node value, XQueryContext ctx) throws XPathException {
        DOMStreamer streamer = (DOMStreamer) SerializerPool.getInstance().borrowObject(DOMStreamer.class);
        try {
            MemTreeBuilder builder = new MemTreeBuilder(ctx);
            builder.startDocument();
            DocumentBuilderReceiver receiver = new DocumentBuilderReceiver(
                    builder);
            streamer.setContentHandler(receiver);
            streamer.serialize(value, false);
            return builder.getDocument().getNode(1);
        } catch (SAXException e) {
            throw new XPathException(
                    "Failed to transform node into internal model: " + e.getMessage());
        } finally {
            SerializerPool.getInstance().returnObject(streamer);
        }
    }

    public int valueType() {
        return Type.NODE;
    }
    
    public int cardinality(){
        return Cardinality.ZERO_OR_ONE;
    }

    public Class<Node> source() {
        return Node.class;
    }

    public Class<Sequence> target() {
        return Sequence.class;
    }
}
