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
package org.fabric3.contribution.manifest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.contribution.Deployable;
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

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.fabric3.host.Namespaces.F3;
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
                        MissingManifestAttribute failure =
                                new MissingManifestAttribute("Composite attribute must be specified", location);
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
                            context.addError(new InvalidQNamePrefix("The prefix " + prefix + " specified in the contribution manifest file for "
                                                                            + uri + " is invalid", location));
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
                    List<Pattern> patterns = new ArrayList<Pattern>();
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
            runtimeModes = new ArrayList<RuntimeMode>();
            for (String mode : modes) {
                if ("controller".equals(mode)) {
                    runtimeModes.add(RuntimeMode.CONTROLLER);
                } else if ("participant".equals(mode)) {
                    runtimeModes.add(RuntimeMode.PARTICIPANT);
                } else if ("vm".equals(mode)) {
                    runtimeModes.add(RuntimeMode.VM);
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
            if (!"extension".equals(name) && !"description".equals(name) && !"capabilities".equals(name) && !"required-capabilities".equals(name)) {
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
