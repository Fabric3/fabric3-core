/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.implementation.pojo.reflection;

import org.fabric3.implementation.pojo.objectfactory.ArrayMultiplicityObjectFactory;
import org.fabric3.implementation.pojo.objectfactory.ListMultiplicityObjectFactory;
import org.fabric3.implementation.pojo.objectfactory.MapMultiplicityObjectFactory;
import org.fabric3.implementation.pojo.objectfactory.MultiplicityObjectFactory;
import org.fabric3.implementation.pojo.objectfactory.SetMultiplicityObjectFactory;
import org.fabric3.implementation.pojo.manager.ImplementationManager;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.implementation.pojo.manager.ImplementationManagerImpl;
import org.fabric3.implementation.pojo.spi.invocation.LifecycleInvoker;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectableType;
import org.fabric3.spi.model.type.java.InjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.fabric3.spi.objectfactory.InjectionAttributes;
import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectFactory;

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

/**
 *
 */
public class ReflectiveImplementationManagerFactory implements ImplementationManagerFactory {
    private static final ObjectFactory<?> NULL_FACTORY = new ObjectFactory<Object>() {
        public Object getInstance() {
            return null;
        }
    };

    private final URI componentUri;
    private final Class<?> implementationClass;
    private final Constructor<?> constructor;
    private final List<Injectable> cdiSources;
    private final Map<InjectionSite, Injectable> postConstruction;
    private final LifecycleInvoker initInvoker;
    private final LifecycleInvoker destroyInvoker;
    private final Map<Injectable, ObjectFactory<?>> factories = new HashMap<Injectable, ObjectFactory<?>>();
    private final ClassLoader cl;
    private final boolean reinjectable;

    public ReflectiveImplementationManagerFactory(URI componentUri,
                                                  Constructor<?> constructor,
                                                  List<Injectable> cdiSources,
                                                  Map<InjectionSite, Injectable> postConstruction,
                                                  Method initMethod,
                                                  Method destroyMethod,
                                                  boolean reinjectable,
                                                  ClassLoader cl) {
        this.componentUri = componentUri;
        this.implementationClass = constructor.getDeclaringClass();
        this.constructor = constructor;
        this.cdiSources = cdiSources;
        this.postConstruction = postConstruction;
        this.initInvoker = initMethod == null ? null : new MethodLifecycleInvoker(initMethod);
        this.destroyInvoker = destroyMethod == null ? null : new MethodLifecycleInvoker(destroyMethod);
        this.reinjectable = reinjectable;
        this.cl = cl;

    }

    @SuppressWarnings({"unchecked"})
    public ImplementationManager createManager() {
        ObjectFactory<?> factory = new ReflectiveObjectFactory(constructor, getConstructorParameterFactories(cdiSources));
        Map<Injectable, Injector<Object>> mappings = createInjectorMappings();

        Injectable[] attributes = mappings.keySet().toArray(new Injectable[mappings.size()]);
        Injector<Object>[] injectors = mappings.values().toArray(new Injector[mappings.size()]);

        return new ImplementationManagerImpl(componentUri, factory, attributes, injectors, initInvoker, destroyInvoker, reinjectable, cl);
    }

    public Class<?> getImplementationClass() {
        return implementationClass;
    }

    public void startUpdate() {
        for (Map.Entry<Injectable, ObjectFactory<?>> entry : factories.entrySet()) {
            // signal to multiplicity factories that previous contents should be overwritten if the factory is updated (e.g. during reinjection)
            Injectable injectable = entry.getKey();
            ObjectFactory<?> factory = entry.getValue();
            if (InjectableType.REFERENCE == injectable.getType() || InjectableType.CALLBACK == injectable.getType()) {
                if (factory instanceof MultiplicityObjectFactory) {
                    MultiplicityObjectFactory<?> multiplicityObjectFactory = (MultiplicityObjectFactory<?>) factory;
                    multiplicityObjectFactory.startUpdate();
                }
            }
        }
    }

    public void endUpdate() {
        for (Map.Entry<Injectable, ObjectFactory<?>> entry : factories.entrySet()) {
            // signal to multiplicity factories updates are complete
            Injectable injectable = entry.getKey();
            ObjectFactory<?> factory = entry.getValue();
            if (InjectableType.REFERENCE == injectable.getType() || InjectableType.CALLBACK == injectable.getType()) {
                if (factory instanceof MultiplicityObjectFactory) {
                    MultiplicityObjectFactory<?> multiplicityObjectFactory = (MultiplicityObjectFactory<?>) factory;
                    multiplicityObjectFactory.endUpdate();
                }
            }
        }
    }

    public void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory) {
        setObjectFactory(injectable, objectFactory, InjectionAttributes.EMPTY_ATTRIBUTES);
    }

    public void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory, InjectionAttributes attributes) {
        if (InjectableType.REFERENCE == injectable.getType() || InjectableType.CALLBACK == injectable.getType()) {
            setUpdateableFactory(injectable, objectFactory, attributes);
        } else {
            // the factory corresponds to a property or context, which will override previous values if reinjected
            factories.put(injectable, objectFactory);
        }
    }

    public ObjectFactory<?> getObjectFactory(Injectable injectable) {
        return factories.get(injectable);
    }

    public void removeObjectFactory(Injectable injectable) {
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

            try {
                MethodInjectionSite methodSite = (MethodInjectionSite) site;
                Method method = methodSite.getSignature().getMethod(implementationClass);
                return method.getParameterTypes()[methodSite.getParam()];
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        } else if (site instanceof ConstructorInjectionSite) {
            try {
                ConstructorInjectionSite methodSite = (ConstructorInjectionSite) site;
                Constructor<?> method = methodSite.getSignature().getConstructor(implementationClass);
                return method.getParameterTypes()[methodSite.getParam()];
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
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
            try {
                MethodInjectionSite methodSite = (MethodInjectionSite) site;
                Method method = methodSite.getSignature().getMethod(implementationClass);
                return method.getGenericParameterTypes()[methodSite.getParam()];
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        } else if (site instanceof ConstructorInjectionSite) {
            try {
                ConstructorInjectionSite methodSite = (ConstructorInjectionSite) site;
                Constructor<?> method = methodSite.getSignature().getConstructor(implementationClass);
                return method.getGenericParameterTypes()[methodSite.getParam()];
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        } else {
            throw new AssertionError("Invalid injection site type " + site.getClass());
        }
    }

    /**
     * Returns an ordered array of object factories for the parameters of the constructor used to instantiate an instance.
     *
     * @param sources the ordered list of InjectableAttributes corresponding to the constructor parameter
     * @return the object factories for the constructor
     */
    protected ObjectFactory<?>[] getConstructorParameterFactories(List<Injectable> sources) {
        ObjectFactory<?>[] argumentFactories = new ObjectFactory<?>[sources.size()];
        for (int i = 0; i < argumentFactories.length; i++) {
            Injectable source = sources.get(i);
            ObjectFactory<?> factory = factories.get(source);
            if (factory == null) {
                factory = NULL_FACTORY;
            }
            argumentFactories[i] = factory;
        }
        return argumentFactories;
    }

    /**
     * Returns a map of injectors for all post-construction (i.e. field and method) sites. The injectors inject reference proxies, properties,
     * callback proxies, and context objects on an instance when it is initialized.
     *
     * @return a map of injectors keyed by InjectableAttribute.
     */
    protected Map<Injectable, Injector<Object>> createInjectorMappings() {
        Map<Injectable, Injector<Object>> injectors = new LinkedHashMap<Injectable, Injector<Object>>(postConstruction.size());
        for (Map.Entry<InjectionSite, Injectable> entry : postConstruction.entrySet()) {
            InjectionSite site = entry.getKey();
            Injectable attribute = entry.getValue();
            InjectableType type = attribute.getType();
            ObjectFactory<?> factory = factories.get(attribute);
            if (factory == null && (type == InjectableType.REFERENCE || type == InjectableType.CALLBACK)) {
                // The reference or callback is not configured, i.e. wired. Set an empty, updateable ObjectFactory as it may be wired later.
                factory = createObjectFactory(site.getType());
                factories.put(attribute, factory);
            }
            if (factory != null) {
                if (site instanceof FieldInjectionSite) {

                    try {
                        FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                        Field field = getField(fieldSite.getName());
                        injectors.put(attribute, new FieldInjector(field, factory));
                    } catch (NoSuchFieldException e) {
                        throw new AssertionError(e);
                    }
                } else if (site instanceof MethodInjectionSite) {
                    try {
                        MethodInjectionSite methodSite = (MethodInjectionSite) site;
                        Method method = methodSite.getSignature().getMethod(implementationClass);
                        injectors.put(attribute, new MethodInjector(method, factory));
                    } catch (ClassNotFoundException e) {
                        throw new AssertionError(e);
                    } catch (NoSuchMethodException e) {
                        throw new AssertionError(e);
                    }
                }
            }
        }
        return injectors;
    }

    private void setUpdateableFactory(Injectable injectable, ObjectFactory<?> objectFactory, InjectionAttributes attributes) {
        // determine if object factory is present. if so, must be updated.
        ObjectFactory<?> factory = factories.get(injectable);
        if (factory == null) {
            // factory not present, add it first checking ot see if it is a collection type and, if so, wrapping it in a collection-based factory
            Class<?> type = getMemberType(injectable);
            if (Map.class.equals(type)) {
                MapMultiplicityObjectFactory mapFactory = new MapMultiplicityObjectFactory();
                mapFactory.startUpdate();
                mapFactory.addObjectFactory(objectFactory, attributes);
                factories.put(injectable, mapFactory);
            } else if (Set.class.equals(type)) {
                SetMultiplicityObjectFactory setFactory = new SetMultiplicityObjectFactory();
                setFactory.startUpdate();
                setFactory.addObjectFactory(objectFactory, attributes);
                factories.put(injectable, setFactory);
            } else if (List.class.equals(type)) {
                ListMultiplicityObjectFactory listFactory = new ListMultiplicityObjectFactory();
                listFactory.startUpdate();
                listFactory.addObjectFactory(objectFactory, attributes);
                factories.put(injectable, listFactory);
            } else if (Collection.class.equals(type)) {
                ListMultiplicityObjectFactory listFactory = new ListMultiplicityObjectFactory();
                listFactory.startUpdate();
                listFactory.addObjectFactory(objectFactory, attributes);
                factories.put(injectable, listFactory);
            } else if (type.isArray()) {
                ArrayMultiplicityObjectFactory arrayFactory = new ArrayMultiplicityObjectFactory(type.getComponentType());
                arrayFactory.startUpdate();
                arrayFactory.addObjectFactory(objectFactory, attributes);
                factories.put(injectable, arrayFactory);
            } else {
                // not a collection type, add the factory
                factories.put(injectable, objectFactory);
            }
        } else if (factory instanceof MultiplicityObjectFactory) {
            MultiplicityObjectFactory<?> multiplicityObjectFactory = (MultiplicityObjectFactory<?>) factory;
            multiplicityObjectFactory.addObjectFactory(objectFactory, attributes);
        } else {
            // overwrite the existing factory with a new one
            factories.put(injectable, objectFactory);
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

    private ObjectFactory<?> createObjectFactory(String referenceType) {
        if ("java.util.Map".equals(referenceType)) {
            return new MapMultiplicityObjectFactory();
        } else if ("java.util.Set".equals(referenceType)) {
            return new SetMultiplicityObjectFactory();
        } else if ("java.util.List".equals(referenceType)) {
            return new ListMultiplicityObjectFactory();
        } else if ("java.util.Collection".equals(referenceType)) {
            return new ListMultiplicityObjectFactory();
        } else {
            return NULL_FACTORY;
        }
    }
}
