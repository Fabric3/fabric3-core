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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.introspection.java.contract;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 * Introspects a JavaServiceContract from a Java type.
 *
 * @version $Rev$ $Date$
 */
public interface JavaContractProcessor {
    /**
     * Introspects a Java class and return the JavaServiceContract. If validation errors or warnings are encountered, they will be reported in the
     * IntrospectionContext.
     *
     * @param interfaze the Java class to introspect
     * @param context   the introspection context for reporting errors and warnings
     * @return the ServiceContract corresponding to the interface type
     */
    JavaServiceContract introspect(Class<?> interfaze, IntrospectionContext context);

    /**
     * Introspects a generic Java class and return the JavaServiceContract. If validation errors or warnings are encountered, they will be reported in
     * the IntrospectionContext.
     *
     * @param interfaze the Java class to introspect
     * @param baseClass the base class to use for introspecting and resolving generic formal types to actual types. For example, a service contract on
     *                  a reference may contain a formal type declartion (e.g. T) that is defined by the implementation class where the reference is
     *                  injected. The base class may also be the same as the interface to be introspected in cases where a service contract is not
     *                  associated with an implementation class.
     * @param context   the introspection context for reporting errors and warnings
     * @return the ServiceContract corresponding to the interface type
     */
    JavaServiceContract introspect(Class<?> interfaze, Class<?> baseClass, IntrospectionContext context);

}
