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
package org.fabric3.spi.introspection;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.failure.ValidationFailure;

/**
 * Context for the current introspection session.
 *
 * The context allows both errors and warnings to be gathered. Errors indicate problems that will prevent an assembly from being activated such as a
 * missing component implementation. Warnings indicate issues that are not in themselves fatal but which may result in an activation failure.
 */
public interface IntrospectionContext {

    /**
     * Returns the active contribution URI.
     *
     * @return the active contribution URI
     */
    URI getContributionUri();

    /**
     * Returns true if the validation has detected any fatal errors.
     *
     * @return true if the validation has detected any fatal errors
     */
    boolean hasErrors();

    /**
     * Returns the list of fatal errors detected during validation.
     *
     * @return the list of fatal errors detected during validation
     */
    List<ValidationFailure> getErrors();

    /**
     * Add a fatal error to the validation results.
     *
     * @param e the fatal error that has been found
     */
    void addError(ValidationFailure e);

    /**
     * Add a collection of fatal errors to the validation results.
     *
     * @param errors the fatal errors that have been found
     */
    void addErrors(List<ValidationFailure> errors);

    /**
     * Returns true if the validation has detected any non-fatal warnings.
     *
     * @return true if the validation has detected any non-fatal warnings
     */
    boolean hasWarnings();

    /**
     * Returns the list of non-fatal warnings detected during validation.
     *
     * @return the list of non-fatal warnings detected during validation
     */
    List<ValidationFailure> getWarnings();

    /**
     * Add a non-fatal warning to the validation results.
     *
     * @param e the non-fatal warning that has been found
     */
    void addWarning(ValidationFailure e);

    /**
     * Add a collection of non-fatal warnings to the validation results.
     *
     * @param warnings the non-fatal warnings that have been found
     */
    void addWarnings(List<ValidationFailure> warnings);

    /**
     * Returns the classloader of the contribution being installed.
     *
     * @return the contribution classloader
     */
    ClassLoader getClassLoader();

    /**
     * Returns the location of the XML artifact being introspected.
     *
     * @return the location of the XML artifact being introspected. This may return null if the source is not a dereferenceable artifact.
     */
    URL getSourceBase();

    /**
     * Target namespace for this loader context.
     *
     * @return Target namespace.
     */
    String getTargetNamespace();

    /**
     * Sets the current target namespace
     *
     * @param namespace the namespace to set
     */
    void setTargetNamespace(String namespace);

    /**
     * Used for introspecting Java generics. Returns a cache of mappings from formal parameter types to actual types for a class. Since the
     * IntrospectionContext is disposed after a contribution has been installed, it is safe to cache pointers to classes.
     *
     * @param type the class
     * @return the cache of mappings from formal parameter types to actual types for a class or null if the mapping does not exist
     */
    TypeMapping getTypeMapping(Class<?> type);

    /**
     * Used for introspecting Java generics. Returns the cache of classes and their resolved parameter types mapped to actual types.
     *
     * @return the cache of classes and their resolved parameter types mapped to actual types
     */
    Map<Class<?>, TypeMapping> getTypeMappings();

    /**
     * Adds a mapping from formal parameter types to actual types for a class to the cache.
     *
     * @param type        the class
     * @param typeMapping the mappings
     */
    void addTypeMapping(Class<?> type, TypeMapping typeMapping);

}
