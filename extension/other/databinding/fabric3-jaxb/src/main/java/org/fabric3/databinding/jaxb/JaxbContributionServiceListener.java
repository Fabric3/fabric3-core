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
package org.fabric3.databinding.jaxb;

import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;

/**
 * Adds implicit imports for JAXB implementation classes to user contributions since <code>JAXBContext</code> searches the TCCL, which is set to the
 * contribution classloader during runtime execution of user code.
 */
public class JaxbContributionServiceListener implements ContributionServiceListener {
    private JavaImport jaxbImport;
    private JavaImport iStackImport;
    private JavaImport xsomImport;
    private JavaImport txw2Import;
    private JavaImport dtdParserImport;
    private JavaImport utilImport;

    public JaxbContributionServiceListener() {
        PackageInfo jaxbInfo = new PackageInfo("com.sun.xml.bind.*");
        jaxbImport = new JavaImport(jaxbInfo);
        PackageInfo istackInfo = new PackageInfo("com.sun.istack.*");
        iStackImport = new JavaImport(istackInfo);
        PackageInfo xsominfo = new PackageInfo("com.sun.xml.xsom.*");
        xsomImport = new JavaImport(xsominfo);
        PackageInfo txw2Info = new PackageInfo("com.sun.xml.txw2.*");
        txw2Import = new JavaImport(txw2Info);
        PackageInfo dtdParserInfo = new PackageInfo("com.sun.xml.dtdparser.*");
        dtdParserImport = new JavaImport(dtdParserInfo);
        PackageInfo utilInfo = new PackageInfo("com.sun.xml.util.*");
        utilImport = new JavaImport(utilInfo);
    }

    public void onProcessManifest(Contribution contribution) {
        ContributionManifest manifest = contribution.getManifest();
        if (manifest.isExtension()) {
            // extensions should manually enable JAXB
            return;
        }
        manifest.addImport(jaxbImport);
        manifest.addImport(iStackImport);
        manifest.addImport(xsomImport);
        manifest.addImport(txw2Import);
        manifest.addImport(dtdParserImport);
        manifest.addImport(utilImport);
    }

    public void onStore(Contribution contribution) {
        // no-op
    }

    public void onInstall(Contribution contribution) {
        // no-op
    }

    public void onUpdate(Contribution contribution) {
        // no-op
    }

    public void onUninstall(Contribution contribution) {
        // no-op
    }

    public void onRemove(Contribution contribution) {
        // no-op
    }
}
