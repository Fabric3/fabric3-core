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

package org.fabric3.fabric.contract;

import junit.framework.TestCase;

import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.api.model.type.java.Signature;

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
