/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.binding.ftp.introspection;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.ftp.model.FtpBindingDefinition;
import org.fabric3.binding.ftp.model.TransferMode;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;

/**
 *
 */
public class FtpBindingLoader extends AbstractValidatingTypeLoader<FtpBindingDefinition> {
    private final LoaderHelper loaderHelper;

    /**
     * Constructor.
     *
     * @param loaderHelper the policy helper
     */
    public FtpBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
        addAttributes("uri", "requires", "policySets", "mode", "tmpFileSuffix");
    }

    public FtpBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        FtpBindingDefinition bd = null;
        String uri = null;

        try {

            uri = reader.getAttributeValue(null, "uri");
            String transferMode = reader.getAttributeValue(null, "mode");
            if (uri == null) {
                MissingAttribute failure = new MissingAttribute("A binding URI must be specified ", startLocation);
                introspectionContext.addError(failure);
                return null;
            }
            if (!uri.startsWith("ftp://") && !uri.startsWith("FTP://")) {
                uri = "ftp://" + uri;
            }
            TransferMode tMode = transferMode != null ? TransferMode.valueOf(transferMode) : TransferMode.PASSIVE;
            URI endpointUri = new URI(uri);
            bd = new FtpBindingDefinition(endpointUri, tMode);

            String tmpFileSuffix = reader.getAttributeValue(null, "tmpFileSuffix");
            if (tmpFileSuffix != null) {
                bd.setTmpFileSuffix(tmpFileSuffix);
            }

            loaderHelper.loadPolicySetsAndIntents(bd, reader, introspectionContext);

            validateAttributes(reader, introspectionContext, bd);

            while (true) {
                switch (reader.next()) {
                case XMLStreamConstants.END_ELEMENT:
                    if ("binding.ftp".equals(reader.getName().getLocalPart())) {
                        return bd;
                    }
                case XMLStreamConstants.START_ELEMENT:
                    if ("commands".equals(reader.getName().getLocalPart())) {
                        boolean success = parseCommands(bd, reader, introspectionContext);
                        if (!success) {
                            while (true) {
                                // position the curser at the end of the binding.ftp entry
                                LoaderUtil.skipToEndElement(reader);
                                if ("binding.ftp".equals(reader.getName().getLocalPart())) {
                                    return bd;
                                }
                            }
                        }
                    }
                }

            }

        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("The FTP binding URI is not valid: " + uri, startLocation);
            introspectionContext.addError(failure);
        }

        LoaderUtil.skipToEndElement(reader);
        return bd;

    }

    private boolean parseCommands(FtpBindingDefinition bd, XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        while (true) {
            switch (reader.nextTag()) {
            case XMLStreamConstants.END_ELEMENT:
                if ("commands".equals(reader.getName().getLocalPart())) {
                    return true;
                }
                break;
            case XMLStreamConstants.START_ELEMENT:
                Location location = reader.getLocation();
                if ("command".equals(reader.getName().getLocalPart())) {
                    reader.next();
                    bd.addSTORCommand(reader.getText());
                } else {
                    UnrecognizedElement error = new UnrecognizedElement(reader, location, bd);
                    context.addError(error);
                    return false;
                }
            }
        }
    }

}
