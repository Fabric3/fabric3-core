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
package org.fabric3.admin.interpreter.parser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.ParseException;

public class ParserHelper {

    private ParserHelper() {
    }

    /**
     * Parses authorization parameters.
     *
     * @param command the command being parsed.
     * @param tokens  the input parameters in token form
     * @param index   the starting parameter index to parse
     * @throws ParseException if there is an error parsing the parameters
     */
    public static void parseAuthorization(Command command, String[] tokens, int index) throws ParseException {
        if ("-u".equals(tokens[index])) {
            command.setUsername(tokens[index + 1]);
        } else if ("-p".equals(tokens[index])) {
            command.setPassword(tokens[index + 1]);
        } else {
            throw new ParseException("Unrecognized parameter: " + tokens[index]);
        }
        if ("-u".equals(tokens[index + 2])) {
            command.setUsername(tokens[index + 3]);
        } else if ("-p".equals(tokens[index + 2])) {
            command.setPassword(tokens[index + 3]);
        } else {
            throw new ParseException("Unrecognized parameter: " + tokens[index + 2]);
        }

    }

    /**
     * Parses a URL input parameter.
     *
     * @param value the value to parse.
     * @return the URL
     * @throws MalformedURLException if the value is an invalid URL
     */
    public static URL parseUrl(String value) throws MalformedURLException {
        if (!value.contains(":/")) {
            // assume it is a file
            return new File(value).toURI().toURL();
        } else {
            return new URL(value);
        }
    }
}
