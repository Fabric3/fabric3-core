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
package org.fabric3.api.annotation.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * F3 specific persistence unit annotation that suppports CDI for JPA entity manager factories.
 *
 * Standard JPA annotation can be applied on constructor arguments.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PersistenceUnit {

    /**
     * @return The name by which the entity manager factory is to be accessed in the environment referencing context, and is not needed when
     *         dependency injection is used.
     */
    public abstract java.lang.String name() default "";

    /**
     * @return The name of the persistence unit as defined in the persistence.xml file.
     */
    public abstract java.lang.String unitName() default "";

}
