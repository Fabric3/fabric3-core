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

import org.exist.xquery.Cardinality;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.DoubleValue;
import org.exist.xquery.value.Type;

/** 
 * @version $Rev$ $Date$
 */
public class DoubleTransformer extends AbstractTransformer <Double,DoubleValue> {

    public Double transformFrom(DoubleValue value,Class<?> type,XQueryContext ctx) throws XPathException {
        return value.getDouble();
    }

    public DoubleValue transformTo(Double value, XQueryContext ctx) throws XPathException {
        return new DoubleValue(value);
    }

    public  int valueType() {
        return Type.DOUBLE;
    }
    
    public int cardinality(){
        return Cardinality.ZERO_OR_ONE;
    }

    public Class<Double> source() {
        return Double.class;
    }

    public Class<DoubleValue> target() {
        return DoubleValue.class;
    }


}
