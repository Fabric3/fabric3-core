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

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.spi.contribution.Capability;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidQNamePrefix;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.fabric3.spi.model.os.Library;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.fabric3.api.Namespaces.F3;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a contribution manifest from a contribution element
 */
@EagerInit
public class ContributionElementLoader implements TypeLoader<ContributionManifest> {
    private static final QName CONTRIBUTION = new QName(SCA_NS, "contribution");
    private static final QName DEPLOYABLE = new QName(SCA_NS, "deployable");
    private static final QName SCAN = new QName(F3, "scan");
    private static final QName PROVIDES_CAPABILITY = new QName(F3, "provides.capability");
    private static final QName REQUIRES_CAPABILITY = new QName(F3, "requires.capability");

    private final LoaderRegistry registry;

    public ContributionElementLoader(@Reference LoaderRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void start() {
        registry.registerLoader(CONTRIBUTION, this);
    }

    @Destroy
    public void stop() {
        registry.unregisterLoader(CONTRIBUTION);
    }

    public ContributionManifest load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        ContributionManifest manifest = new ContributionManifest();
        QName element = reader.getName();
        if (!CONTRIBUTION.equals(element)) {
            throw new AssertionError("Loader not positioned on the <contribution> element: " + element);
        }
        validateContributionAttributes(reader, context);
        boolean extension = Boolean.valueOf(reader.getAttributeValue(F3, "extension"));
        manifest.setExtension(extension);

        String contextPath = reader.getAttributeValue(F3, "context");
        manifest.setContext(contextPath);

        String description = reader.getAttributeValue(F3, "description");
        manifest.setDescription(description);

        while (true) {
            int event = reader.next();
            switch (event) {
                case START_ELEMENT:
                    element = reader.getName();
                    Location location = reader.getLocation();

                    if (DEPLOYABLE.equals(element)) {
                        validateDeployableAttributes(reader, context);
                        String name = reader.getAttributeValue(null, "composite");
                        if (name == null) {
                            MissingManifestAttribute failure = new MissingManifestAttribute("Composite attribute must be specified", location);
                            context.addError(failure);
                            return null;
                        }
                        QName qName;
                        // read qname but only set namespace if it is explicitly declared
                        int index = name.indexOf(':');
                        if (index != -1) {
                            String prefix = name.substring(0, index);
                            String localPart = name.substring(index + 1);
                            String ns = reader.getNamespaceContext().getNamespaceURI(prefix);
                            if (ns == null) {
                                URI uri = context.getContributionUri();
                                context.addError(new InvalidQNamePrefix(
                                        "The prefix " + prefix + " specified in the contribution manifest file for " + uri + " is invalid", location));
                                return null;
                            }
                            qName = new QName(ns, localPart, prefix);
                        } else {
                            qName = new QName(null, name);
                        }
                        List<RuntimeMode> runtimeModes = parseRuntimeModes(reader, context);
                        List<String> environments = parseEnvironments(reader);
                        Deployable deployable = new Deployable(qName, runtimeModes, environments);
                        manifest.addDeployable(deployable);
                    } else if (REQUIRES_CAPABILITY.equals(element)) {
                        parseRequiredCapabilities(manifest, reader, context);
                    } else if (PROVIDES_CAPABILITY.equals(element)) {
                        parseProvidedCapabilities(manifest, reader, context);
                    } else if (SCAN.equals(element)) {
                        validateScanAttributes(reader, context);
                        String excludeAttr = reader.getAttributeValue(null, "exclude");
                        if (excludeAttr == null) {
                            MissingAttribute error = new MissingAttribute("The exclude attribute must be set on the scan element", location);
                            context.addError(error);
                            continue;
                        }
                        String[] excludes = excludeAttr.split(",");
                        List<Pattern> patterns = new ArrayList<>();
                        for (String exclude : excludes) {
                            patterns.add(Pattern.compile(exclude));
                        }
                        manifest.setScanExcludes(patterns);

                    } else {
                        Object object = registry.load(reader, Object.class, context);
                        if (object instanceof Export) {
                            manifest.addExport((Export) object);
                        } else if (object instanceof Import) {
                            manifest.addImport((Import) object);
                        } else if (object instanceof ExtendsDeclaration) {
                            ExtendsDeclaration declaration = (ExtendsDeclaration) object;
                            manifest.addExtend(declaration.getName());
                        } else if (object instanceof ProvidesDeclaration) {
                            ProvidesDeclaration declaration = (ProvidesDeclaration) object;
                            manifest.addExtensionPoint(declaration.getName());
                        } else if (object instanceof Library) {
                            Library library = (Library) object;
                            manifest.addLibrary(library);
                        } else if (object != null) {
                            UnrecognizedElement failure = new UnrecognizedElement(reader, location);
                            context.addError(failure);
                            return null;
                        }
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (CONTRIBUTION.equals(reader.getName())) {
                        return manifest;
                    }
                    break;
            }
        }
    }

    private void parseProvidedCapabilities(ContributionManifest manifest, XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute error = new MissingAttribute("Capability name must be specified", location);
            context.addError(error);
            return;
        }
        Capability capability = new Capability(name);
        manifest.addProvidedCapability(capability);
    }

    private void parseRequiredCapabilities(ContributionManifest manifest, XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute error = new MissingAttribute("Capability name must be specified", location);
            context.addError(error);
            return;
        }
        boolean loaded = Boolean.valueOf(reader.getAttributeValue(null, "loaded"));
        Capability capability = new Capability(name, loaded);
        manifest.addRequiredCapability(capability);
    }

    private List<RuntimeMode> parseRuntimeModes(XMLStreamReader reader, IntrospectionContext context) {
        String modeAttr = reader.getAttributeValue(null, "modes");
        List<RuntimeMode> runtimeModes;
        if (modeAttr == null) {
            runtimeModes = Deployable.DEFAULT_MODES;
        } else {
            String[] modes = modeAttr.trim().split(" ");
            runtimeModes = new ArrayList<>();
            for (String mode : modes) {
                if ("vm".equals(mode)) {
                    runtimeModes.add(RuntimeMode.VM);
                } else if ("node".equals(mode)) {
                    runtimeModes.add(RuntimeMode.NODE);
                } else {
                    runtimeModes = Deployable.DEFAULT_MODES;
                    Location location = reader.getLocation();
                    InvalidValue error = new InvalidValue("Invalid mode attribute: " + modeAttr, location);
                    context.addError(error);
                    break;
                }
            }
        }
        return runtimeModes;
    }

    private List<String> parseEnvironments(XMLStreamReader reader) {
        String modeAttr = reader.getAttributeValue(null, "environments");
        if (modeAttr == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(modeAttr.trim().split(" "));
        }
    }

    private void validateContributionAttributes(XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"extension".equals(name) && !"description".equals(name) && !"context".equals(name) && !"capabilities".equals(name)
                && !"required-capabilities".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, location));
            }
        }
    }

    private void validateDeployableAttributes(XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"composite".equals(name) && !"modes".equals(name) && !"environments".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, location));
            }
        }
    }

    private void validateScanAttributes(XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"exclude".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, location));
            }
        }
    }

}
