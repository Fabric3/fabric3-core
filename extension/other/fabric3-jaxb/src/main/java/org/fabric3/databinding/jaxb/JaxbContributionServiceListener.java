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
 *
 * @version $Rev$ $Date$
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
