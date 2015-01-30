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

import junit.framework.TestCase;
import org.fabric3.api.model.type.java.Signature;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class JavaContractMatcherExtensionTestCase extends TestCase {

    public void testNoMatchLocalDifferentHierarchy() throws Exception {
        JavaContractMatcherExtension matcher = new JavaContractMatcherExtension();

        JavaServiceContract testService = new JavaServiceContract(TestService.class);
        JavaServiceContract otherTestService = new JavaServiceContract(OtherTestService.class);
        assertFalse(matcher.isAssignableFrom(testService, otherTestService, true).isAssignable());
    }

    public void testMatchRemoteDifferentHierarchy() throws Exception {
        JavaContractMatcherExtension matcher = new JavaContractMatcherExtension();

        JavaServiceContract testService = new JavaServiceContract(TestService.class);
        testService.setRemotable(true);
        JavaServiceContract otherTestService = new JavaServiceContract(OtherTestService.class);
        otherTestService.setRemotable(true);
        assertTrue(matcher.isAssignableFrom(testService, otherTestService, true).isAssignable());
    }

    public void testNoMatchRemoteNarrowTarget() throws Exception {
        JavaContractMatcherExtension matcher = new JavaContractMatcherExtension();

        JavaServiceContract testService = new JavaServiceContract(TestService.class);
        testService.setRemotable(true);
        JavaServiceContract restrictedService = new JavaServiceContract(RestrictedTestService.class);
        restrictedService.setRemotable(true);
        assertFalse(matcher.isAssignableFrom(testService, restrictedService, true).isAssignable());
    }

    public void testMatchRemoteWiderTarget() throws Exception {
        JavaContractMatcherExtension matcher = new JavaContractMatcherExtension();

        JavaServiceContract restrictedService = new JavaServiceContract(RestrictedTestService.class);
        restrictedService.setRemotable(true);
        JavaServiceContract testService = new JavaServiceContract(TestService.class);
        testService.setRemotable(true);
        assertTrue(matcher.isAssignableFrom(restrictedService, testService, true).isAssignable());
    }

    public void testMatchSameClassNameRemoteWiderTarget() throws Exception {
        JavaContractMatcherExtension matcher = new JavaContractMatcherExtension();

        JavaServiceContract source = new JavaServiceContract(TestService.class);
        source.setRemotable(true);
        JavaServiceContract target = new JavaServiceContract(TestService.class);
        target.getMethodSignatures().add(new Signature("foo"));   // simulate same interface name but with an additional method
        target.setRemotable(true);
        assertTrue(matcher.isAssignableFrom(source, target, true).isAssignable());
    }

    public void testNoMatchSameClassRemoteNarrowTarget() throws Exception {
        JavaContractMatcherExtension matcher = new JavaContractMatcherExtension();

        JavaServiceContract source = new JavaServiceContract(TestService.class);
        source.setRemotable(true);
        source.getMethodSignatures().add(new Signature("foo"));   // simulate same interface name but with an additional method

        JavaServiceContract target = new JavaServiceContract(TestService.class);
        target.setRemotable(true);
        assertFalse(matcher.isAssignableFrom(source, target, true).isAssignable());
    }


    private interface TestService {

        void test(String param);

        void test(int param);
    }

    private interface RestrictedTestService {

        void test(String param);

    }

    private interface OtherTestService {

        void test(String param);

        void test(int param);
    }

}
