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

package org.fabric3.implementation.pojo.builder;

import org.w3c.dom.Document;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;

/**
 * Creates ObjectFactory instances for property values.
 *
 * @version $Rev$ $Date$
 */
public interface PropertyObjectFactoryBuilder {

    /**
     * Create the ObjectFactory from the given DOM value.
     *
     * @param name        the property name
     * @param dataType    the property type
     * @param value       the DOM to transform
     * @param many        true if the property is many-valued
     * @param classLoader the classloader for the target type
     * @return the ObjectFactory
     * @throws BuilderException if there is an error building the factory
     */
    ObjectFactory<?> createObjectFactory(String name, DataType<?> dataType, Document value, boolean many, ClassLoader classLoader)
            throws BuilderException;
}
