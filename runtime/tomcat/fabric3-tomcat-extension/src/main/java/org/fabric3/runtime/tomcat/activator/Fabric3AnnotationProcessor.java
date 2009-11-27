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
package org.fabric3.runtime.tomcat.activator;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;

import org.apache.AnnotationProcessor;

import org.fabric3.spi.Injector;
import org.fabric3.spi.ObjectCreationException;

/**
 * Injects a servlet instance with reference proxies, properties, resources, and SCA APIs.
 * <p/>
 * Note this replaces standard Tomcat injection support for JSR-250 Commons Annotations, including <code>@Resource</code> and
 * <code>@PostConstruct</code>.
 *
 * @version $Rev$ $Date$
 */
public class Fabric3AnnotationProcessor implements AnnotationProcessor {
    private Map<String, List<Injector<?>>> injectorMappings;

    /**
     * Constructor.
     *
     * @param injectorMappings mapping of servlet class name to injectors.
     */
    public Fabric3AnnotationProcessor(Map<String, List<Injector<?>>> injectorMappings) {
        this.injectorMappings = injectorMappings;
    }

    @SuppressWarnings({"unchecked"})
    public void processAnnotations(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException {
        List<Injector<?>> injectors = injectorMappings.get(instance.getClass().getName());
        if (injectors != null) {
            for (Injector injector : injectors) {
                try {
                    injector.inject(instance);
                } catch (ObjectCreationException e) {
                    throw new InvocationTargetException(e);
                }
            }
        }
    }

    public void postConstruct(Object instance) throws IllegalAccessException, InvocationTargetException {

    }

    public void preDestroy(Object instance) throws IllegalAccessException, InvocationTargetException {

    }

}
