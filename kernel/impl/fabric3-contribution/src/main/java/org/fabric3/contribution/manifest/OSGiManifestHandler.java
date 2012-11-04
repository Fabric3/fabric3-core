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
package org.fabric3.contribution.manifest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.host.Version;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.manifest.JarManifestHandler;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Parses OSGi manifest headers and adds the metadata to the SCA contribution manifest.
 */
@EagerInit
public class OSGiManifestHandler implements JarManifestHandler {
    private static final String IMPORT_HEADER = "Import-Package";
    private static final String EXPORT_HEADER = "Export-Package";
    private static final String VERSION = "version=";
    private static final String RESOLUTION = "resolution:=";

    public void processManifest(ContributionManifest contributionManifest, Manifest jarManifest, IntrospectionContext context) {
        Attributes attributes = jarManifest.getMainAttributes();
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(IMPORT_HEADER)) {
                List<JavaImport> imports = parseImportHeader(entry.getValue().toString(), context);
                if (imports == null) {
                    // error
                    return;
                }
                for (JavaImport imprt : imports) {
                    contributionManifest.addImport(imprt);
                }
            } else if (entry.getKey().toString().equalsIgnoreCase(EXPORT_HEADER)) {
                List<JavaExport> exports = parseExportHeader(entry.getValue().toString(), context);
                if (exports == null) {
                    // error
                    return;
                }
                for (JavaExport export : exports) {
                    contributionManifest.addExport(export);
                }

            }
        }
    }

    private List<JavaImport> parseImportHeader(String header, IntrospectionContext context) {
        OSGiManifestEntryParser parser = new OSGiManifestEntryParser(header);
        List<JavaImport> imports = new ArrayList<JavaImport>();
        PackageInfo info = null;
        while (true) {
            OSGiManifestEntryParser.EventType type = parser.next();
            switch (type) {
            case BEGIN:
                break;
            case PATH:
                info = new PackageInfo(parser.getText());
                break;
            case PARAMETER:
                String text = parser.getText();
                if (text.startsWith(VERSION)) {
                    if (!parseVersion(text, info, context)) {
                        return null;
                    }
                } else if (text.startsWith(RESOLUTION)) {
                    String val = text.substring(RESOLUTION.length());
                    info.setRequired("required".equalsIgnoreCase(val));
                }

                break;
            case END_CLAUSE:
                if (info != null) {
                    JavaImport imprt = new JavaImport(info);
                    imports.add(imprt);
                }
                break;
            case END:
                return imports;
            }
        }
    }

    private List<JavaExport> parseExportHeader(String header, IntrospectionContext context) {
        OSGiManifestEntryParser parser = new OSGiManifestEntryParser(header);
        List<JavaExport> exports = new ArrayList<JavaExport>();
        PackageInfo info = null;
        while (true) {
            OSGiManifestEntryParser.EventType type = parser.next();
            switch (type) {
            case BEGIN:
                break;
            case PATH:
                info = new PackageInfo(parser.getText());
                break;
            case PARAMETER:
                String text = parser.getText();
                if (text.startsWith(VERSION)) {
                    if (!parseVersion(text, info, context)) {
                        return null;
                    }
                }
                break;
            case END_CLAUSE:
                JavaExport export = new JavaExport(info);
                exports.add(export);
                break;
            case END:
                return exports;
            }
        }
    }

    private boolean parseVersion(String text, PackageInfo info, IntrospectionContext context) {
        String val = text.substring(VERSION.length());
        if (val.startsWith("\"[")) {
            info.setMinInclusive(true);
            val = val.substring(2);
        } else if (val.startsWith("\"(")) {
            info.setMinInclusive(false);
            val = val.substring(2);
        } else if (val.startsWith("\"")) {
            // strip quote
            val = val.substring(1);
        }
        if (val.endsWith("]\"")) {
            info.setMaxInclusive(true);
            val = val.substring(0, val.length() - 2);
        } else if (val.endsWith(")\"")) {
            info.setMaxInclusive(false);
            val = val.substring(0, val.length() - 2);
        } else if (val.endsWith("\"")) {
            // strip quote
            val = val.substring(0, val.length() - 1);
        }
        String[] versions = val.split(",");
        try {
            Version packageVersion = new Version(versions[0]);
            info.setMinVersion(packageVersion);
            if (versions.length == 2) {
                packageVersion = new Version(versions[1]);
                info.setMaxVersion(packageVersion);
            }
        } catch (IllegalArgumentException e) {
            InvalidOSGiManifest failure = new InvalidOSGiManifest("Invalid version " + versions[0] + " in: " + text, e);
            context.addError(failure);
            return false;
        }
        return true;
    }


}
