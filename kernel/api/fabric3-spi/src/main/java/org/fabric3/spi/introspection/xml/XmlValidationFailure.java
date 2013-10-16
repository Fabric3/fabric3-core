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
package org.fabric3.spi.introspection.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.stream.Location;

import org.fabric3.host.failure.ValidationFailure;
import org.fabric3.api.model.type.ModelObject;

/**
 * Base class for validation failures occurring in XML artifacts.
 */
public abstract class XmlValidationFailure extends ValidationFailure {
    private int line = -1;
    private int column = -1;
    private int offset = -1;
    private String resourceURI = "system";
    private String message;

    private List<Object> sources;

    protected XmlValidationFailure(String message, Location location, ModelObject... sources) {
        this.message = message;
        if (location != null) {
            line = location.getLineNumber();
            column = location.getColumnNumber();
            offset = location.getCharacterOffset();
            resourceURI = location.getSystemId();
        }
        this.sources = new ArrayList<Object>();
        if (sources != null) {
            this.sources.addAll(Arrays.asList(sources));
        }
    }

    protected XmlValidationFailure(String message) {
        this(message, null);
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getOffset() {
        return offset;
    }

    public String getResourceURI() {
        return resourceURI;
    }

    public List<Object> getSources() {
        return sources;
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append(message);
        if (line != -1) {
            builder.append(" [").append(line).append(',').append(column).append("]");
        }
        return builder.toString();
    }

    public String getShortMessage() {
        return message;
    }

}
