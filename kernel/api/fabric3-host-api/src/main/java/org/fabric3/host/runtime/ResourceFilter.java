package org.fabric3.host.runtime;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Filters resources against a collection of masks.
 *
 * @version $Rev$ $Date$
 */
public class ResourceFilter {
    private String[] resourceMasks;

    public ResourceFilter(String[] resourceMasks) {
        this.resourceMasks = resourceMasks;
    }

    public URL filterResource(URL url) {
        if (url == null || resourceMasks.length == 0) {
            return url;
        }
        String str = url.toString();
        for (String mask : resourceMasks) {
            if (str.contains(mask)) {
                return null;
            }
        }
        return url;
    }

    public Enumeration<URL> filterResources(Enumeration<URL> enumeration) {
        if (resourceMasks == null || enumeration == null || resourceMasks.length == 0) {
            return enumeration;
        }
        List<URL> resources = Collections.list(enumeration);
        List<URL> maskedResources = new ArrayList<URL>(resources.size());
        for (URL resource : resources) {
            String str = resource.toString();
            boolean toInclude = true;
            for (String mask : resourceMasks) {
                if (str.contains(mask)) {
                    toInclude = false;
                    break;
                }
            }
            if (toInclude) {
                maskedResources.add(resource);
            }
        }
        return Collections.enumeration(maskedResources);
    }

}
