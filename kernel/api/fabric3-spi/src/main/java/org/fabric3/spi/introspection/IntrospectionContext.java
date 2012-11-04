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
package org.fabric3.spi.introspection;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.fabric3.host.contribution.ValidationFailure;

/**
 * Context for the current introspection session.
 * <p/>
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
