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
package org.fabric3.exist.runtime;

import org.exist.dom.DocumentSet;
import org.exist.xquery.AnalyzeContextInfo;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.UserDefinedFunction;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.fabric3.exist.transform.Transformer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Invokes function from exist DB into Fabric3 Runtime
 * 
 * @version $Rev$ $Date$
 */
public class F3ExistXQueryFunction extends UserDefinedFunction {

    final InvocationChain chain;
    final String callbackURI;
    final Transformer[] paramTransforms;
    final Transformer returnTransform;
    final private Class<?>[] paramTypes;
    private Sequence[] args;

    public F3ExistXQueryFunction(XQueryContext ctx, FunctionSignature sig, String callbackURI, InvocationChain chain, Transformer[] paramTransforms, Class<?>[] paramTypes, Transformer returnTransform) {
        super(ctx, sig);
        this.chain = chain;
        this.callbackURI = callbackURI;
        this.paramTransforms = paramTransforms;
        this.paramTypes=paramTypes;
        this.returnTransform = returnTransform;


    }

    @Override
    public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException {
        Object[] parameters = new Object[paramTransforms.length];
        for (int i = 0; i < parameters.length && i < args.length; i++) {
            Sequence seq ;
            if (paramTransforms[i].cardinality()> Cardinality.MANY) {
                seq = args[i];
            }else if (paramTransforms[i].cardinality()> Cardinality.ZERO) {
                seq = (Sequence) args[i].itemAt(0);
            } else {
                continue;
            }
            parameters[i] = paramTransforms[i].transformFrom(seq, paramTypes[i], null);
        }

        WorkContext workContext = new WorkContext();
        if (callbackURI != null) {
            // the wire is bidrectional so a callframe is required
            CallFrame frame = new CallFrame(callbackURI, null, null, null);
            workContext.addCallFrame(frame);
        }
        Interceptor head = chain.getHeadInterceptor();
        Message input = new MessageImpl(parameters, false, workContext);

        Message output = head.invoke(input);
        if (output.isFault()) {
            throw new XPathException((Throwable) output.getBody());
        } else {
            return returnTransform.transformTo(output.getBody(), context);
        }
    }

    @Override
    public void setArguments(Sequence[] args, DocumentSet[] docs) throws XPathException {
        this.args = args;
    }

    @Override
    public void analyze(AnalyzeContextInfo info) throws XPathException {
    }

    @Override
    public int returnsType() {
        return returnTransform.valueType();
    }
}
