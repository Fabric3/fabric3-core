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
package org.fabric3.implementation.pojo.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.implementation.pojo.supplier.ArrayMultiplicitySupplier;
import org.fabric3.implementation.pojo.supplier.ListMultiplicitySupplier;
import org.fabric3.implementation.pojo.supplier.MapMultiplicitySupplier;
import org.fabric3.implementation.pojo.supplier.MultiplicitySupplier;
import org.fabric3.implementation.pojo.supplier.SetMultiplicitySupplier;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.spi.container.injection.InjectionAttributes;
import org.fabric3.spi.container.injection.Injector;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 *
 */
public class ImplementationManagerFactoryImpl implements ImplementationManagerFactory {
    private static final Supplier<?> NULL_FACTORY = () -> null;

    private final URI componentUri;
    private final Class<?> implementationClass;
    private final Constructor<?> constructor;
    private final List<Injectable> cdiSources;
    private final Map<InjectionSite, Injectable> postConstruction;
    private final LifecycleInvoker initInvoker;
    private final LifecycleInvoker destroyInvoker;
    private final boolean reinjectable;
    private final ClassLoader cl;
    private ReflectionFactory reflectionFactory;

    private final Map<Injectable, Supplier<?>> factories;

    public ImplementationManagerFactoryImpl(URI componentUri,
                                            Constructor<?> constructor,
                                            List<Injectable> cdiSources,
                                            Map<InjectionSite, Injectable> postConstruction,
                                            LifecycleInvoker initInvoker,
                                            LifecycleInvoker destroyInvoker,
                                            boolean reinjectable,
                                            ClassLoader cl,
                                            ReflectionFactory reflectionFactory) {
        this.componentUri = componentUri;
        this.reflectionFactory = reflectionFactory;
        this.implementationClass = constructor.getDeclaringClass();
        this.constructor = constructor;
        this.cdiSources = cdiSources;
        this.postConstruction = postConstruction;
        this.initInvoker = initInvoker;
        this.destroyInvoker = destroyInvoker;
        this.reinjectable = reinjectable;
        this.cl = cl;
        factories = new HashMap<>();

    }

    @SuppressWarnings({"unchecked"})
    public ImplementationManager createManager() {
        Supplier<?> factory = reflectionFactory.createInstantiator(constructor, getConstructorParameterSuppliers(cdiSources));
        Map<Injectable, Injector<?>> mappings = createInjectorMappings();

        Injectable[] attributes = mappings.keySet().toArray(new Injectable[mappings.size()]);
        Injector<Object>[] injectors = mappings.values().toArray(new Injector[mappings.size()]);

        return new ImplementationManagerImpl(componentUri, factory, attributes, injectors, initInvoker, destroyInvoker, reinjectable, cl);
    }

    public Class<?> getImplementationClass() {
        return implementationClass;
    }

    public void startUpdate() {
        for (Map.Entry<Injectable, Supplier<?>> entry : factories.entrySet()) {
            // signal to multiplicity factories that previous contents should be overwritten if the factory is updated (e.g. during reinjection)
            Injectable injectable = entry.getKey();
            Supplier<?> factory = entry.getValue();
            if (InjectableType.REFERENCE == injectable.getType() || InjectableType.CALLBACK == injectable.getType()) {
                if (factory instanceof MultiplicitySupplier) {
                    MultiplicitySupplier<?> multiplicitySupplier = (MultiplicitySupplier<?>) factory;
                    multiplicitySupplier.startUpdate();
                }
            }
        }
    }

    public void endUpdate() {
        for (Map.Entry<Injectable, Supplier<?>> entry : factories.entrySet()) {
            // signal to multiplicity factories updates are complete
            Injectable injectable = entry.getKey();
            Supplier<?> factory = entry.getValue();
            if (InjectableType.REFERENCE == injectable.getType() || InjectableType.CALLBACK == injectable.getType()) {
                if (factory instanceof MultiplicitySupplier) {
                    MultiplicitySupplier<?> multiplicitySupplier = (MultiplicitySupplier<?>) factory;
                    multiplicitySupplier.endUpdate();
                }
            }
        }
    }

    public void setSupplier(Injectable injectable, Supplier<?> supplier) {
        setSupplier(injectable, supplier, InjectionAttributes.EMPTY_ATTRIBUTES);
    }

    public void setSupplier(Injectable injectable, Supplier<?> supplier, InjectionAttributes attributes) {
        if (InjectableType.REFERENCE == injectable.getType() || InjectableType.CALLBACK == injectable.getType()) {
            setUpdatableFactory(injectable, supplier, attributes);
        } else {
            // the factory corresponds to a property or context, which will override previous values if re-injected
            factories.put(injectable, supplier);
        }
    }

    public Supplier<?> getObjectSupplier(Injectable injectable) {
        return factories.get(injectable);
    }

    public void removeSupplier(Injectable injectable) {
        factories.remove(injectable);
    }

    public Class<?> getMemberType(Injectable injectable) {
        InjectionSite site = findInjectionSite(injectable);
        if (site == null) {
            throw new AssertionError("No injection site for " + injectable + " in " + implementationClass);
        }
        if (site instanceof FieldInjectionSite) {
            try {
                FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                Field field = getField(fieldSite.getName());
                return field.getType();
            } catch (NoSuchFieldException e) {
                throw new AssertionError(e);
            }
        } else if (site instanceof MethodInjectionSite) {
            MethodInjectionSite methodSite = (MethodInjectionSite) site;
            Method method = methodSite.getMethod();
            return method.getParameterTypes()[methodSite.getParam()];
        } else if (site instanceof ConstructorInjectionSite) {
            ConstructorInjectionSite methodSite = (ConstructorInjectionSite) site;
            Constructor<?> method = methodSite.getConstructor();
            return method.getParameterTypes()[methodSite.getParam()];
        } else {
            throw new AssertionError("Invalid injection site type: " + site.getClass());
        }
    }

    public Type getGenericType(Injectable injectable) {
        InjectionSite site = findInjectionSite(injectable);
        if (site == null) {
            throw new AssertionError("No injection site for " + injectable + " in " + implementationClass);
        }
        if (site instanceof FieldInjectionSite) {
            try {
                FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                Field field = getField(fieldSite.getName());
                return field.getGenericType();
            } catch (NoSuchFieldException e) {
                throw new AssertionError(e);
            }
        } else if (site instanceof MethodInjectionSite) {
            MethodInjectionSite methodSite = (MethodInjectionSite) site;
            Method method = methodSite.getMethod();
            return method.getGenericParameterTypes()[methodSite.getParam()];
        } else if (site instanceof ConstructorInjectionSite) {
            ConstructorInjectionSite methodSite = (ConstructorInjectionSite) site;
            Constructor<?> method = methodSite.getConstructor();
            return method.getGenericParameterTypes()[methodSite.getParam()];
        } else {
            throw new AssertionError("Invalid injection site type " + site.getClass());
        }
    }

    /**
     * Returns an ordered array of Suppliers for the parameters of the constructor used to instantiate an instance.
     *
     * @param sources the ordered list of InjectableAttributes corresponding to the constructor parameter
     * @return the object Suppliers for the constructor
     */
    protected Supplier<?>[] getConstructorParameterSuppliers(List<Injectable> sources) {
        Supplier<?>[] argumentFactories = new Supplier<?>[sources.size()];
        for (int i = 0; i < argumentFactories.length; i++) {
            Injectable source = sources.get(i);
            Supplier<?> factory = factories.get(source);
            if (factory == null) {
                factory = NULL_FACTORY;
            }
            argumentFactories[i] = factory;
        }
        return argumentFactories;
    }

    /**
     * Returns a map of injectors for all post-construction (i.e. field and method) sites. The injectors inject reference proxies, properties, callback proxies,
     * and context objects on an instance when it is initialized.
     *
     * @return a map of injectors keyed by InjectableAttribute.
     */
    protected Map<Injectable, Injector<?>> createInjectorMappings() {
        Map<Injectable, Injector<?>> injectors = new LinkedHashMap<>(postConstruction.size());
        for (Map.Entry<InjectionSite, Injectable> entry : postConstruction.entrySet()) {
            InjectionSite site = entry.getKey();
            Injectable attribute = entry.getValue();
            InjectableType type = attribute.getType();
            Supplier<?> supplier = factories.get(attribute);
            if (supplier == null && (type == InjectableType.REFERENCE || type == InjectableType.CALLBACK)) {
                // The reference or callback is not configured, i.e. wired. Set an empty, updateable Supplier as it may be wired later.
                supplier = createSupplier(site.getType());
                factories.put(attribute, supplier);
            }
            if (supplier != null) {
                if (site instanceof FieldInjectionSite) {

                    try {
                        FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                        Field field = getField(fieldSite.getName());
                        Injector<?> injector = reflectionFactory.createInjector(field, supplier);
                        injectors.put(attribute, injector);
                    } catch (NoSuchFieldException e) {
                        throw new AssertionError(e);
                    }
                } else if (site instanceof MethodInjectionSite) {
                    MethodInjectionSite methodSite = (MethodInjectionSite) site;
                    Method method = methodSite.getMethod();
                    Injector<?> injector = reflectionFactory.createInjector(method, supplier);
                    injectors.put(attribute, injector);
                }
            }
        }
        return injectors;
    }

    private void setUpdatableFactory(Injectable injectable, Supplier<?> supplier, InjectionAttributes attributes) {
        // determine if Supplier is present. if so, must be updated.
        Supplier<?> factory = factories.get(injectable);
        if (factory == null) {
            // factory not present, add it first checking ot see if it is a collection type and, if so, wrapping it in a collection-based factory
            Class<?> type = getMemberType(injectable);
            if (Map.class.equals(type)) {
                MapMultiplicitySupplier mapFactory = new MapMultiplicitySupplier();
                mapFactory.startUpdate();
                mapFactory.addSupplier(supplier, attributes);
                factories.put(injectable, mapFactory);
            } else if (Set.class.equals(type)) {
                SetMultiplicitySupplier setFactory = new SetMultiplicitySupplier();
                setFactory.startUpdate();
                setFactory.addSupplier(supplier, attributes);
                factories.put(injectable, setFactory);
            } else if (List.class.equals(type)) {
                ListMultiplicitySupplier listFactory = new ListMultiplicitySupplier();
                listFactory.startUpdate();
                listFactory.addSupplier(supplier, attributes);
                factories.put(injectable, listFactory);
            } else if (Collection.class.equals(type)) {
                ListMultiplicitySupplier listFactory = new ListMultiplicitySupplier();
                listFactory.startUpdate();
                listFactory.addSupplier(supplier, attributes);
                factories.put(injectable, listFactory);
            } else if (type.isArray()) {
                ArrayMultiplicitySupplier arrayFactory = new ArrayMultiplicitySupplier(type.getComponentType());
                arrayFactory.startUpdate();
                arrayFactory.addSupplier(supplier, attributes);
                factories.put(injectable, arrayFactory);
            } else {
                // not a collection type, add the factory
                factories.put(injectable, supplier);
            }
        } else if (factory instanceof MultiplicitySupplier) {
            MultiplicitySupplier<?> multiplicitySupplier = (MultiplicitySupplier<?>) factory;
            multiplicitySupplier.addSupplier(supplier, attributes);
        } else {
            // overwrite the existing factory with a new one
            factories.put(injectable, supplier);
        }
    }

    // FIXME this is a hack until can replace getMemberType/getGenericType as they assume a single injection site
    private InjectionSite findInjectionSite(Injectable attribute) {
        // try constructor
        for (int i = 0; i < cdiSources.size(); i++) {
            Injectable injectable = cdiSources.get(i);
            if (attribute.equals(injectable)) {
                return new ConstructorInjectionSite(constructor, i);
            }
        }
        // try postConstruction
        for (Map.Entry<InjectionSite, Injectable> entry : postConstruction.entrySet()) {
            if (entry.getValue().equals(attribute)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Field getField(String name) throws NoSuchFieldException {
        Class<?> clazz = implementationClass;
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private Supplier<?> createSupplier(Class<?> referenceType) {
        if (Map.class.equals(referenceType)) {
            return new MapMultiplicitySupplier();
        } else if (Set.class.equals(referenceType)) {
            return new SetMultiplicitySupplier();
        } else if (List.class.equals(referenceType)) {
            return new ListMultiplicitySupplier();
        } else if (Collection.class.equals(referenceType)) {
            return new ListMultiplicitySupplier();
        } else {
            return NULL_FACTORY;
        }
    }
}
