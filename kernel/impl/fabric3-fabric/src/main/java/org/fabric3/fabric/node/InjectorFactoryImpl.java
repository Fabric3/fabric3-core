package org.fabric3.fabric.node;

import java.lang.reflect.AccessibleObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Consumer;
import org.fabric3.api.model.type.component.Producer;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.api.node.service.InjectorFactory;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.PostProcessor;
import org.fabric3.spi.introspection.java.annotation.ClassVisitor;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 *
 */
public class InjectorFactoryImpl implements InjectorFactory {
    private ClassVisitor classVisitor;
    private ServiceResolver serviceResolver;
    private IntrospectionHelper introspectionHelper;

    private List<PostProcessor> postProcessors = Collections.emptyList();

    @org.oasisopen.sca.annotation.Reference(required = false)
    public void setPostProcessors(List<PostProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    public InjectorFactoryImpl(@org.oasisopen.sca.annotation.Reference ClassVisitor classVisitor,
                               @org.oasisopen.sca.annotation.Reference ServiceResolver serviceResolver,
                               @org.oasisopen.sca.annotation.Reference IntrospectionHelper helper) {
        this.classVisitor = classVisitor;
        this.serviceResolver = serviceResolver;
        this.introspectionHelper = helper;
    }

    public Map<AccessibleObject, Supplier<Object>> getInjectors(Class<?> clazz) {
        Map<AccessibleObject, Supplier<Object>> injectors = new HashMap<>();

        // introspect class
        InjectingComponentType componentType = new InjectingComponentType(clazz);
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        TypeMapping typeMapping = new TypeMapping();
        context.addTypeMapping(clazz, typeMapping);
        introspectionHelper.resolveTypeParameters(clazz, typeMapping);
        classVisitor.visit(componentType, clazz, context);

        for (PostProcessor postProcessor : postProcessors) {
            postProcessor.process(componentType, clazz, context);
        }

        // process injection sites
        for (Map.Entry<ModelObject, InjectionSite> entry : componentType.getInjectionSiteMappings().entrySet()) {
            ModelObject key = entry.getKey();
            InjectionSite value = entry.getValue();
            if (key instanceof Reference) {
                Reference<?> reference = (Reference) key;
                handleReference(reference, value, clazz, injectors);
            } else if (key instanceof Producer) {
                Producer producer = (Producer) key;
                throw new UnsupportedOperationException("Not yet implemented");
            } else if (key instanceof Consumer) {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        }
        return injectors;
    }

    private void handleReference(Reference<?> reference, InjectionSite site, Class<?> implClass, Map<AccessibleObject, Supplier<Object>> injectors) {
        if (reference.getBindings().isEmpty()) {
            return;
        }
        Class<?> interfaze = reference.getServiceContract().getInterfaceClass();
        Binding binding = reference.getBindings().get(0);

        AccessibleObject accessibleObject;
        if (site instanceof FieldInjectionSite) {
            accessibleObject = ((FieldInjectionSite) site).getField();
        } else if (site instanceof MethodInjectionSite) {
            accessibleObject = ((MethodInjectionSite) site).getMethod();
        } else {
            // ignore
            return;
        }
        // create supplier to resolve the proxy
        injectors.put(accessibleObject, () -> serviceResolver.resolve(interfaze, binding, implClass));
    }
}
