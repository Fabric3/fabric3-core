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
*/
package org.fabric3.databinding.jaxb.format;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.fabric3.spi.binding.format.AbstractParameterEncoder;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.invocation.Message;

/**
 * ParameterEncoder that uses JAXB. Currently, only string-based serialization is supported but enhancements to support alternative formats such as
 * binary streams could be supported.
 *
 * @version $Rev$ $Date$
 */
public class JAXBParameterEncoder extends AbstractParameterEncoder {
    private JAXBContext jaxbContext;

    /**
     * Constructor.
     *
     * @param jaxbContext the JAXBContext to use for de/serialization.
     */
    public JAXBParameterEncoder(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    @Override
    public String encodeText(Message message) throws EncoderException {
        return serialize(message);
    }

    @Override
    public Object decode(String operationName, String body) throws EncoderException {
        return deserialize(body);
    }

    @Override
    public Object decodeResponse(String operationName, String serialized) throws EncoderException {
        return decode(operationName, serialized);
    }

    @Override
    public Throwable decodeFault(String operationName, String serialized) throws EncoderException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(serialized);
            return (Throwable) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new EncoderException(e);
        }
    }

    private String serialize(Message message) throws EncoderException {
        Object body = message.getBody();
        if (body != null && body.getClass().isArray() && !body.getClass().isPrimitive()) {
            Object[] payload = (Object[]) body;
            if (payload.length > 1) {
                throw new UnsupportedOperationException("Multiple parameters not supported");
            }
            body = payload[0];
        }
        StringWriter writer = new StringWriter();
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(body, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new EncoderException(e);
        }
    }

    private Object deserialize(String body) throws EncoderException {
        StringReader reader = new StringReader(body);
        try {
            Unmarshaller marshaller = jaxbContext.createUnmarshaller();
            return marshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new EncoderException(e);
        }
    }


}