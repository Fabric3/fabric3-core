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
 */
package org.fabric3.api.annotation.model;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.fabric3.api.Namespaces;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Configures a class as a component
 */
@Target({TYPE})
@Retention(RUNTIME)
@Inherited
public @interface Component {
    public static final String DEFAULT_COMPOSITE = Namespaces.F3_PREFIX + "DefaultApplicationComposite";

    /**
     * Specifies the composite qualified name
     *
     * @return the composite name
     */
    String composite() default DEFAULT_COMPOSITE;

    /**
     * Specifies the component name.
     *
     * @return the component name
     */
    String name() default "";

    /**
     * Specifies namespaces used by component configuration. Namespaces may be referenced in configuration elements such as properties.
     *
     * @return the namespaces
     */
    Namespace[] namespaces() default {};
}
