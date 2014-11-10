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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.jms.runtime.wire;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fabric3.binding.jms.spi.provision.PayloadType;

/**
 * Utility class for message processing.
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


    public static Object getPayload(Message message, PayloadType payloadType) throws JMSException, JmsBadMessageException {
        Object payload;
        switch (payloadType) {
        case OBJECT:
            if (!(message instanceof ObjectMessage)) {
                throw new JmsBadMessageException("Invalid message type. Expecting ObjectMessage");
            }
            ObjectMessage objectMessage = (ObjectMessage) message;
            payload = objectMessage.getObject();
            break;
        case STREAM:
            throw new UnsupportedOperationException("Stream message not yet supported");
        case TEXT:
            if (!(message instanceof TextMessage)) {
                throw new JmsBadMessageException("Invalid message type. Expecting TextMessage");
            }
            TextMessage textMessage = (TextMessage) message;
            payload = textMessage.getText();
            break;
        case BOOLEAN:
            if (!(message instanceof BytesMessage)) {
                throw new JmsBadMessageException("Invalid message type. Expecting BytesMessage");
            }
            BytesMessage booleanMessage = (BytesMessage) message;
            return booleanMessage.readBoolean();
        case BYTE:
            if (!(message instanceof BytesMessage)) {
                throw new JmsBadMessageException("Invalid message type. Expecting BytesMessage");
            }
            BytesMessage bytesMessage = (BytesMessage) message;
            return bytesMessage.readByte();
        case CHARACTER:
            if (!(message instanceof BytesMessage)) {
                throw new JmsBadMessageException("Invalid message type. Expecting BytesMessage");
            }
            BytesMessage charMessage = (BytesMessage) message;
            return charMessage.readChar();
        case DOUBLE:
            if (!(message instanceof BytesMessage)) {
                throw new JmsBadMessageException("Invalid message type. Expecting BytesMessage");
            }
            BytesMessage doubleMessage = (BytesMessage) message;
            return doubleMessage.readDouble();
        case FLOAT:
            if (!(message instanceof BytesMessage)) {
                throw new JmsBadMessageException("Invalid message type. Expecting BytesMessage");
            }
            BytesMessage floatMessage = (BytesMessage) message;
            return floatMessage.readFloat();
        case INTEGER:
            if (!(message instanceof BytesMessage)) {
                throw new JmsBadMessageException("Invalid message type. Expecting BytesMessage");
            }
            BytesMessage intMessage = (BytesMessage) message;
            return intMessage.readInt();
        case LONG:
            if (!(message instanceof BytesMessage)) {
                throw new JmsBadMessageException("Invalid message type. Expecting BytesMessage");
            }
            BytesMessage longMessage = (BytesMessage) message;
            return longMessage.readLong();
        case SHORT:
            if (!(message instanceof BytesMessage)) {
                throw new JmsBadMessageException("Invalid message type. Expecting BytesMessage");
            }
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
