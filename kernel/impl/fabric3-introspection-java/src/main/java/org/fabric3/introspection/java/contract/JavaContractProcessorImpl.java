/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.java.contract;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.jws.WebMethod;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Conversational;
import org.osoa.sca.annotations.EndsConversation;
import org.osoa.sca.annotations.OneWay;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Remotable;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
import static org.fabric3.model.type.contract.Operation.CONVERSATION_END;
import static org.fabric3.model.type.contract.Operation.NO_CONVERSATION;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.InterfaceIntrospector;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.java.contract.OperationIntrospector;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.type.java.JavaTypeInfo;

/**
 * Default implementation of a ContractProcessor for Java interfaces.
 *
 * @version $Rev$ $Date$
 */
public class JavaContractProcessorImpl implements JavaContractProcessor {
    public static final String IDL_INPUT = "idl:input";
    public static final QName ONEWAY_INTENT = new QName(Constants.SCA_NS, "oneWay");

    private final IntrospectionHelper helper;
    private List<InterfaceIntrospector> interfaceIntrospectors;
    private List<OperationIntrospector> operationIntrospectors;

    public JavaContractProcessorImpl(@Reference IntrospectionHelper helper) {
        this.helper = helper;
        interfaceIntrospectors = new ArrayList<InterfaceIntrospector>();
        operationIntrospectors = new ArrayList<OperationIntrospector>();
    }

    @Reference(required = false)
    public void setInterfaceIntrospectors(List<InterfaceIntrospector> interfaceIntrospectors) {
        this.interfaceIntrospectors = interfaceIntrospectors;
    }

    @Reference(required = false)
    public void setOperationIntrospectors(List<OperationIntrospector> operationIntrospectors) {
        this.operationIntrospectors = operationIntrospectors;
    }

    public JavaServiceContract introspect(Class<?> interfaze, IntrospectionContext context) {
        return introspect(interfaze, interfaze, context);
    }

    public JavaServiceContract introspect(Class<?> interfaze, Class<?> baseClass, IntrospectionContext context) {
        JavaServiceContract contract = introspectInterface(interfaze, baseClass, context);
        Callback callback = interfaze.getAnnotation(Callback.class);
        if (callback != null) {
            Class<?> callbackClass = callback.value();
            introspectCallback(interfaze, callbackClass, contract, context);
        } else {
            org.oasisopen.sca.annotation.Callback oasisCallback = interfaze.getAnnotation(org.oasisopen.sca.annotation.Callback.class);
            if (oasisCallback != null) {
                Class<?> callbackClass = oasisCallback.value();
                introspectCallback(interfaze, callbackClass, contract, context);
            }
        }
        return contract;
    }

    private void introspectCallback(Class<?> interfaze, Class<?> callbackClass, JavaServiceContract contract, IntrospectionContext context) {
        if (Void.class.equals(callbackClass)) {
            context.addError(new MissingCallback(interfaze));
            return;
        }
        // the base class for the callback interface is always itself since it is not referenceable in Java from the service implementation
        // or client implementation where the reference is injected
        JavaServiceContract callbackContract = introspectInterface(callbackClass, callbackClass, context);
        contract.setCallbackContract(callbackContract);
    }

    private JavaServiceContract introspectInterface(Class<?> interfaze, Class<?> baseClass, IntrospectionContext context) {
        JavaServiceContract contract = new JavaServiceContract(interfaze);
        contract.setInterfaceName(interfaze.getSimpleName());

        boolean remotable =
                interfaze.isAnnotationPresent(org.oasisopen.sca.annotation.Remotable.class) || interfaze.isAnnotationPresent(Remotable.class);
        contract.setRemotable(remotable);

        boolean conversational = helper.isAnnotationPresent(interfaze, Conversational.class);
        contract.setConversational(conversational);

        List<Operation> operations = introspectOperations(interfaze, baseClass, remotable, conversational, context);
        contract.setOperations(operations);
        for (InterfaceIntrospector introspector : interfaceIntrospectors) {
            introspector.introspect(contract, interfaze, context);
        }
        return contract;
    }

    private List<Operation> introspectOperations(Class<?> interfaze,
                                                 Class<?> baseClass,
                                                 boolean remotable,
                                                 boolean conversational,
                                                 IntrospectionContext context) {
        Method[] methods = interfaze.getMethods();
        List<Operation> operations = new ArrayList<Operation>(methods.length);

        for (Method method : methods) {
            String name = method.getName();

            TypeMapping typeMapping = getTypeMapping(interfaze, baseClass, context);
            DataType<?> returnType = introspectReturnType(method, typeMapping);
            List<DataType<?>> paramTypes = introspectParameterTypes(method, typeMapping);
            List<DataType<?>> faultTypes = introspectFaultTypes(method, typeMapping);

            int conversationSequence = introspectConversationSequence(conversational, method, context);

            Operation operation = new Operation(name, paramTypes, returnType, faultTypes, conversationSequence);
            operation.setRemotable(remotable);

            if (method.isAnnotationPresent(org.oasisopen.sca.annotation.OneWay.class) || method.isAnnotationPresent(OneWay.class)) {
                operation.addIntent(ONEWAY_INTENT);
            }

            WebMethod webMethod = method.getAnnotation(WebMethod.class);
            if (webMethod != null && webMethod.operationName().length() > 0) {
                operation.setWsdlName(webMethod.operationName());
            }

            for (OperationIntrospector introspector : operationIntrospectors) {
                introspector.introspect(operation, method, context);
            }
            if (remotable) {
                checkOverloadedOperations(method, operations, context);
            }
            operations.add(operation);
        }
        return operations;
    }

    private DataType<?> introspectReturnType(Method method, TypeMapping typeMapping) {
        Class<?> physicalReturnType = method.getReturnType();
        Type gReturnType = method.getGenericReturnType();
        Type logicalReturnType = typeMapping.getActualType(gReturnType);
        return createDataType(physicalReturnType, logicalReturnType, typeMapping);
    }

    private List<DataType<?>> introspectParameterTypes(Method method, TypeMapping typeMapping) {
        Class<?>[] physicalParameterTypes = method.getParameterTypes();
        Type[] gParamTypes = method.getGenericParameterTypes();
        List<DataType<?>> parameterDataTypes = new ArrayList<DataType<?>>(gParamTypes.length);
        for (int i = 0; i < gParamTypes.length; i++) {
            Type gParamType = gParamTypes[i];
            Type logicalParamType = typeMapping.getActualType(gParamType);
            DataType<?> dataType = createDataType(physicalParameterTypes[i], logicalParamType, typeMapping);
            parameterDataTypes.add(dataType);
        }
        return parameterDataTypes;
    }

    private List<DataType<?>> introspectFaultTypes(Method method, TypeMapping typeMapping) {
        Class<?>[] physicalFaultTypes = method.getExceptionTypes();
        Type[] gFaultTypes = method.getGenericExceptionTypes();  // possible even though it is not possible to catch a generic exception type
        List<DataType<?>> faultDataTypes = new ArrayList<DataType<?>>(gFaultTypes.length);
        for (int i = 0; i < gFaultTypes.length; i++) {
            Type gFaultType = gFaultTypes[i];
            Type logicalType = typeMapping.getActualType(gFaultType);
            DataType<?> dataType = createDataType(physicalFaultTypes[i], logicalType, typeMapping);
            faultDataTypes.add(dataType);
        }
        return faultDataTypes;
    }

    private TypeMapping getTypeMapping(Class<?> type, Class<?> baseClass, IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(baseClass);
        if (typeMapping == null) {
            typeMapping = new TypeMapping();
            context.addTypeMapping(type, typeMapping);
            helper.resolveTypeParameters(type, typeMapping);
        }
        return typeMapping;
    }

    /**
     * Determines the conversational sequence of a conversational service.
     *
     * @param conversational true if the service is conversaional
     * @param method         the method being introspected
     * @param context        the introspection context 
     * @return the conversational sequence
     */
    private int introspectConversationSequence(boolean conversational, Method method, IntrospectionContext context) {
        int conversationSequence = NO_CONVERSATION;
        if (method.isAnnotationPresent(EndsConversation.class)) {
            if (!conversational) {
                context.addError(new InvalidConversationalOperation(method));
            }
            conversationSequence = CONVERSATION_END;
        } else if (conversational) {
            conversationSequence = Operation.CONVERSATION_CONTINUE;
        }
        return conversationSequence;
    }

    /**
     * Returns a data type from a logical and physical type pairing.
     *
     * @param physicalType the physical type
     * @param type         the type to construct the data type from
     * @param mapping      the resolved generic type mappings
     * @return the data type
     */
    @SuppressWarnings({"unchecked"})
    private DataType<?> createDataType(Class<?> physicalType, Type type, TypeMapping mapping) {
        if (type instanceof Class) {
            // not a generic
            return new JavaClass(physicalType);
        } else {
            JavaTypeInfo info = helper.createTypeInfo(type, mapping);
            return new JavaGenericType(info);
        }
    }

    /**
     * Validates a remotable interface does not contain overloaded operations by comparing the current method to introspected operations.
     *
     * @param method     the method being introspected
     * @param operations the interface operations
     * @param context    the introspection context
     */
    private void checkOverloadedOperations(Method method, List<Operation> operations, IntrospectionContext context) {
        for (Operation entry : operations) {
            String name = method.getName();
            if (entry.getName().equals(name)) {
                OverloadedOperation error = new OverloadedOperation(name);
                context.addError(error);
            }
        }
    }


}
