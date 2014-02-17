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
package org.fabric3.implementation.junit.introspection;

import javax.xml.namespace.QName;
import java.lang.annotation.Annotation;

import org.fabric3.api.Namespaces;
import org.fabric3.api.implementation.junit.Fabric3Runner;
import org.fabric3.spi.introspection.java.ComponentAnnotationMapper;
import org.junit.runner.RunWith;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Maps the {@link RunWith} annotation configured with the {@link Fabric3Runner} class to the JUnit component implementation type.
 */
@EagerInit
public class JUnitComponentAnnotationMapper implements ComponentAnnotationMapper {
    private static final QName JUNIT = new QName(Namespaces.F3, "junit");

    public QName getImplementationType(Annotation annotation) {
        if (!(annotation instanceof RunWith)) {
            return null;
        }
        RunWith runWith = (RunWith) annotation;
        if (!Fabric3Runner.class.equals(runWith.value())) {
            return null;
        }
        return JUNIT;
    }
}
