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
 */
package org.fabric3.contribution.listener;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionServiceListener;
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
        imports.forEach(manifest::addImport);
    }

    public void onInstall(Contribution contribution) {
        ContributionManifest manifest = contribution.getManifest();
        if (!manifest.isExtension()) {
            return;
        }
        manifest.getExports().stream().filter(export -> export instanceof JavaExport).forEach(export -> {
            JavaExport javaExport = (JavaExport) export;
            String name = javaExport.getPackageInfo().getName();
            if (name.startsWith(BINDING_PACKAGE) || name.startsWith(IMPLEMENTATION_PACKAGE)) {
                JavaImport imprt = new JavaImport(javaExport.getPackageInfo());
                imports.add(imprt);
            }
        });
    }

    public void onUpdate(Contribution contribution) {
    }

    public void onUninstall(Contribution contribution) {
        ContributionManifest manifest = contribution.getManifest();
        if (!manifest.isExtension()) {
            return;
        }
        manifest.getExports().stream().filter(export -> export instanceof JavaExport).forEach(export -> {
            JavaExport javaExport = (JavaExport) export;
            String name = javaExport.getPackageInfo().getName();
            if (name.startsWith(BINDING_PACKAGE) || name.startsWith(IMPLEMENTATION_PACKAGE)) {
                JavaImport imprt = new JavaImport(javaExport.getPackageInfo());
                imports.remove(imprt);
            }
        });
    }

    public void onRemove(Contribution contribution) {
    }
}
