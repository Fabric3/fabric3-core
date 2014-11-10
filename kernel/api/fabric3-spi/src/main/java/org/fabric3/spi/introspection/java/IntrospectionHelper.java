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
package org.fabric3.spi.introspection.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.spi.introspection.ImplementationNotFoundException;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;

/**
 * Helper service that provides support methods to simplify introspection.
 */
public interface IntrospectionHelper {

    /**
     * Load the class using the supplied ClassLoader. The class will be defined so any initializer present will be fired. As the class is being
     * loaded, the Thread context ClassLoader will be set to the supplied classloader.
     *
     * @param name the name of the class to load
     * @param cl   the classloader to use to load it
     * @return the class
     * @throws ImplementationNotFoundException
     *          if the class could not be found
     */
    Class<?> loadClass(String name, ClassLoader cl) throws ImplementationNotFoundException;

    /**
     * Derive the name of an injection site from a field.
     *
     * @param field    the field to inspect
     * @param override an override specified in an annotation
     * @return the name of the injection site
     */
    String getSiteName(Field field, String override);

    /**
     * Derive the name of an injection site from a setter method.
     *
     * @param setter   the setter method to inspect
     * @param override an override specified in an annotation
     * @return the name of the injection site
     */
    String getSiteName(Method setter, String override);

    /**
     * Derive the name of an injection site from a setter method.
     *
     * @param constructor the constructor to inspect
     * @param index       the index of the constructor parameter to inspect
     * @param override    an override specified in an annotation
     * @return the name of the injection site
     */
    String getSiteName(Constructor<?> constructor, int index, String override);

    /**
     * Returns the generic type of a setter method.
     *
     * @param setter the method to inspect
     * @return the type of value the setter method injects
     */
    Type getGenericType(Method setter);

    /**
     * Returns the generic type of a method parameter.
     *
     * @param method the method to inspect
     * @param index  the parameter index
     * @return the type of value the method injects
     */
    Type getGenericType(Method method, int index);

    /**
     * Returns the generic type of a constructor parameter.
     *
     * @param constructor the constructor to inspect
     * @param index       the parameter index
     * @return the type of value the constructor injects
     */
    Type getGenericType(Constructor<?> constructor, int index);

    /**
     * Add multiplicity metadata to the reference definition, including cardinality and if the multiplicity is keyed.
     *
     * @param definition  the reference definition
     * @param required    whether a value must be supplied (implies 1.. multiplicity)
     * @param type        the multiplicity of a type
     * @param typeMapping the current introspection type mapping    @return the multiplicity of the type
     */
    public void processMultiplicity(ReferenceDefinition definition, boolean required, Type type, TypeMapping typeMapping);

    /**
     * Introspects the type Returns true if the supplied type should be treated as many-valued.
     * <p/>
     * This is generally true for arrays, Collection or Map types.
     *
     * @param type        the type to check
     * @param typeMapping the mapping to use to resolve any formal types
     * @return true if the type should be treated as many-valued
     */
    MultiplicityType introspectMultiplicity(Type type, TypeMapping typeMapping);

    /**
     * Heuristically determines the injection type of the field, method, or constructor parameter associated with the given type.
     *
     * @param type        the type to infer
     * @param typeMapping the type mapping for resolved parameters
     * @return the attribute type
     */
    InjectableType inferType(Type type, TypeMapping typeMapping);

    /**
     * Determine if an annotation is present on this interface or any super interface.
     * <p/>
     * This is similar to the use of @Inherited on classes (given @Inherited does not apply to interfaces).
     *
     * @param type           the interface to check
     * @param annotationType the annotation to look for
     * @return true if the annotation is present
     */
    boolean isAnnotationPresent(Class<?> type, Class<? extends Annotation> annotationType);

    /**
     * Resolves the formal parameters of a class, its super class and super interfaces to the concrete types.
     *
     * @param type        the class whose parameters should be resolved
     * @param typeMapping the type mapping to update with resolved types
     */
    void resolveTypeParameters(Class<?> type, TypeMapping typeMapping);

    /**
     * Creates a JavaTypeInfo for a given type based on the class hierarchy the type is used in. For example, the JavaTypeInfo for a field type or
     * method parameter will be introspected using the containing class hierarchy and its generics declarations.
     *
     * @param type        the type to introspect
     * @param typeMapping the type mapping to use for introspecting the class hierarchy
     * @return the TypeInfo
     */
    JavaTypeInfo createTypeInfo(Type type, TypeMapping typeMapping);

    /**
     * Returns the base type for the supplied type.
     * <p/>
     * The base type is the actual type of a property or reference having removed any decoration for arrays or collections.
     *
     * @param type        the type of a field or parameter
     * @param typeMapping the mapping to use to resolve any formal types
     * @return the actual type of the property or reference corresponding to the parameter
     */
    Class<?> getBaseType(Type type, TypeMapping typeMapping);

    /**
     * Returns all service interfaces directly implemented by a class or any super class.
     * <p/>
     * Class#getInterfaces only returns interfaces directly implemented by the class. This method returns all interfaces including those implemented
     * by any super classes. It excludes interfaces that are super-interfaces of those implemented by subclasses.
     *
     * @param type the class whose interfaces should be returned
     * @return the unique interfaces implemented by that class
     */
    Set<Class<?>> getImplementedInterfaces(Class<?> type);

    /**
     * Returns method injection sites provided by a class or any super class.
     * <p/>
     * Methods that are part of any service contract are excluded.
     *
     * @param type     the class whose method sites should be returned
     * @param services the services implemented by the class
     * @return the method injection sites for the class
     */
    Set<Method> getInjectionMethods(Class<?> type, Collection<AbstractService> services);

    /**
     * Returns method injection sites provided by a class or any super class.
     * <p/>
     * Methods that are part of any service contract are excluded.
     *
     * @param type the class whose field injection sites should be returned
     * @return the setter injection sites for the class
     */
    Set<Field> getInjectionFields(Class<?> type);
}
