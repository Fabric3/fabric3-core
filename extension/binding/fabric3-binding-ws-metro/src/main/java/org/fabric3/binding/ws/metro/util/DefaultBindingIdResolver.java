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
package org.fabric3.binding.ws.metro.util;

import javax.xml.namespace.QName;
import java.util.List;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.developer.JAXWSProperties;

/**
 * Default implementation of the binding Id resolver.
 */
public class DefaultBindingIdResolver implements BindingIdResolver {

    /**
     * Resolves bindings based on the requested intents and policy sets.
     *
     * @param requestedIntents Intents requested on the bindings.
     * @return Resolved binding Id.
     */
    public BindingID resolveBindingId(List<QName> requestedIntents) {

        BindingID bindingID = BindingID.SOAP11_HTTP;
        if (requestedIntents.contains(MayProvideIntents.SOAP1_2) || requestedIntents.contains(MayProvideIntents.SOAPV1_2)) {
            bindingID = BindingID.X_SOAP12_HTTP;
        } else if (requestedIntents.contains(MayProvideIntents.X_SOAP1_2)) {
            bindingID = BindingID.X_SOAP12_HTTP;
        } else if (requestedIntents.contains(MayProvideIntents.REST)) {
            bindingID = BindingID.parse(JAXWSProperties.REST_BINDING);
        }

        return bindingID;

    }

}
