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
package org.fabric3.exist.introspection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.exist.EXistException;
import org.exist.security.xacml.AccessContext;
import org.exist.util.DatabaseConfigurationException;
import org.exist.xquery.FunctionCall;
import org.exist.xquery.FunctionId;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.UserDefinedFunction;
import org.exist.xquery.Variable;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.SequenceType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.component.InvalidImplementation;
import org.fabric3.xquery.scdl.XQueryComponentType;
import org.fabric3.xquery.scdl.XQueryImplementation;
import org.fabric3.xquery.scdl.XQueryProperty;
import org.fabric3.xquery.scdl.XQueryServiceContract;

public class IntrospectingXQueryContext extends XQueryContext {

    protected Map<String, String> properties = new HashMap<String, String>();
    protected Map<String, QName> references = new HashMap<String, QName>();
    protected Map<String, QName> services = new HashMap<String, QName>();
    protected Map<String, QName> callbacks = new HashMap<String, QName>();
    protected Map<String, QName> callbackNames = new HashMap<String, QName>();
    protected Map<QName, String> serviceCallbacks = new HashMap<QName, String>();
    protected Map<QName, String> referenceCallbacks = new HashMap<QName, String>();
    protected Map<String, QName> propertyNames = new HashMap<String, QName>();
    protected Map<QName, List<Operation>> referenceOperations = new HashMap<QName, List<Operation>>();
    protected Map<QName, List<Operation>> serviceOperations = new HashMap<QName, List<Operation>>();
    protected Map<QName, List<Operation>> callbackOperations = new HashMap<QName, List<Operation>>();

    /**
     * As the name implies this context class is used for the execution context of a query. This subclass attempts to keep track
     * of the variable and function declarations so that it can populate the Xquery implementation definition. 
     * 
     * @version $Rev$ $Date$
     */
    public IntrospectingXQueryContext() throws EXistException, DatabaseConfigurationException {
        super(AccessContext.XMLDB);
        loadDefaultNS();
    }

    public void process(XQueryImplementation impl, IntrospectionContext context) {
        XQueryComponentType type = impl.getComponentType();

        for (Map.Entry<String, QName> entry : services.entrySet()) {
            QName serviceName = entry.getValue();
            ServiceDefinition service = new ServiceDefinition(serviceName.getLocalPart(), buildContract(serviceName, serviceOperations.get(serviceName), context));
            type.add(service);
        }

        for (Map.Entry<String, QName> entry : references.entrySet()) {
            QName referenceName = entry.getValue();
            ReferenceDefinition reference = new ReferenceDefinition(referenceName.getLocalPart(), buildContract(referenceName, referenceOperations.get(referenceName), context));
            type.add(reference);
        }

        for (Map.Entry<QName, String> entry : serviceCallbacks.entrySet()) {
            QName serviceName = entry.getKey();
            String callback = entry.getValue();
            QName callbackName = callbackNames.get(callback);
            if (callbackName == null) {
                context.addError(new InvalidImplementation("Unable to find callback " + callbackName + " for service ", serviceName.getLocalPart()));
            }
            ServiceContract contact =buildContract(callbackName, callbackOperations.get(callbackName), context);
                        
            ServiceDefinition service = type.getServices().get(serviceName.getLocalPart());
            if (service != null) {
                service.getServiceContract().setCallbackContract(contact);
            } 
        }

        for (Map.Entry<QName, String> entry : referenceCallbacks.entrySet()) {
            QName referenceName = entry.getKey();
            String callbackName = entry.getValue();
            ServiceDefinition callback = type.getServices().get(callbackName);
            if (callback == null) {
                context.addError(new InvalidImplementation("Unable to find callback service " + callbackName + " for reference ", referenceName.getLocalPart()));
            }
            ReferenceDefinition reference = type.getReferences().get(referenceName.getLocalPart());
            if (reference != null) {
                reference.getServiceContract().setCallbackContract(callback.getServiceContract());
            } 
        }

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String propertyName = entry.getValue();
            QName variable = propertyNames.get(propertyName);
            XQueryProperty property = new XQueryProperty();
            property.setName(propertyName);
            property.setVariableName(variable);
            type.add(property);
        }
    }

    protected XQueryServiceContract buildContract(QName name, List<Operation> operations, IntrospectionContext context) {

        if (operations == null || operations.size() == 0) {
            context.addWarning(new InvalidImplementation("No operations defined for ", name.toString()));
        }
        XQueryServiceContract contract = new XQueryServiceContract();
        contract.setRemotable(false);
        contract.setQname(name);
        contract.setOperations(operations);
        return contract;

    }

    @Override
    public void declareNamespace(String prefix, String uri) throws XPathException {
        super.declareNamespace(prefix, uri);
        QName qName = null;
        if (uri.startsWith("sca:service:")) {
            String serviceName = uri.substring(12);
            int index = serviceName.indexOf(":callback:");
            if (index > -1) {
                String callback = serviceName.substring(index + 10);
                serviceName = serviceName.substring(0, index);
                qName = new QName(uri, serviceName, prefix);
                serviceCallbacks.put(qName, callback);

            } else {
                qName = new QName(uri, serviceName, prefix);
            }
            services.put(prefix, qName);
            serviceOperations.put(qName, new ArrayList<Operation>());

        } else if (uri.startsWith("sca:reference:")) {
            String referenceName = uri.substring(14);
            int index = referenceName.indexOf(":callback:");
            if (index > -1) {
                String callback = referenceName.substring(index + 10);
                referenceName = referenceName.substring(0, index);
                qName = new QName(uri, referenceName, prefix);
                referenceCallbacks.put(qName, callback);
            } else {
                qName = new QName(uri, referenceName, prefix);
            }
            references.put(prefix, qName);
            referenceOperations.put(qName, new ArrayList<Operation>());

        } else if (uri.startsWith("sca:callback:")) {
            String callbackName = uri.substring(13);
            qName = new QName(uri, callbackName, prefix);
            callbacks.put(prefix, qName);
            callbackOperations.put(qName, new ArrayList<Operation>());
            callbackNames.put(callbackName, qName);

        } else if (uri.startsWith("sca:property:")) {
            properties.put(prefix, uri.substring(13));
        }
    }

    @Override
    public void resolveForwardReferences() throws XPathException {
        while (!forwardReferences.empty()) {
            FunctionCall call = (FunctionCall) forwardReferences.pop();
            FunctionId id = new FunctionId(call.getQName(), call.getArgumentCount());
            UserDefinedFunction func = (UserDefinedFunction) declaredFunctions.get(id);
            if (func == null) {
                SequenceType[] args = new SequenceType[call.getArgumentCount()];
                Arrays.fill(args, new SequenceType());
                FunctionSignature sig = new FunctionSignature(call.getQName(), args, new SequenceType());
                func = new UserDefinedFunction(this, sig);
                declareFunction(func);
            }
            call.resolveForwardReference(func);
        }
    }

    //TODO When we configure the XQueryComponentType all the service, reference, and properties are resolved
    // but the mappings back to the Xquery references are lost. Does this need a custom IDL to describe the 
    // relationship between the XQuery references and their corresponding SCA definitions?
    @Override
    public void declareFunction(UserDefinedFunction function) throws XPathException {

        if (function.getName().getNamespaceURI().startsWith("sca:")) {
            Operation<QName> operation = getOperation(function);
            String prefix = function.getName().getPrefix();
            if (services.containsKey(prefix)) {
                serviceOperations.get(services.get(prefix)).add(operation);
            } else if (references.containsKey(prefix)) {
                referenceOperations.get(references.get(prefix)).add(operation);
             } else if (callbacks.containsKey(prefix)) {
                callbackOperations.get(callbacks.get(prefix)).add(operation);
            }
        }
        declaredFunctions.put(function.getSignature().getFunctionId(), function);
    }

    protected Operation<QName> getOperation(UserDefinedFunction function) {
        ArrayList<DataType<QName>> parameters = new ArrayList<DataType<QName>>();
        for (int i = 0; i < function.getSignature().getArgumentCount(); i++) {
            parameters.add(new DataType<QName>(Object.class, new QName("")));
        }
        DataType<List<DataType<QName>>> paramVal = new DataType<List<DataType<QName>>>(Object[].class, parameters);
        Operation operation = new Operation(function.getName().getLocalName(), paramVal, new DataType<QName>(Object.class, new QName("")), new ArrayList<DataType<QName>>());
        return operation;
    }

    @Override
    public Variable resolveVariable(org.exist.dom.QName qname) throws XPathException {
        Variable var = super.resolveVariable(qname);
        if (qname.getNamespaceURI().startsWith("sca:")) {
            String prefix = qname.getPrefix();
            String propertyName = properties.get(prefix);
            propertyNames.put(propertyName, new QName(qname.getNamespaceURI(), qname.getLocalName(), qname.getPrefix()));
        }
        return var;
    }
}
