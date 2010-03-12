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
package org.fabric3.binding.jms.runtime.helper;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fabric3.binding.jms.spi.provision.PayloadType;

/**
 * Utility class for message processing.
 *
 * @version $Rev$ $Date$
 */
public class MessageHelper {

    public static Message createBytesMessage(Session session, Object payload, PayloadType payloadType) throws JMSException {
        BytesMessage message = session.createBytesMessage();
        switch (payloadType) {

        case BOOLEAN:
            message.writeBoolean((Boolean) payload);
            break;
        case BYTE:
            message.writeByte((Byte) payload);
            break;
        case CHARACTER:
            message.writeChar((Character) payload);
            break;
        case DOUBLE:
            message.writeDouble((Double) payload);
            break;
        case FLOAT:
            message.writeFloat((Float) payload);
            break;
        case INTEGER:
            message.writeInt((Integer) payload);
            break;
        case LONG:
            message.writeLong((Long) payload);
            break;
        case SHORT:
            message.writeShort((Short) payload);
            break;
        }
        return message;
    }


    public static Object getPayload(Message message, PayloadType payloadType) throws JMSException {
        Object payload;
        switch (payloadType) {
        case OBJECT:
            ObjectMessage objectMessage = (ObjectMessage) message;
            payload = objectMessage.getObject();
            break;
        case STREAM:
            throw new UnsupportedOperationException("Stream message not yet supported");
        case TEXT:
            TextMessage textMessage = (TextMessage) message;
            payload = textMessage.getText();
            break;
        case XML:
            TextMessage xmlMessage = (TextMessage) message;
            payload = xmlMessage.getText();
            break;
        case BOOLEAN:
            BytesMessage booleanMessage = (BytesMessage) message;
            return booleanMessage.readBoolean();
        case BYTE:
            BytesMessage bytesMessage = (BytesMessage) message;
            return bytesMessage.readByte();
        case CHARACTER:
            BytesMessage charMessage = (BytesMessage) message;
            return charMessage.readChar();
        case DOUBLE:
            BytesMessage doubleMessage = (BytesMessage) message;
            return doubleMessage.readDouble();
        case FLOAT:
            BytesMessage floatMessage = (BytesMessage) message;
            return floatMessage.readFloat();
        case INTEGER:
            BytesMessage intMessage = (BytesMessage) message;
            return intMessage.readInt();
        case LONG:
            BytesMessage longMessage = (BytesMessage) message;
            return longMessage.readLong();
        case SHORT:
            BytesMessage shortMessage = (BytesMessage) message;
            return shortMessage.readShort();
        default:
            throw new UnsupportedOperationException("Unsupported message type: " + message);
        }
        return payload;
    }

    private MessageHelper() {
    }
}
