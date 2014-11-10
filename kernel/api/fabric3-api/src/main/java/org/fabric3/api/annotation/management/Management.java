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
package org.fabric3.api.annotation.management;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that can be applied to an implementation to indicate it should be exposed to a management framework.
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface Management {
    String FABRIC3_ADMIN_ROLE = "ROLE_FABRIC3_ADMIN";

    String FABRIC3_OBSERVER_ROLE = "ROLE_FABRIC3_OBSERVER";

    /**
     * Returns the name the implementation should be registered with.
     *
     * @return the name the implementation should be registered with
     */
    String name() default "";

    /**
     * Returns the group the implementation should be registered under.
     *
     * @return the group the implementation should be registered under
     */
    String group() default "";

    /**
     * Returns the managed resource path.
     *
     * @return the managed resource path.
     */
    String path() default "";

    /**
     * Returns the management description.
     *
     * @return the management description
     */
    String description() default "";

    /**
     * Returns the roles required to access getter attributes.
     *
     * @return the roles required to access getter attributes
     */
    String[] readRoles() default {FABRIC3_ADMIN_ROLE, FABRIC3_OBSERVER_ROLE};

    /**
     * Returns the roles required to access setter attributes and operations.
     *
     * @return the roles required to access setter attributes and operations
     */
    String[] writeRoles() default {FABRIC3_ADMIN_ROLE};

}
