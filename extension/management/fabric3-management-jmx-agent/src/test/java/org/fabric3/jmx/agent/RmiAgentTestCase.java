/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.jmx.agent;

import junit.framework.TestCase;

/**
 * @version $Revision: 9250 $ $Date: 2010-07-30 12:52:01 +0200 (Fri, 30 Jul 2010) $
 */
public class RmiAgentTestCase extends TestCase {

    public void testPortParse() throws Exception {
        RmiAgent agent = new RmiAgent(null, null, null);
        agent.setJmxPort("1111");
        assertEquals(1111, agent.getMinPort());
        assertEquals(1111, agent.getMaxPort());
    }

    public void testPortRangeParse() throws Exception {
        RmiAgent agent = new RmiAgent(null, null, null);
        agent.setJmxPort("1000-1200");
        assertEquals(1000, agent.getMinPort());
        assertEquals(1200, agent.getMaxPort());
    }

}
