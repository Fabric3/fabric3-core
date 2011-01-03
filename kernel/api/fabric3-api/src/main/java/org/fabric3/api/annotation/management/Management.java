/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
*/
package org.fabric3.api.annotation.management;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that can be applied to an implementation to indicate it should be exposed to a management framework.
 *
 * @version $Rev$ $Date$
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
