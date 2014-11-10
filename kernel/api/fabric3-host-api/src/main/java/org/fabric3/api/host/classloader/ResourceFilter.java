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
package org.fabric3.api.host.classloader;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Filters resources against a collection of masks.
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
        List<URL> maskedResources = new ArrayList<>(resources.size());
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
