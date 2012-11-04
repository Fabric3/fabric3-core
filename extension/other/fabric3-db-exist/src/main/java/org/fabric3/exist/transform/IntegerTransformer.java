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

import org.exist.xquery.Cardinality;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.IntegerValue;
import org.exist.xquery.value.Type;

/** */
public class IntegerTransformer extends AbstractTransformer <Integer,IntegerValue> {

    public Integer transformFrom(IntegerValue value,Class<?> type,XQueryContext ctx) throws XPathException {
        return value.getInt();
    }

    public IntegerValue transformTo(Integer value, XQueryContext ctx) throws XPathException {
        return new IntegerValue(value, Type.INTEGER);
    }

    public  int valueType() {
        return Type.INTEGER;
    }
    
    public int cardinality(){
        return Cardinality.ZERO_OR_ONE;
    }

    public Class<Integer> source() {
        return Integer.class;
    }

    public Class<IntegerValue> target() {
        return IntegerValue.class;
    }


}
