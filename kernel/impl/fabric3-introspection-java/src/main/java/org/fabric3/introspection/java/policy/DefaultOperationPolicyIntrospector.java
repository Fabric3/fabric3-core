package org.fabric3.introspection.java.policy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.annotation.PolicyAnnotationProcessor;
import org.fabric3.spi.introspection.java.policy.OperationPolicyIntrospector;

/**
 * Default implementation of OperationPolicyIntrospector.
 */
public class DefaultOperationPolicyIntrospector implements OperationPolicyIntrospector {
    private PolicyAnnotationProcessor policyProcessor;

    public DefaultOperationPolicyIntrospector(@Reference PolicyAnnotationProcessor policyProcessor) {
        this.policyProcessor = policyProcessor;
    }

    public void introspectPolicyOnOperations(ServiceContract contract, Class<?> implClass, IntrospectionContext context) {
        for (Operation operation : contract.getOperations()) {
            // determine the operation signature and look up the corresponding method on the implementation class
            List<DataType> types = operation.getInputTypes();
            Class<?>[] params = new Class<?>[types.size()];
            for (int i = 0; i < types.size(); i++) {
                DataType dataType = types.get(i);
                Class<?> type = dataType.getType();
                params[i] = type;
            }
            try {
                Method method = implClass.getMethod(operation.getName(), params);
                for (Annotation annotation : method.getAnnotations()) {
                    policyProcessor.process(annotation, operation, context);
                }
            } catch (NoSuchMethodException e) {
                // should not happen
                throw new AssertionError(e);
            }
        }
    }

}
