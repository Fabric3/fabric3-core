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
package org.fabric3.contribution.listener;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Automatically imports installed binding and implementation API packages in user contributions. This avoids requiring user contributions to manually import
 * API packages.
 */
@EagerInit
public class APIImportListener implements ContributionServiceListener {
    public static final String BINDING_PACKAGE = "org.fabric3.api.binding.";
    public static final String IMPLEMENTATION_PACKAGE = "org.fabric3.api.implementation.";

    private List<JavaImport> imports = new ArrayList<>();

    public void onStore(Contribution contribution) {
    }

    public void onProcessManifest(Contribution contribution) {
        ContributionManifest manifest = contribution.getManifest();
        if (manifest.isExtension()) {
            return;
        }
        for (JavaImport imprt : imports) {
            manifest.addImport(imprt);
        }
    }

    public void onInstall(Contribution contribution) {
        ContributionManifest manifest = contribution.getManifest();
        if (!manifest.isExtension()) {
            return;
        }
        for (Export export : manifest.getExports()) {
            if (export instanceof JavaExport) {
                JavaExport javaExport = (JavaExport) export;
                String name = javaExport.getPackageInfo().getName();
                if (name.startsWith(BINDING_PACKAGE) || name.startsWith(IMPLEMENTATION_PACKAGE)) {
                    JavaImport imprt = new JavaImport(javaExport.getPackageInfo());
                    imports.add(imprt);
                }
            }
        }
    }

    public void onUpdate(Contribution contribution) {
    }

    public void onUninstall(Contribution contribution) {
        ContributionManifest manifest = contribution.getManifest();
        if (!manifest.isExtension()) {
            return;
        }
        for (Export export : manifest.getExports()) {
            if (export instanceof JavaExport) {
                JavaExport javaExport = (JavaExport) export;
                String name = javaExport.getPackageInfo().getName();
                if (name.startsWith(BINDING_PACKAGE) || name.startsWith(IMPLEMENTATION_PACKAGE)) {
                    JavaImport imprt = new JavaImport(javaExport.getPackageInfo());
                    imports.remove(imprt);
                }
            }
        }
    }

    public void onRemove(Contribution contribution) {
    }
}
