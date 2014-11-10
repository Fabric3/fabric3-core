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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that can be applied to an implementation method to indicate it should be exposed to a management framework.
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface ManagementOperation {

    /**
     * Returns the operation path relative to the managed resource.
     *
     * @return the operation path.
     */
    String path() default "";

    /**
     * Returns the type of operation.
     *
     * @return the operation type
     */
    OperationType type() default OperationType.UNDEFINED;

    /**
     * Returns the operation description.
     *
     * @return the operation description
     */
    String description() default "";

    /**
     * Returns the roles allowed to access the operation.
     *
     * @return the roles allowed to access the operation
     */
    String[] rolesAllowed() default {};
}