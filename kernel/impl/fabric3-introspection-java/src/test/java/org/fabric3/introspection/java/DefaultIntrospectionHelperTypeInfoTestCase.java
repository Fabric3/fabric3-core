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
package org.fabric3.introspection.java;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

import junit.framework.TestCase;

import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.model.type.java.JavaTypeInfo;

/**
 * @version $Rev$ $Date$
 */
public class DefaultIntrospectionHelperTypeInfoTestCase extends TestCase {

    public void testTypeInfo() throws Exception {
        DefaultIntrospectionHelper helper = new DefaultIntrospectionHelper();
        TypeMapping mapping = new TypeMapping();
        helper.resolveTypeParameters(BondMission.class, mapping);

        Field subject = Activity.class.getField("subject");
        Type subjectType = subject.getGenericType();
        JavaTypeInfo info = helper.createTypeInfo(subjectType, mapping);
        assertEquals(Map.class, info.getRawType());
        assertEquals(String.class, info.getParameterTypesInfos().get(0).getRawType());
        assertEquals(Agent.class, info.getParameterTypesInfos().get(1).getRawType());

        Field action = Activity.class.getField("action");
        Type actionType = action.getGenericType();
        info = helper.createTypeInfo(actionType, mapping);
        assertEquals(Mission.class, info.getRawType());
        assertTrue(info.getParameterTypesInfos().isEmpty());

    }

    public static class Activity<S, A> {
        public S subject;
        public A action;
    }

    public static class SecretAgentActivity<A2> extends Activity<Map<String, Agent>, A2> {
    }

    public static class SecretAgentMission extends SecretAgentActivity<Mission> {
    }

    public static class BondMission extends SecretAgentMission {
    }

    public static class Agent {
    }

    public static class Mission {
    }

}


