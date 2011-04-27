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
package org.fabric3.contribution.manifest;

/**
 * Parses OSGi headers, which are of the form:
 * <pre>
 *    header ::= clause ( Õ,Õ clause )
 *    clause ::= path ( Õ;Õ path ) *
 *                 ( Õ;Õ parameter ) *
 * </pre>
 *
 * @version $Rev$ $Date$
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
                    if (pos == header.length() && state == EventType.BEGIN) {
                        state = EventType.PATH;
//                        return EventType.END_CLAUSE;
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
