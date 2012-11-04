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
 */
public class MetroContributionServiceListener implements ContributionServiceListener {
    private JavaImport saajImport;
    private JavaImport wsImport;
    private JavaImport sunSaajImport;

    public MetroContributionServiceListener() {
        // JAX-WS classes
        PackageInfo wsInfo = new PackageInfo("javax.xml.ws.*");
        wsInfo.setMinVersion(new Version("2.2.0"));
        wsImport = new JavaImport(wsInfo);

        // SAAJ API and implementation
        PackageInfo saajInfo = new PackageInfo("javax.xml.soap.*");
        saajInfo.setMinVersion(new Version("1.3.0"));
        saajImport = new JavaImport(saajInfo);

        PackageInfo sunSaajInfo = new PackageInfo("com.sun.xml.messaging.saaj.*");
        sunSaajInfo.setMinVersion(new Version("2.0.1"));
        sunSaajImport = new JavaImport(sunSaajInfo);
    }

    public void onProcessManifest(Contribution contribution) {
        if (contribution.getManifest().isExtension()) {
            // the contribution is an extension
            return;
        }
        boolean saajImported = false;
        boolean wsImported = false;
        boolean sunImported = false;

        ContributionManifest manifest = contribution.getManifest();
        for (Import imprt : manifest.getImports()) {
            if (imprt instanceof JavaImport) {
                JavaImport contributionImport = (JavaImport) imprt;
                String name = contributionImport.getPackageInfo().getName();
                // check if already explicitly imported
                if (name.equals("javax.xml.soap.*")) {
                    saajImported = true;
                } else if (name.equals("javax.xml.ws.*")) {
                    wsImported = true;
                } else if (name.equals("com.sun.xml.messaging.saaj.*")) {
                    sunImported = true;
                }
            }
        }
        if (!saajImported) {
            manifest.addImport(saajImport);
        }
        if (!wsImported) {
            manifest.addImport(wsImport);
        }
        if (!sunImported) {
            manifest.addImport(sunSaajImport);
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
