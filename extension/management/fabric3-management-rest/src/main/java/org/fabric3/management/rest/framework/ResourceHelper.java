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
package org.fabric3.management.rest.framework;

import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;

import org.fabric3.management.rest.model.SelfLink;

/**
 * Framework helper methods.
 */
public final class ResourceHelper {

    private ResourceHelper() {
    }

    /**
     * Creates a resource self link.
     *
     * @param request the current HTTP request
     * @return the self link
     */
    public static SelfLink createSelfLink(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        URL selfHref = ResourceHelper.createUrl(requestUrl);
        return new SelfLink(selfHref);
    }

    /**
     * Suppresses {@link MalformedURLException} when converting a String to a URL.
     *
     * @param url the URL as a String
     * @return the URL
     */
    public static URL createUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns a request URL as a string with the trailing "/" removed.
     *
     * @param request the current HTTP request
     * @return the request URL as a String
     */
    public static String getRequestUrl(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        if (requestUrl.endsWith("/") && requestUrl.length() > 1) {
            requestUrl = requestUrl.substring(0, requestUrl.length() - 1);
        }
        return requestUrl;
    }


}
