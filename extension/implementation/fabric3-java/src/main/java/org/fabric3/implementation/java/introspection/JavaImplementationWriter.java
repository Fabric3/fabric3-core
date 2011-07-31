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
package org.fabric3.implementation.java.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.implementation.java.model.JavaImplementation;
import org.fabric3.spi.introspection.xml.TypeWriter;
import org.fabric3.spi.introspection.xml.UnrecognizedTypeException;
import org.fabric3.spi.introspection.xml.Writer;

/**
 * Serializes a Java component implementation to a StAX stream.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JavaImplementationWriter implements TypeWriter<JavaImplementation> {
    private Writer writer;

    @Reference(required = false)
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    @Init
    public void init() {
        if (writer != null) {
            writer.register(JavaImplementation.class, this);
        }
    }

    @Destroy
    public void destroy() {
        if (writer != null) {
            writer.unregister(JavaImplementation.class);
        }
    }

    public void write(JavaImplementation implementation, XMLStreamWriter writer) throws XMLStreamException, UnrecognizedTypeException {
        writer.writeStartElement("implementation.java");
        writer.writeAttribute("class", implementation.getImplementationClass());
        writer.writeEndElement();
    }
}
