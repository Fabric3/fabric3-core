package org.fabric3.binding.ws.metro.contribution;

import org.fabric3.host.Version;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;

/**
 * Adds WS API exports to application contributions.
 *
 * @version $Rev$ $Date$
 */
public class MetroContributionServiceListener implements ContributionServiceListener {
    private JavaImport saajImport;
    private JavaImport wsImport;

    public MetroContributionServiceListener() {
        PackageInfo saajInfo = new PackageInfo("javax.xml.soap.*");
        saajInfo.setMinVersion(new Version("1.3.0"));
        saajImport = new JavaImport(saajInfo);
        PackageInfo wsInfo = new PackageInfo("javax.xml.ws.*");
        wsInfo.setMinVersion(new Version("2.2.0"));
        wsImport = new JavaImport(wsInfo);
    }

    public void onProcessManifest(Contribution contribution) {
        if (contribution.getManifest().isExtension()) {
            // the contribution is an extension
            return;
        }
        boolean saajImported = false;
        boolean wsImported = false;
        ContributionManifest manifest = contribution.getManifest();
        for (Import imprt : manifest.getImports()) {
            if (imprt instanceof JavaImport) {
                JavaImport contributionImport = (JavaImport) imprt;
                String name = contributionImport.getPackageInfo().getName();
                if (name.equals("javax.xml.soap.*")) {
                    // already explicitly imported
                    saajImported = true;
                } else if (name.equals("javax.xml.ws.*")) {
                    // already explicitly imported
                    wsImported = true;
                }
            }
        }
        if (!saajImported) {
            manifest.addImport(saajImport);
        }
        if (!wsImported) {
            manifest.addImport(wsImport);
        }
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
