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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.zeromq.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperation;

/**
 *
 */
public final class ZeroMQAttacherHelper {

    private ZeroMQAttacherHelper() {
    }

    /**
     * Returns the invocation chains for a wire in their natural order.
     *
     * @param wire the wire
     * @return the invocation chains
     */
    public static List<InvocationChain> sortChains(Wire wire) {
        TreeMap<PhysicalOperation, InvocationChain> map = new TreeMap<>();
        for (InvocationChain chain : wire.getInvocationChains()) {
            map.put(chain.getPhysicalOperation(), chain);
        }
        List<InvocationChain> sorted = new ArrayList<>();
        sorted.addAll(map.values());
        return sorted;
    }

}
