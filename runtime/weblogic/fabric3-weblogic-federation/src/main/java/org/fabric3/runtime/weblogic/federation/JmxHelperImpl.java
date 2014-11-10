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
package org.fabric3.runtime.weblogic.federation;

import java.util.StringTokenizer;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.oasisopen.sca.annotation.Reference;

/**
 * Helper class for JMX operations.
 */
public final class JmxHelperImpl implements JmxHelper {
    private static final String RUNTIME_MBEAN = "com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean";
    private MBeanServer mbServer;

    public JmxHelperImpl(@Reference MBeanServer mbServer) {
        this.mbServer = mbServer;
    }

    public <T> T getRuntimeJmxAttribute(Class<T> clazz, String name) throws JMException {
        Object current = new ObjectName(RUNTIME_MBEAN);
        for (StringTokenizer st = new StringTokenizer(name, "/"); st.hasMoreTokens();) {
            String token = st.nextToken();
            current = mbServer.getAttribute((ObjectName) current, token);
        }
        return clazz.cast(current);
    }
}