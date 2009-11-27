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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.webapp;

import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletContext;

import junit.framework.TestCase;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

/**
 * @version $Rev$ $Date$
 */
public class WebappUtilTestCase extends TestCase {
    private ServletContext context;
    private WebappUtilImpl util;
    private ClassLoader cl;
    private URL systemUrl;


    public void testGetInitParameterWhenSpecified() {
        String name = "name";
        String value = "default";
        expect(context.getInitParameter(name)).andReturn(value);
        replay(context);

        assertEquals(value, util.getInitParameter(name, "default"));
        verify(context);
    }

    public void testGetInitParameterUsingDefault() {
        String name = "name";
        String value = "default";
        expect(context.getInitParameter(name)).andReturn(null);
        replay(context);

        assertEquals(value, util.getInitParameter(name, value));
        verify(context);
    }

    public void testGetInitParameterWithZeroLength() {
        String name = "name";
        String value = "default";
        expect(context.getInitParameter(name)).andReturn("");
        replay(context);

        assertEquals(value, util.getInitParameter(name, value));
        verify(context);
    }

    public void testGetScdlFromWebapp() throws MalformedURLException {
        String path = "/WEB-INF/test";
        expect(context.getResource(path)).andReturn(systemUrl);
        replay(context);
        replay(cl);
        assertSame(systemUrl, util.convertToURL(path, cl));
        verify(context);
        verify(cl);
    }

    public void testGetScdlFromWebappMissing() throws MalformedURLException {
        String path = "/WEB-INF/test";
        expect(context.getResource(path)).andReturn(null);
        replay(context);
        expect(cl.getResource(path)).andReturn(null);
        replay(cl);
        assertNull(util.convertToURL(path, cl));
        verify(context);
        verify(cl);
    }

    public void testGetScdlFromWebappMalformed() throws MalformedURLException {
        String path = "/WEB-INF/test";
        expect(context.getResource(path)).andThrow(new MalformedURLException());
        replay(context);
        replay(cl);
        try {
            util.convertToURL(path, cl);
            fail();
        } catch (MalformedURLException e) {
            // OK
        }
        verify(context);
        verify(cl);
    }

    public void testGetScdlFromClasspath() throws MalformedURLException {
        String path = "META-INF/test";
        replay(context);
        expect(cl.getResource(path)).andReturn(systemUrl);
        replay(cl);
        assertSame(systemUrl, util.convertToURL(path, cl));
        verify(context);
        verify(cl);
    }

    public void testGetScdlFromClasspathMissing() throws MalformedURLException {
        String path = "META-INF/test";
        replay(context);
        expect(cl.getResource(path)).andReturn(null);
        replay(cl);
        assertNull(util.convertToURL(path, cl));
        verify(context);
        verify(cl);
    }

    protected void setUp() throws Exception {
        super.setUp();
        context = createMock(ServletContext.class);
        util = new WebappUtilImpl(context);
        cl = createMock(ClassLoader.class);
        systemUrl = new URL("file:/system.scdl");
    }
}
