/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.cache.infinispan.introspection;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.fabric3.cache.infinispan.model.InfinispanResourceDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.w3c.dom.Document;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;


/**
 * Unit tests for infinispan configuration loader.
 * @version $Rev$ $Date$
 */
public class InfinispanTypeLoaderTest extends TestCase {

	private static final String config = "<caches><cache>" +
			"<namedCache name=\"dataIndexCache\">" +
			"<loaders passivation=\"false\" preload=\"true\" shared=\"true\">" +
			"	<loader purgeOnStartup=\"false\" class=\"org.infinispan.loaders.file.FileCacheStore\">" +
			"	<properties>" +
			"		<property name=\"location\" value=\"/tmp\" />" +
			"		<property name=\"streamBufferSize\" value=\"4096\" />" +
			"	</properties>" +
			"	</loader>" +
			"</loaders>" +
			"</namedCache></cache></caches>";
	
	/**
	 * Test method for {@link InfinispanTypeLoader#load(javax.xml.stream.XMLStreamReader, org.fabric3.spi.introspection.IntrospectionContext)}.
	 */
	public final void testLoad() throws Exception {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(config.getBytes()));
        LoaderHelper loaderHelper = EasyMock.createMock(LoaderHelper.class);
        Document doc = EasyMock.createMock(Document.class);
        EasyMock.expect(loaderHelper.transform(reader)).andReturn(doc);
        
        EasyMock.replay(loaderHelper);
        InfinispanResourceDefinition resource = new InfinispanTypeLoader(loaderHelper).load(reader, context);
        EasyMock.verify(loaderHelper);
	}

}




