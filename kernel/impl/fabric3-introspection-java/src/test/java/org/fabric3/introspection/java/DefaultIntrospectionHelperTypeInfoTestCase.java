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
 */
package org.fabric3.introspection.java;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

import junit.framework.TestCase;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.model.type.java.JavaTypeInfo;

/**
 *
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


