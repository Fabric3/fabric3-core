/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.introspection.java.contract;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.InterfaceIntrospector;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.java.contract.TypeIntrospector;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.OneWay;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Remotable;

/**
 * Default implementation of a ContractProcessor for Java interfaces.
 */
public class JavaContractProcessorImpl implements JavaContractProcessor {
    private final IntrospectionHelper helper;
    private List<InterfaceIntrospector> interfaceIntrospectors;
    private List<TypeIntrospector> typeIntrospectors;

    public JavaContractProcessorImpl(@Reference IntrospectionHelper helper) {
        this.helper = helper;
        interfaceIntrospectors = new ArrayList<>();
        typeIntrospectors = new ArrayList<>();
    }

    @Reference(required = false)
    public void setInterfaceIntrospectors(List<InterfaceIntrospector> interfaceIntrospectors) {
        this.interfaceIntrospectors = interfaceIntrospectors;
    }

    @Reference(required = false)
    public void setTypeIntrospectors(List<TypeIntrospector> typeIntrospectors) {
        this.typeIntrospectors = typeIntrospectors;
    }

    public JavaServiceContract introspect(Class<?> interfaze, IntrospectionContext context, ModelObject... modelObjects) {
        return introspect(interfaze, interfaze, context);
    }

    public JavaServiceContract introspect(Class<?> interfaze, Class<?> baseClass, IntrospectionContext context, ModelObject... modelObjects) {
        JavaServiceContract contract = introspectInterface(interfaze, baseClass, context, modelObjects);
        Callback callback = interfaze.getAnnotation(Callback.class);
        if (callback != null) {
            Class<?> callbackClass = callback.value();
            introspectCallback(interfaze, callbackClass, contract, context);
        }
        return contract;
    }

    private void introspectCallback(Class<?> interfaze,
                                    Class<?> callbackClass,
                                    JavaServiceContract contract,
                                    IntrospectionContext context,
                                    ModelObject... modelObjects) {
        if (Void.class.equals(callbackClass)) {
            context.addError(new MissingCallback(interfaze, modelObjects));
            return;
        }
        // the base class for the callback interface is always itself since it is not referenceable in Java from the service implementation
        // or client implementation where the reference is injected
        JavaServiceContract callbackContract = introspectInterface(callbackClass, callbackClass, context);
        if (contract.isRemotable() != callbackContract.isRemotable()) {
            String forwardName = contract.getInterfaceName();
            String callbackName = callbackContract.getInterfaceName();
            InvalidCallbackContract error = new InvalidCallbackContract(
                    "The remotable attribute on the forward and callback contract do not match: " + forwardName + "," + callbackName,
                    callbackClass,
                    modelObjects);
            context.addError(error);
        }
        contract.setCallbackContract(callbackContract);
    }

    private JavaServiceContract introspectInterface(Class<?> interfaze, Class<?> baseClass, IntrospectionContext context, ModelObject... modelObjects) {
        JavaServiceContract contract = new JavaServiceContract(interfaze);
        contract.setInterfaceName(interfaze.getSimpleName());

        boolean remotable = interfaze.isAnnotationPresent(org.oasisopen.sca.annotation.Remotable.class) || interfaze.isAnnotationPresent(Remotable.class);
        contract.setRemotable(remotable);

        List<Operation> operations = introspectOperations(interfaze, baseClass, remotable, context, modelObjects);
        contract.setOperations(operations);
        for (InterfaceIntrospector introspector : interfaceIntrospectors) {
            introspector.introspect(contract, interfaze, context);
        }
        return contract;
    }

    private List<Operation> introspectOperations(Class<?> interfaze,
                                                 Class<?> baseClass,
                                                 boolean remotable,
                                                 IntrospectionContext context,
                                                 ModelObject... modelObjects) {
        Method[] methods = interfaze.getMethods();
        List<Operation> operations = new ArrayList<>(methods.length);

        for (Method method : methods) {
            String name = method.getName();

            TypeMapping typeMapping = getTypeMapping(interfaze, baseClass, context);
            DataType returnType = introspectReturnType(method, typeMapping);
            List<DataType> paramTypes = introspectParameterTypes(method, typeMapping);
            List<DataType> faultTypes = introspectFaultTypes(method, typeMapping);

            Operation operation = new Operation(name, paramTypes, returnType, faultTypes);
            operation.setRemotable(remotable);

            if (method.isAnnotationPresent(OneWay.class)) {
                operation.setOneWay(true);
            }

            for (TypeIntrospector introspector : typeIntrospectors) {
                introspector.introspect(operation, method, context);
            }
            if (remotable) {
                checkOverloadedOperations(method, operations, context, modelObjects);
            }
            operations.add(operation);
        }
        return operations;
    }

    private DataType introspectReturnType(Method method, TypeMapping typeMapping) {
        Class<?> physicalReturnType = method.getReturnType();
        Type gReturnType = method.getGenericReturnType();
        Type actualReturnType = typeMapping.getActualType(gReturnType);
        return createDataType(physicalReturnType, actualReturnType, typeMapping);
    }

    private List<DataType> introspectParameterTypes(Method method, TypeMapping typeMapping) {
        Class<?>[] physicalParameterTypes = method.getParameterTypes();
        Type[] gParamTypes = method.getGenericParameterTypes();
        List<DataType> parameterDataTypes = new ArrayList<>(gParamTypes.length);
        for (int i = 0; i < gParamTypes.length; i++) {
            Type gParamType = gParamTypes[i];
            Type actualParamType = typeMapping.getActualType(gParamType);
            DataType dataType = createDataType(physicalParameterTypes[i], actualParamType, typeMapping);
            parameterDataTypes.add(dataType);
        }
        return parameterDataTypes;
    }

    private List<DataType> introspectFaultTypes(Method method, TypeMapping typeMapping) {
        Class<?>[] physicalFaultTypes = method.getExceptionTypes();
        Type[] gFaultTypes = method.getGenericExceptionTypes();  // possible even though it is not possible to catch a generic exception type
        List<DataType> faultDataTypes = new ArrayList<>(gFaultTypes.length);
        for (int i = 0; i < gFaultTypes.length; i++) {
            Type gFaultType = gFaultTypes[i];
            Type actualType = typeMapping.getActualType(gFaultType);
            DataType dataType = createDataType(physicalFaultTypes[i], actualType, typeMapping);
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
     * Returns a data type from a logical and physical type pairing.
     *
     * @param rawType the raw type
     * @param actualType    the type to construct the data type from
     * @param mapping the resolved generic type mappings
     * @return the data type
     */
    @SuppressWarnings({"unchecked"})
    private DataType createDataType(Class<?> rawType, Type actualType, TypeMapping mapping) {
        if (actualType instanceof Class) {
            // not a generic
            return new JavaType(rawType);
        } else {
            JavaTypeInfo info = helper.createTypeInfo(actualType, mapping);
            return new JavaGenericType(info);
        }
    }

    /**
     * Validates a remotable interface does not contain overloaded operations by comparing the current method to introspected operations.
     *
     * @param method       the method being introspected
     * @param operations   the interface operations
     * @param context      the introspection context
     * @param modelObjects the parent model objects
     */
    private void checkOverloadedOperations(Method method, List<Operation> operations, IntrospectionContext context, ModelObject... modelObjects) {
        for (Operation entry : operations) {
            String name = method.getName();
            if (entry.getName().equals(name)) {
                OverloadedOperation error = new OverloadedOperation(method, modelObjects);
                context.addError(error);
            }
        }
    }

}
