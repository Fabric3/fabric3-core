/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.exist.transform;

import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.exist.memtree.DocumentBuilderReceiver;
import org.exist.memtree.MemTreeBuilder;
import org.exist.memtree.NodeImpl;
import org.exist.util.serializer.DOMStreamer;
import org.exist.util.serializer.SerializerPool;
import org.exist.xquery.Cardinality;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** 
 * @version $Rev$ $Date$
 */
public class NodeListTransformer extends AbstractTransformer<NodeList, ValueSequence> {

    DocumentBuilder documentBuilder;

    public NodeListTransformer() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        documentBuilder = factory.newDocumentBuilder();
    }

    public NodeList transformFrom(ValueSequence value, Class<?> type, XQueryContext ctx) throws XPathException {
        try {
            Document doc = documentBuilder.newDocument();
            final ArrayList<Node> nodeList=new ArrayList<Node>(value.getItemCount());
            for (SequenceIterator i = value.iterate(); i.hasNext();) {
                Item next = i.nextItem();
                //Node node = doc.importNode((Node) next, true);
                nodeList.add((Node) next);
            }
            return new NodeList() {

                public Node item(int index) {
                    return nodeList.get(index);
                }

                public int getLength() {
                    return nodeList.size();
                }
            };
        } catch (Exception e) {
            throw new XPathException(e);
        }
    }

    public ValueSequence transformTo(NodeList value, XQueryContext ctx) throws XPathException {
        DOMStreamer streamer = (DOMStreamer) SerializerPool.getInstance().borrowObject(DOMStreamer.class);
        try {
            MemTreeBuilder builder = new MemTreeBuilder();
            builder.startDocument();
            DocumentBuilderReceiver receiver = new DocumentBuilderReceiver(
                    builder);
            streamer.setContentHandler(receiver);
            ValueSequence seq = new ValueSequence();
            int last = builder.getDocument().getLastNode();
            for (int i = 0; i < value.getLength(); i++) {
                Node n = value.item(i);
                streamer.serialize(n, false);
                NodeImpl created = builder.getDocument().getNode(last + 1);
                seq.add(created);
                last = builder.getDocument().getLastNode();
            }
            return seq;
        } catch (SAXException e) {
            throw new XPathException(
                    "Failed to transform node into internal model: " + e.getMessage());
        } finally {
            SerializerPool.getInstance().returnObject(streamer);
        }
    }

    public int valueType() {
        return Type.ITEM;
    }

    public int cardinality(){
        return Cardinality.ZERO_OR_MORE;
    }
    
    public Class<NodeList> source() {
        return NodeList.class;
    }

    public Class<ValueSequence> target() {
        return ValueSequence.class;
    }
}
