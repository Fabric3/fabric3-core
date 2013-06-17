/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.host.runtime;

/**
 * Records an extension component provided by the host environment that must be registered with the runtime domain.
 */
public class ComponentRegistration {
    private String name;
    private Class<?> service;
    private Object instance;
    private boolean introspect;

    /**
     * Constructor.
     *
     * @param name       the component name
     * @param service    the component service contract
     * @param instance   the component instance
     * @param introspect true if the component should be introspected and a component type generated
     * @param <S>        the service contract type
     * @param <I>        the component instance
     */
    public <S, I extends S> ComponentRegistration(String name, Class<S> service, I instance, boolean introspect) {
        this.name = name;
        this.service = service;
        this.instance = instance;
        this.introspect = introspect;
    }

    public String getName() {
        return name;
    }

    public Class<?> getService() {
        return service;
    }

    public Object getInstance() {
        return instance;
    }

    public boolean isIntrospect() {
        return introspect;
    }
}
