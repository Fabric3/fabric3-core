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
package org.fabric3.introspection.java.annotation;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.oasisopen.sca.annotation.Confidentiality;
import org.oasisopen.sca.annotation.PolicySets;
import org.oasisopen.sca.annotation.Requires;

import org.fabric3.model.type.PolicyAware;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

import static org.oasisopen.sca.annotation.Confidentiality.CONFIDENTIALITY;
import static org.oasisopen.sca.annotation.Confidentiality.CONFIDENTIALITY_MESSAGE;

/**
 *
 */
public class PolicyAnnotationProcessorImplTestCase extends TestCase {
    private PolicyAnnotationProcessorImpl processor = new PolicyAnnotationProcessorImpl();

    public void testRequires() throws Exception {
        Requires annotation = TestClass.class.getAnnotation(Requires.class);
        IntrospectionContext ctx = new DefaultIntrospectionContext();

        QName qname = new QName("namespace", "foo");
        PolicyAware modelObject = EasyMock.createMock(PolicyAware.class);
        modelObject.addIntent(qname);
        EasyMock.expectLastCall();
        EasyMock.replay(modelObject);

        processor.process(annotation, modelObject, ctx);
        EasyMock.verify(modelObject);
    }

    public void testPolicySets() throws Exception {
        PolicySets annotation = TestPolicySet.class.getAnnotation(PolicySets.class);
        IntrospectionContext ctx = new DefaultIntrospectionContext();

        QName qname = new QName("namespace", "foo");
        PolicyAware modelObject = EasyMock.createMock(PolicyAware.class);
        modelObject.addPolicySet(qname);
        EasyMock.expectLastCall();
        EasyMock.replay(modelObject);

        processor.process(annotation, modelObject, ctx);
        EasyMock.verify(modelObject);
    }

    public void testUnQualified() throws Exception {
        Confidentiality annotation = TestClass.class.getAnnotation(Confidentiality.class);
        IntrospectionContext ctx = new DefaultIntrospectionContext();

        QName qname = QName.valueOf(CONFIDENTIALITY);
        PolicyAware modelObject = EasyMock.createMock(PolicyAware.class);
        modelObject.addIntent(qname);
        EasyMock.expectLastCall();
        EasyMock.replay(modelObject);

        processor.process(annotation, modelObject, ctx);
        EasyMock.verify(modelObject);
    }

    public void testQualified() throws Exception {
        Confidentiality annotation = TestQualifiedClass.class.getAnnotation(Confidentiality.class);
        IntrospectionContext ctx = new DefaultIntrospectionContext();

        QName qname = QName.valueOf(CONFIDENTIALITY_MESSAGE);
        PolicyAware modelObject = EasyMock.createMock(PolicyAware.class);
        modelObject.addIntent(qname);
        EasyMock.expectLastCall();
        EasyMock.replay(modelObject);

        processor.process(annotation, modelObject, ctx);
        EasyMock.verify(modelObject);
    }

    @Confidentiality
    @Requires("{namespace}foo")
    private class TestClass {


    }


    @Confidentiality(CONFIDENTIALITY_MESSAGE)
    private class TestQualifiedClass {

    }

    @PolicySets("{namespace}foo")
    private class TestPolicySet {

    }
}