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

import javax.xml.stream.XMLStreamReader;
import org.exist.memtree.MemTreeBuilder;
import org.exist.memtree.NodeImpl;
import org.exist.xquery.Cardinality;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Type;
//TODO Hold off on implementing this until the exist XQJ code gets merged into the trunk
//We don't want to reinvent the wheel http://fisheye3.atlassian.com/browse/exist/branches/allad/src/org/exist/xqj/Marshaller.java?r=7983
/** 
 * @version $Rev$ $Date$
 */
public class XMLStreamReaderTransformer extends AbstractTransformer <XMLStreamReader,NodeImpl> {

    public XMLStreamReader transformFrom(NodeImpl value,Class<?> type,XQueryContext ctx) throws XPathException {
       // doc.getBroker().getXMLStreamReader(parent, true);
        return null;

    }

    public NodeImpl transformTo(XMLStreamReader value, XQueryContext ctx) throws XPathException {

                MemTreeBuilder builder = new MemTreeBuilder(ctx);

         return null;
    }

    public  int valueType() {
        return Type.ITEM;
    }
    
    public int cardinality(){
        return Cardinality.ZERO_OR_ONE;
    }

    public Class<XMLStreamReader> source() {
        return XMLStreamReader.class;
    }

    public Class<NodeImpl> target() {
        return NodeImpl.class;
    }


}
