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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.introspection;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.failure.ValidationFailure;

/**
 * Default implementation of an IntrospectionContext.
 */
public class DefaultIntrospectionContext implements IntrospectionContext {
    private List<ValidationFailure> errors = new ArrayList<>();
    private List<ValidationFailure> warnings = new ArrayList<>();
    private ClassLoader classLoader;
    private URL sourceBase;
    private String targetNamespace;
    private URI contributionUri;
    private Map<Class<?>, TypeMapping> typeMappings = new HashMap<>();

    /**
     * Constructor.
     */
    public DefaultIntrospectionContext() {
    }

    /**
     * Constructor.
     *
     * @param contributionUri the active contribution URI
     * @param classLoader     the classloader for loading application resources
     * @param sourceBase      the composite location
     * @param targetNamespace the target namespace.
     */
    public DefaultIntrospectionContext(URI contributionUri, ClassLoader classLoader, URL sourceBase, String targetNamespace) {
        this.classLoader = classLoader;
        this.sourceBase = sourceBase;
        this.targetNamespace = targetNamespace;
        this.contributionUri = contributionUri;
    }

    /**
     * Constructor.
     *
     * @param contributionUri the active contribution URI
     * @param classLoader     the classloader for loading application resources
     */
    public DefaultIntrospectionContext(URI contributionUri, ClassLoader classLoader) {
        this(contributionUri, classLoader, null, null);
    }

    /**
     * Constructor.
     *
     * @param contributionUri the active contribution URI
     * @param classLoader     the classloader for loading application resources
     * @param sourceBase      the composite location
     */
    public DefaultIntrospectionContext(URI contributionUri, ClassLoader classLoader, URL sourceBase) {
        this(contributionUri, classLoader, sourceBase, null);
    }

    /**
     * Initializes from a parent context, overriding the target namespace.
     *
     * @param parentContext   the parent context.
     * @param targetNamespace the target namespace.
     */
    public DefaultIntrospectionContext(IntrospectionContext parentContext, String targetNamespace) {
        this(parentContext.getContributionUri(),
             parentContext.getClassLoader(),
             parentContext.getSourceBase(),
             targetNamespace
        );
    }

    /**
     * Initializes from a parent context.
     *
     * @param parentContext Parent context.
     */
    public DefaultIntrospectionContext(IntrospectionContext parentContext) {
        this(parentContext.getContributionUri(),
             parentContext.getClassLoader(),
             parentContext.getSourceBase(),
             parentContext.getTargetNamespace());
        typeMappings.putAll(parentContext.getTypeMappings());
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ValidationFailure> getErrors() {
        return errors;
    }

    public void addError(ValidationFailure e) {
        errors.add(e);
    }

    public void addErrors(List<ValidationFailure> errors) {
        this.errors.addAll(errors);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public List<ValidationFailure> getWarnings() {
        return warnings;
    }

    public void addWarning(ValidationFailure e) {
        warnings.add(e);
    }

    public void addWarnings(List<ValidationFailure> warnings) {
        this.warnings.addAll(warnings);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public URL getSourceBase() {
        return sourceBase;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public URI getContributionUri() {
        return contributionUri;
    }

    public TypeMapping getTypeMapping(Class<?> type) {
        return typeMappings.get(type);
    }

    public void addTypeMapping(Class<?> type, TypeMapping typeMapping) {
        typeMappings.put(type, typeMapping);
    }

    public Map<Class<?>, TypeMapping> getTypeMappings() {
        return typeMappings;
    }
}
