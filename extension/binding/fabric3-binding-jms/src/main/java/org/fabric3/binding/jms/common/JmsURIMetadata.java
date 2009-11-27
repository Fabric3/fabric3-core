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
package org.fabric3.binding.jms.common;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @version $Revision$ $Date$
 */
public class JmsURIMetadata {
    public final static String CONNECTIONFACORYNAME = "connectionFactoryName";
    public final static String DESTINATIONTYPE = "destinationType";
    public final static String DELIVERYMODE = "deliveryMode";
    public final static String TIMETOLIVE = "timeToLive";
    public final static String PRIORITY = "priority";
    public final static String RESPONSEDESTINAT = "responseDestination";

    private String destination;
    private Map<String, String> properties;

    public String getDestination() {
        return destination;
    }

    private JmsURIMetadata(String destination) {
        this.destination = destination;
        properties = new HashMap<String, String>();
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Parses metadata from an input string.
     *
     * @param uri string for /binding.jms/@uri
     * @return a JmsURIMetadata
     * @throws URISyntaxException when the URI is not a valid format required by /binding.jms/@uri.
     */
    public static JmsURIMetadata parseURI(String uri) throws URISyntaxException {
        //TODO implement better validation
        boolean matches = Pattern.matches(
                "jms:(.*?)[\\?(.*?)=(.*?)((&(.*?)=(.*?))*)]?", uri);
        if (!matches) {
            throw new URISyntaxException(uri, "Not a valid URI format for binding.jms");
        }
        return doParse(uri);
    }

    private static JmsURIMetadata doParse(String uri) {
        StringTokenizer token = new StringTokenizer(uri, ":?=&");
        String current;
        String propertyName = null;
        int pos = 0;
        JmsURIMetadata result = null;
        while (token.hasMoreTokens()) {
            current = token.nextToken();
            if (1 == pos) {
                result = new JmsURIMetadata(current);
            } else if (pos % 2 == 0) {
                propertyName = current;
            } else if (0 != pos) {// ignore beginning 'jms'
                assert propertyName != null;
                assert result != null;
                result.properties.put(propertyName.trim(), current.trim());
            }
            pos++;
        }
        return result;
    }

}
