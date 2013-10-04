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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.exist.EXistException;
import org.exist.dom.QName;
import org.exist.security.xacml.AccessContext;
import org.exist.source.URLSource;
import org.exist.storage.DBBroker;
import org.exist.xquery.AnalyzeContextInfo;
import org.exist.xquery.CompiledXQuery;
import org.exist.xquery.FunctionCall;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.PathExpr;
import org.exist.xquery.UserDefinedFunction;
import org.exist.xquery.Variable;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQuery;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.EmptySequence;
import org.exist.xquery.value.SequenceType;
import org.fabric3.exist.ExistDBInstance;
import org.fabric3.exist.transform.Transformer;
import org.fabric3.exist.transform.TransformerRegistry;
import org.fabric3.spi.container.builder.WiringException;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 */
public class ExistXQueryCompiler {

    private static final Map<String, Class<?>> PRIMITIVES_TYPES;


    static {
        PRIMITIVES_TYPES = new HashMap<String, Class<?>>();
        PRIMITIVES_TYPES.put("boolean", Boolean.class);
        PRIMITIVES_TYPES.put("byte", Byte.class);
        PRIMITIVES_TYPES.put("short", Short.class);
        PRIMITIVES_TYPES.put("int", Integer.class);
        PRIMITIVES_TYPES.put("long", Long.class);
        PRIMITIVES_TYPES.put("float", Float.class);
        PRIMITIVES_TYPES.put("double", Double.class);
        PRIMITIVES_TYPES.put("char", Character.class);
        PRIMITIVES_TYPES.put("void", Void.class);
    }
    Map<String, Map<String, ExistFunction>> references = new HashMap<String, Map<String, ExistFunction>>();
    Map<String, Map<String, ExistFunction>> services = new HashMap<String, Map<String, ExistFunction>>();
    Map<String, PropertyVariable> variables = new HashMap<String, PropertyVariable>();
    private ExistXQService service;
    private ExistDBInstance instance;
    private ClassLoader cl;
    private URLSource source;
    private TransformerRegistry trRegistry;

    public ExistXQueryCompiler(ClassLoader classLoader, ExistDBInstance instance, TransformerRegistry trRegistry, URLSource source) {
        this.cl = classLoader;
        this.instance = instance;
        this.source = source;
        this.trRegistry = trRegistry;
    }

    public void includeExistFunctionMappings(Map<String, List<javax.xml.namespace.QName>> ExistFunctions, Map<String, Map<String, ExistFunction>> services) {
        for (Map.Entry<String, List<javax.xml.namespace.QName>> entry : ExistFunctions.entrySet()) {
            Map<String, ExistFunction> operations = new HashMap<String, ExistFunction>();
            services.put(entry.getKey(), operations);
            for (javax.xml.namespace.QName function : entry.getValue()) {
                ExistFunction serviceFunction = new ExistFunction();
                serviceFunction.functionName = new QName(function.getLocalPart(), function.getNamespaceURI(), function.getPrefix());
                operations.put(function.getLocalPart(), serviceFunction);
            }
        }
    }

    public void includeReferenceFunctionMappings(Map<String, List<javax.xml.namespace.QName>> referenceFunctions, Map<String, Map<String, ExistFunction>> references) {
        for (Map.Entry<String, List<javax.xml.namespace.QName>> entry : referenceFunctions.entrySet()) {
            Map<String, ExistFunction> operations = new HashMap<String, ExistFunction>();
            references.put(entry.getKey(), operations);
            for (javax.xml.namespace.QName function : entry.getValue()) {
                ExistFunction referenceFunction = new ExistFunction();
                referenceFunction.functionName = new QName(function.getLocalPart(), function.getNamespaceURI(), function.getPrefix());
                operations.put(function.getLocalPart(), referenceFunction);
            }
        }
    }

    public void includeServiceFunctionMappings(Map<String, List<javax.xml.namespace.QName>> ExistFunctions) {
        includeExistFunctionMappings(ExistFunctions, services);
    }

    public void includeReferenceFunctionMappings(Map<String, List<javax.xml.namespace.QName>> referenceFunctions) {
        includeReferenceFunctionMappings(referenceFunctions, references);
    }

    public void includeCallbackFunctionMappings(Map<String, List<javax.xml.namespace.QName>> callbackFunctions) {
        includeReferenceFunctionMappings(callbackFunctions, references);
    }

    public void includePropertyMappings(Map<String, javax.xml.namespace.QName> properties) {
        for (Map.Entry<String, javax.xml.namespace.QName> entry : properties.entrySet()) {
            javax.xml.namespace.QName varName = entry.getValue();
            PropertyVariable var = new PropertyVariable();
            var.varName = new QName(varName.getLocalPart(), varName.getNamespaceURI(), varName.getPrefix());
            variables.put(entry.getKey(), var);
        }
    }

    public void linkPropertyValues(Map<String, Document> propertyValues) throws WiringException {
        for (Map.Entry<String, Document> entry : propertyValues.entrySet()) {
            String name = entry.getKey();
            PropertyVariable var = variables.get(name);
            if (var != null) {
                var.value = entry.getValue();
            } else {
                throw new WiringException("Property does not exist: " + name);
            }
        }
    }

    public void linkSourceWire(String name, InteractionType interactionType, String callbackUri, Wire wire) throws WiringException {
        Map<String, ExistFunction> operations = references.get(name);
        if (operations == null) {
            throw new WiringException(String.format("No operations defined for ", source));
        }
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            PhysicalOperationDefinition op = entry.getKey();
            InvocationChain chain = entry.getValue();
            ExistFunction function = operations.get(op.getName());
            if (function == null) {
                throw new WiringException(String.format("Undefined operation ", op.getName()));
            }
            function.chain = chain;
            function.callbackUri=callbackUri;
            generateSignature(function, op);
        }
    }

    public void linkTargetWire(String name, InteractionType interactionType, Wire wire) throws WiringException {
        Map<String, ExistFunction> operations = services.get(name);
        if (operations == null) {
            if ("XQueryService".equals(name)) {
                Map.Entry<PhysicalOperationDefinition, InvocationChain> entry = wire.getInvocationChains().entrySet().iterator().next();
                PhysicalOperationDefinition op = entry.getKey();
                InvocationChain chain = entry.getValue();
                service = new ExistXQService();
                service.chain = chain;
            } else {
                throw new WiringException(String.format("No operations defined for ", name));
            }
        } else {
            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
                PhysicalOperationDefinition op = entry.getKey();
                InvocationChain chain = entry.getValue();
                ExistFunction function = operations.get(op.getName());
                if (function == null) {
                    throw new WiringException(String.format("Undefined operation ", op.getName()));
                }
                function.chain = chain;
                generateSignature(function, op);
            }
        }

    }

    void generateSignature(ExistFunction function, PhysicalOperationDefinition physicalOperation) throws WiringException {
        function.signature = new FunctionSignature(function.functionName);
        List<String> opArgumentTypes = physicalOperation.getParameters();
        String opReturnType = physicalOperation.getReturnType();

        SequenceType[] argumentTypes = new SequenceType[opArgumentTypes.size()];
        function.paramTransformers = new Transformer[opArgumentTypes.size()];
        function.paramTypes = new Class<?>[opArgumentTypes.size()];
        SequenceType returnType = null;
        try {
            for (int i = 0; i < argumentTypes.length; i++) {
                Class clazz = loadClass(cl, opArgumentTypes.get(i));
                function.paramTypes[i]=clazz;
                function.paramTransformers[i] = trRegistry.getTransformer(clazz);
                argumentTypes[i] = new SequenceType(function.paramTransformers[i].valueType(), function.paramTransformers[i].cardinality());
            }
            Class clazz = loadClass(cl, opReturnType);
            function.returnTransform = trRegistry.getTransformer(clazz);
            function.returnType = clazz;
            returnType = new SequenceType(function.returnTransform.valueType(), function.returnTransform.cardinality());
        } catch (ClassNotFoundException ce) {
            throw new WiringException(ce);
        }

        function.signature.setArgumentTypes(argumentTypes);
        function.signature.setReturnType(returnType);

    }

    public CompiledXQuery compile() throws EXistException, XPathException {
        CompiledXQuery compiledXQuery = null;
        DBBroker broker = null;
        try {
            broker = instance.getInstance();
            if (broker==null){
                throw new EXistException("Unable to obtain a broker for the instance");
            }
            XQuery xquery = broker.getXQueryService();
            XQueryContext context = xquery.newContext(AccessContext.XMLDB);
            for (Map<String, ExistFunction> functions : references.values()) {
                for (ExistFunction function : functions.values()) {
                    UserDefinedFunction func =new F3ExistXQueryFunction(context, function.signature, function.callbackUri, function.chain, function.paramTransformers,function.paramTypes, function.returnTransform);
                    context.declareFunction(func);
                }
            }
            for (PropertyVariable v : variables.values()) {
                Variable var = new Variable(v.varName);
                if (v.value==null){
                    var.setValue(new EmptySequence());
                    continue;
                }
                Node root = v.value.getDocumentElement();
                if (root.getNamespaceURI()==null && "value".equals(root.getNodeName())) {
                    Transformer trans = trRegistry.getTransformer(String.class);
                    var.setValue(trans.transformTo(root.getTextContent(), context));
                } else {
                    Transformer trans = trRegistry.getTransformer(Node.class);
                    var.setValue(trans.transformTo(root, context));
                }
                context.declareGlobalVariable(var);
            }
            try {
                compiledXQuery = xquery.compile(context, source);
            } catch (XPathException ex) {
                throw new EXistException("Cannot compile xquery " + source.getURL(), ex);
            } catch (IOException ex) {
                throw new EXistException("I/O exception while compiling xquery " + source.getURL(), ex);
            }
            PathExpr root =(PathExpr)compiledXQuery;
            for (Map<String, ExistFunction> functions : services.values()) {
                for (ExistFunction function : functions.values()) {
                    UserDefinedFunction userFunction = context.resolveFunction(function.functionName, function.signature.getArgumentCount());
                    FunctionCall functionCall = new FunctionCall(context, userFunction);
                    functionCall.setASTNode(root.getASTNode());
                    functionCall.analyze(new AnalyzeContextInfo(root, 0));
                    function.chain.addInterceptor(new ExistXQueryTargetInterceptor(functionCall, function.paramTransformers, function.returnTransform, function.returnType, instance));
                }
            }
            if (service != null) {
                service.chain.addInterceptor(new ExistXQueryServiceTargetInterceptor(compiledXQuery, trRegistry, instance));
            }
        } finally {
            instance.releaseInstance(broker);
        }

        return compiledXQuery;

    }

    public static Class<?> loadClass(ClassLoader loader, String className) throws ClassNotFoundException {
        Class<?> clazz = PRIMITIVES_TYPES.get(className);
        if (clazz != null) {
            return clazz;
        }
        return loader.loadClass(className);


    }

    public class PropertyVariable {

        QName varName;
        Document value;
    }

    public class ExistXQService {

        InvocationChain chain;
    }

    public class ExistFunction {

        QName functionName;
        Transformer[] paramTransformers;
        Class<?>[]paramTypes;
        Transformer returnTransform;
        Class<?> returnType;
        InvocationChain chain;
        String callbackUri;
        FunctionSignature signature;
    }
}
