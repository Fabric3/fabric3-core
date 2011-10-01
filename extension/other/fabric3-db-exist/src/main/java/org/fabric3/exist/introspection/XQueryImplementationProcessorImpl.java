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

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import antlr.collections.AST;
import org.exist.xquery.ExternalModule;
import org.exist.xquery.PathExpr;
import org.exist.xquery.parser.XQueryLexer;
import org.exist.xquery.parser.XQueryParser;
import org.exist.xquery.parser.XQueryTreeParser;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.model.type.component.InvalidImplementation;
import org.fabric3.model.type.component.MissingResource;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.xquery.introspection.XQueryImplementationProcessor;
import org.fabric3.xquery.scdl.XQueryComponentType;
import org.fabric3.xquery.scdl.XQueryImplementation;
import org.fabric3.xquery.scdl.XQueryServiceContract;

/**
 * Default implementation of WebImplementationIntrospector.
 *
 * @version $Rev$ $Date$
 */
public class XQueryImplementationProcessorImpl implements XQueryImplementationProcessor {

    private IntrospectionHelper helper;

    public XQueryImplementationProcessorImpl(@Reference(name = "helper") IntrospectionHelper helper) {
        this.helper = helper;
    }

    public void introspect(XQueryImplementation implementation, IntrospectionContext context) {
        XQueryComponentType componentType = new XQueryComponentType();
        componentType.setScope("STATELESS");
        implementation.setComponentType(componentType);
        String location = implementation.getLocation();
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // Set TCCL to the extension classloader as implementations may need access to exist classes. Also, Groovy
            // dependencies such as Antlr use the TCCL.
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            URL locationURL = context.getTargetClassLoader().getResource(location);
            if (locationURL == null) {
                context.addError(new MissingResource("XQuery file not found: ", implementation.getLocation()));
                return;
            }
            try {
                IntrospectingXQueryContext ctx = new IntrospectingXQueryContext();
                XQueryLexer lexer = new XQueryLexer(locationURL.openStream());
                XQueryParser xparser = new XQueryParser(lexer);
                xparser.xpath();
                if (xparser.foundErrors()) {
                    context.addError(new InvalidImplementation("XQuery parse error: ", xparser.getErrorMessage()));
                    return;
                }
                XQueryTreeParser treeParser = new XQueryTreeParser(ctx);
                AST ast = xparser.getAST();
                PathExpr expr = new PathExpr(ctx);
                treeParser.xpath(ast, expr);
                if (treeParser.foundErrors()) {
                    context.addError(new InvalidImplementation("XQuery parse error: ", treeParser.getErrorMessage()));
                    return;
                }
                //expr.analyze(new AnalyzeContextInfo());
                //Sequence result = expr.eval(null, null);
                ctx.process(implementation, context);
                ExternalModule module = treeParser.getModule();
                if (module != null) {//set the implementation as a module, i.e. a collection of Xquery functions
                    implementation.setIsModule(true);
                    implementation.setModuleNameSpace(new javax.xml.namespace.QName(module.getNamespaceURI(), module.getDefaultPrefix()));
                } else {
                    addXQueryService(implementation);
                }
            } catch (Exception ie) {
                ie.printStackTrace();
                context.addError(new InvalidImplementation("XQuery parse error: ", ie.getMessage()));
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    protected void addXQueryService(XQueryImplementation impl) {
        XQueryServiceContract contract = new XQueryServiceContract();
        ServiceDefinition service = new ServiceDefinition("XQueryService", contract);
        DataType<Type> returnDataType = new DataType<Type>(Object.class, Object.class);
        List<DataType<Type>> paramDataTypes = new ArrayList<DataType<Type>>(2);
        DataType<List<DataType<Type>>> inputType = new DataType<List<DataType<Type>>>(Object[].class, paramDataTypes);
        paramDataTypes.add(new DataType<Type>(Map.class, Map.class));
        paramDataTypes.add(new DataType<Type>(Class.class, Class.class));
        Operation<Type> operation = new Operation<Type>("evaluate", inputType, returnDataType, new ArrayList<DataType<Type>>());
        List<Operation<Type>> operations = new ArrayList<Operation<Type>>();
        operations.add(operation);
        contract.setOperations(operations);
        impl.getComponentType().add(service);
    }
}