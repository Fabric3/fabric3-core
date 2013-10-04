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
package org.fabric3.exist.runtime;

import java.util.Map;
import org.exist.EXistException;
import org.exist.dom.QName;
import org.exist.storage.DBBroker;
import org.exist.xquery.CompiledXQuery;
import org.exist.xquery.Variable;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Sequence;
import org.fabric3.exist.ExistDBInstance;
import org.fabric3.exist.transform.TransformerRegistry;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageImpl;
import org.fabric3.spi.container.wire.Interceptor;

/**
 *
 */
public class ExistXQueryServiceTargetInterceptor implements Interceptor {

    private Interceptor next;
    private boolean analyzed = false;
    final private CompiledXQuery compiledXQuery;
    final private ExistDBInstance instance;
    final private TransformerRegistry trRegistry;

    public ExistXQueryServiceTargetInterceptor(CompiledXQuery compiledXQuery, TransformerRegistry trRegistry, ExistDBInstance instance) {
        this.instance = instance;
        this.compiledXQuery = compiledXQuery;
        this.trRegistry = trRegistry;
    }

    public Message invoke(Message msg) {
        Message message = new MessageImpl();
        DBBroker broker = null;
        try {
            broker = instance.getInstance();
            XQueryContext context = compiledXQuery.getContext();
            context.setBroker(broker);
            Object[] args = (Object[]) msg.getBody();
            if (args != null && args.length == 2) {
                Map<javax.xml.namespace.QName,Object> map = (Map<javax.xml.namespace.QName,Object>) args[0];
                for (Map.Entry<javax.xml.namespace.QName,Object>entry : map.entrySet() ){
                    javax.xml.namespace.QName varName = entry.getKey();
                    Object val = entry.getValue();
                    QName name =new QName(varName.getLocalPart(), varName.getNamespaceURI(), varName.getPrefix());
                    Variable var = new Variable(name);
                    var.setValue(trRegistry.getTransformer(val.getClass()).transformTo(val, context));
                    context.declareGlobalVariable(var);
                }
            }
            Sequence returnVal = compiledXQuery.eval(null);
            Class type =(Class)args[1];
            message.setBody(trRegistry.getTransformer(type).transformFrom(returnVal, type,context));
        } catch (EXistException ex) {
            message.setBodyWithFault(ex);
        } catch (XPathException ex) {
            message.setBodyWithFault(ex);
        } finally {
            try {
                instance.releaseInstance(broker);
            } catch (EXistException ex) {
                message.setBodyWithFault(ex);
            }
        }
        return message;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Interceptor getNext() {
        return next;
    }
}
