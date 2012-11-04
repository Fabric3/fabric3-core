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

import java.lang.reflect.Array;
import org.exist.xquery.Cardinality;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;

/** */
public class ArrayTransformer extends AbstractTransformer<Object, ValueSequence> {

    @Override
    public void init() {
        registry.register(Object[].class,this);
    }

    @Override
    public void destroy() {
        registry.unregister(Object[].class,this);
    }
    
    public Object transformFrom(ValueSequence value, Class<?> type, XQueryContext ctx) throws XPathException {
        Class componentClass = type.getComponentType();
        Object[] list = (Object[]) Array.newInstance(componentClass, value.getItemCount());
        int j = 0;
        for (SequenceIterator i = value.iterate(); i.hasNext();) {
            Item next = i.nextItem();
            Sequence seq = next.toSequence();
            Transformer t;
            if (seq.getItemCount() > 1) {
                t = this;
            } else {
                t = registry.getTransformer(next.getType());
            }
            list[j++] = t.transformFrom(next.toSequence(), type.getComponentType(), ctx);
        }
        return list;
    }

    public ValueSequence transformTo(Object array, XQueryContext ctx) throws XPathException {
        ValueSequence seq = new ValueSequence();
        for (int i = 0; i < Array.getLength(array); i++) {
            Object value = Array.get(array, i);
            if (value != null) {
                Transformer t = registry.getTransformer(value.getClass());
                Sequence val = t.transformTo(value, ctx);
                if (val instanceof Item) {
                    seq.add((Item) val);
                } else {
                    seq.addAll(val);
                }

            }
        }
        return seq;
    }

    public int valueType() {
        return Type.ITEM;
    }

    public int cardinality() {
        return Cardinality.ZERO_OR_MORE;
    }

    public Class<Object> source() {
        return Object.class;
    }

    public Class<ValueSequence> target() {
        return ValueSequence.class;
    }
}
