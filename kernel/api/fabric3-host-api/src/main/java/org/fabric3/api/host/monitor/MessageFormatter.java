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
package org.fabric3.api.host.monitor;

import java.text.MessageFormat;

/**
 * Formats a monitor event message, performing variable substitution if required.
 */
public class MessageFormatter {

    public static String format(String message, Object[] args) {
        if (message == null) {
            StringBuilder builder = new StringBuilder();
            for (Object arg : args) {
                builder.append(arg).append(" ");
            }
            return builder.toString();
        }
        if (args != null && args.length != 0) {
            if (message.contains("{0") || message.contains("{1") || message.contains("{2") || message.contains("{3")) {
                return MessageFormat.format(message, args);
            }
        }
        return message;
    }

    private MessageFormatter() {
    }
}
