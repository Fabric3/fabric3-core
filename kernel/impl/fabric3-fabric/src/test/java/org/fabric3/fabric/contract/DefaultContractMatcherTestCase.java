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
package org.fabric3.fabric.contract;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.fabric3.spi.contract.ContractMatcherExtension;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class DefaultContractMatcherTestCase extends TestCase {

    public void testSetMatcher() throws Exception {
        DefaultContractMatcher matcher = new DefaultContractMatcher();
        JavaContractMatcherExtension extension = new JavaContractMatcherExtension();
        List<ContractMatcherExtension<?,?>> extensions =  Collections.<ContractMatcherExtension<?, ?>>singletonList(extension);
        matcher.setMatcherExtensions(extensions);

        JavaServiceContract source = new JavaServiceContract(TestService.class);
        JavaServiceContract target = new JavaServiceContract(TestService.class);
        assertTrue(matcher.isAssignableFrom(source, target, true).isAssignable());

    }


    private interface TestService {

        void test(String param);

        void test(int param);
    }
}
