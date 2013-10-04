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

import org.exist.EXistException;
import org.exist.storage.DBBroker;
import org.exist.xquery.FunctionCall;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Sequence;
import org.fabric3.exist.ExistDBInstance;
import org.fabric3.exist.transform.Transformer;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageImpl;
import org.fabric3.spi.container.wire.Interceptor;

/**
 * * Invokes function from Fabric3 Runtime into exist DB
 * */
public class ExistXQueryTargetInterceptor implements Interceptor {

    private Interceptor next;
    final private FunctionCall functionCall;
    final private ExistDBInstance instance;
    final private Transformer[] paramTransforms;
    final private Transformer returnTransform;
    final private Class<?> returnType;

    public ExistXQueryTargetInterceptor(FunctionCall functionCall, Transformer[] paramTransforms, Transformer returnTransform, Class<?> returnType, ExistDBInstance instance) {
        this.instance = instance;
        this.functionCall = functionCall;
        this.paramTransforms = paramTransforms;
        this.returnTransform = returnTransform;
        this.returnType = returnType;
    }

    public Message invoke(Message msg) {
        Message message = new MessageImpl();
        DBBroker broker = null;
        try {
            broker = instance.getInstance();
            XQueryContext context = functionCall.getContext();
            context.setBroker(broker);
            Object[] args = (Object[]) msg.getBody();
            Sequence[] params = new Sequence[paramTransforms.length];
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    params[i] = paramTransforms[i].transformTo(args[i], context);
                }
            }
            Sequence returnVal = functionCall.evalFunction(null, null, params);
            message.setBody(returnTransform.transformFrom(returnVal, returnType, context));
        } catch (Throwable ex) {
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
