/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.implementation.web.runtime;

import java.util.List;
import java.util.Map;

import org.fabric3.spi.Injector;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.model.type.java.InjectionSite;

/**
 * Creates Injector collections for injecting references, properties and context proxies into web application artifacts. These include servlets,
 * filters, the servlet context, and the session context.
 *
 * @version $Rev$ $Date$
 */
public interface InjectorFactory {
    /**
     * Populates a map of Injectors for each injectable artifact (servlet, filter, servlet context or session context) in the  web application.
     *
     * @param injectors    the map to populate, keyed by artifact id (e.g. servlet class name)
     * @param siteMappings a map keyed by site name (e.g. a reference or property name). The value is a map keyed by injectable artifact id with a
     *                     value containing a description of the injection site. For example, a reference may be injected on fields of multiple
     *                     servlets. This would be represented by an entry keyed on reference name with a value of a map keyed by servlet class name
     *                     and values containing injection site descriptions of the servlet fields.
     * @param factories    the object factories that supply injected values.
     * @param classLoader  the classloader to load classes in for the web application
     * @throws InjectionCreationException if an error occurs creating the injectors.
     */
    void createInjectorMappings(Map<String, List<Injector<?>>> injectors,
                                Map<String, Map<String, InjectionSite>> siteMappings,
                                Map<String, ObjectFactory<?>> factories,
                                ClassLoader classLoader) throws InjectionCreationException;
}
