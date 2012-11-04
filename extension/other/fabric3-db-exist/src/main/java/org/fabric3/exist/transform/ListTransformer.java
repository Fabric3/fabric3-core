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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.exist.xquery.Cardinality;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;

/** */
public class ListTransformer extends AbstractTransformer<List, ValueSequence> {

    public List transformFrom(ValueSequence value, Class<?> type, XQueryContext ctx) throws XPathException {
        ArrayList list = new ArrayList();
        for (SequenceIterator i = value.iterate(); i.hasNext();) {
            Item next = i.nextItem();
            Sequence seq = next.toSequence();
            Transformer t;
            if (seq.getItemCount()>1){
                t = this;
            }else{
                t = registry.getTransformer(next.getType());
            }
            list.add(t.transformFrom(seq, type, ctx));
        }
        return list;
    }

    public ValueSequence transformTo(List value, XQueryContext ctx) throws XPathException {
        ValueSequence seq = new ValueSequence();
        for (Iterator i = value.iterator(); i.hasNext();) {
            Object next = i.next();
            Transformer t = registry.getTransformer(next.getClass());
            Sequence val = t.transformTo(next, ctx);
            if (val instanceof Item) {
                seq.add((Item) val);
            } else {
                seq.addAll(val);
            }
        }
        return seq;
    }

    public int valueType() {
        return Type.ITEM;
    }
    
    public int cardinality(){
        return Cardinality.ZERO_OR_MORE;
    }

    public Class<List> source() {
        return List.class;
    }

    public Class<ValueSequence> target() {
        return ValueSequence.class;
    }
}
