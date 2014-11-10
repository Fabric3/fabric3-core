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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.contribution.manifest;

/**
 * Parses OSGi headers, which are of the form:
 * <pre>
 *    header ::= clause ( , clause )
 *    clause ::= path ( ; path )*
 *              ( ; parameter ) *
 * </pre>
 */
public class OSGiManifestEntryParser {
    public enum EventType {
        /**
         * The parser enters the BEGIN state when its cursor is positioned at the first header character
         */
        BEGIN,

        /**
         * The parser enters the PATH state when an OSGi header path is read
         */
        PATH,

        /**
         * The parser enters the PARAMETER state when an OSGi header parameter is read
         */
        PARAMETER,

        /**
         * The parser enters the END_CLAUSE state when its cursor is position past the last character in the current clause
         */
        END_CLAUSE,

        /**
         * The parser enters the END state when cursor is positioned past the last header character
         */
        END
    }

    private static final char PARAMETER_SEPARATOR = ';';
    private static final char SEPARATOR = ',';

    // the OSGi header text
    private String header;

    // the current parser state
    private EventType state;

    // the internal character buffer
    private StringBuilder text;

    // the current position the parser is at reading the header
    private int pos = 0;

    // true if the parser is currently evaluating quoted text
    private boolean inQuote;

    /**
     * Constructor.
     *
     * @param header the OSGi header to parse
     */
    public OSGiManifestEntryParser(String header) {
        assert header != null;
        this.header = header;
        state = EventType.BEGIN;
    }

    /**
     * Advances the cursor to the next event.
     *
     * @return the event type.
     */
    public EventType next() {
        if (pos <= header.length() - 1) {
            if (EventType.END_CLAUSE == state) {
                // finished with the previous clause, fire the END_CLAUSE and reset to a new path event
                state = EventType.PATH;
                return EventType.END_CLAUSE;
            }
            if (EventType.PATH == state || EventType.PARAMETER == state) {
                text = null;
            }
            while (pos <= header.length() - 1) {
                char c = header.charAt(pos);
                ++pos;
                if (PARAMETER_SEPARATOR == c) {
                    inQuote = false;
                    if (EventType.PATH == state || EventType.BEGIN == state) {
                        state = EventType.PARAMETER;
                        return EventType.PATH;
                    }
                    return state;
                } else if (SEPARATOR == c) {
                    if (inQuote) {
                        appendNoWhiteSpace(c);

                    } else {
                        EventType current = state;
                        if (EventType.BEGIN == state) {
                            current = EventType.PATH;
                        }
                        state = EventType.END_CLAUSE;
                        return current;
                    }

                } else {
                    if (inQuote && c == '"') {
                        inQuote = false;
                    } else if (c == '"') {
                        inQuote = true;
                    }
                    appendNoWhiteSpace(c);
                    if (pos == header.length() && (state == EventType.BEGIN || state == EventType.PATH)) {
                        state = EventType.PATH;
                    }
                }
            }
            return state;
        } else {
            if (state == EventType.END_CLAUSE) {
                state = EventType.END;
            } else {
                // An END_CLAUSE event was not fired since it is the last parameter or path in the list. Force the event.
                state = EventType.END_CLAUSE;
            }
            return state;
        }
    }

    /**
     * Returns the text value for the current cursor position for EventType.PATH and EventType.PARAMETER events.
     *
     * @return the text value for the current cursor position
     * @throws IllegalStateException if the parser is not in the EventType.PATH or EventType.PARAMETER state.
     */
    public String getText() {
        // allow END_CLAUSE since we set the state in advance above when a ',' is found
        if (state != EventType.PATH && state != EventType.PARAMETER && state != EventType.END_CLAUSE) {
            throw new IllegalStateException("Invalid state:" + state);
        }
        return text.toString();
    }

    /**
     * Strips whitespace and newline characters.
     *
     * @param c the character to append to the current text buffer.
     */
    private void appendNoWhiteSpace(char c) {
        if (text == null) {
            text = new StringBuilder();
        }
        if (Character.isWhitespace(c) || c == '\n') {
            return;
        }
        text.append(c);
    }

}
