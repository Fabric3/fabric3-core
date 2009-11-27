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
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;

/** 
 * @version $Rev$ $Date$
 */
public class StringTransformer extends AbstractTransformer <String,StringValue> {

    public String transformFrom(StringValue value,Class<?> type,XQueryContext ctx) throws XPathException {
        return value.getStringValue();
    }

    public StringValue transformTo(String value, XQueryContext ctx) throws XPathException {
        return new StringValue(value);
    }

    public  int valueType() {
        return Type.STRING;
    }
    
    public int cardinality(){
        return Cardinality.ZERO_OR_ONE;
    }

    public Class<String> source() {
        return String.class;
    }

    public Class<StringValue> target() {
        return StringValue.class;
    }


}
