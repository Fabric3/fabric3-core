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
