/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.implementation.pojo.builder;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;

/**
 * @version $Rev$ $Date$
 */
public final class MethodUtils {

    public static Method findMethod(PhysicalSourceDefinition sourceDefinition,
                                    PhysicalTargetDefinition targetDefinition,
                                    PhysicalOperationDefinition operation,
                                    Class<?> implementationClass,
                                    ClassLoader loader,
                                    ClassLoaderRegistry classLoaderRegistry) throws WireAttachException {
        List<String> params = operation.getTargetParameterTypes();
        Class<?>[] paramTypes = new Class<?>[params.size()];
        assert loader != null;
        for (int i = 0; i < params.size(); i++) {
            String param = params.get(i);
            try {
                paramTypes[i] = classLoaderRegistry.loadClass(loader, param);
            } catch (ClassNotFoundException e) {
                URI sourceUri = sourceDefinition.getUri();
                URI targetUri = targetDefinition.getUri();
                throw new WireAttachException("Implementation class not found when wiring " + sourceUri + " to " + targetUri, e);
            }
        }
        Method method = null;
        if (operation.isRemotable()) {
            // if the operation is remotable, do not match on parameter types since method names cannot be overloaded
            Method[] methods = implementationClass.getMethods();
            String name = operation.getName();
            for (Method entry : methods) {
                if (name.equals(entry.getName())) {
                    method = entry;
                    break;
                }
            }
            if (method == null) {
                URI sourceUri = sourceDefinition.getUri();
                URI targetUri = targetDefinition.getUri();
                throw new WireAttachException("No matching method found when wiring " + sourceUri + " to " + targetUri);
            }
        } else {
            // operation is remote, match on operation names and parameter types
            try {
                method = implementationClass.getMethod(operation.getName(), paramTypes);
            } catch (NoSuchMethodException e) {
                URI sourceUri = sourceDefinition.getUri();
                URI targetUri = targetDefinition.getUri();
                throw new WireAttachException("No matching method found when wiring " + sourceUri + " to " + targetUri, e);
            }
        }
        return method;
    }

}
