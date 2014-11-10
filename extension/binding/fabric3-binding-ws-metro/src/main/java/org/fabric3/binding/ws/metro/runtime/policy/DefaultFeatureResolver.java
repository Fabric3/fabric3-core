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
package org.fabric3.binding.ws.metro.runtime.policy;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import com.sun.xml.ws.binding.SOAPBindingImpl;
import com.sun.xml.ws.developer.BindingTypeFeature;
import com.sun.xml.ws.developer.JAXWSProperties;
import org.fabric3.binding.ws.metro.util.MayProvideIntents;

/**
 * Default implementation of FeatureResolver.
 */
public class DefaultFeatureResolver implements FeatureResolver {
    private Field field;

    public DefaultFeatureResolver() {
        try {
            field = WebServiceFeature.class.getDeclaredField("enabled");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Translates the requested intents to web service features.
     *
     * @param requestedIntents Requested intents.
     * @return Resolved feature sets.
     */
    public WebServiceFeature[] getFeatures(List<QName> requestedIntents) {
        List<WebServiceFeature> features = new LinkedList<>();
        if (requestedIntents.contains(MayProvideIntents.MESSAGE_OPTIMIZATION)) {
            features.add(new MTOMFeature());
        }
        resolveBinding(requestedIntents, features);
        features.add(new AddressingFeature());
        WebServiceFeature[] webServiceFeatures = new WebServiceFeature[features.size()];
        webServiceFeatures = features.toArray(webServiceFeatures);
        return webServiceFeatures;
    }

    private void resolveBinding(List<QName> requestedIntents, List<WebServiceFeature> features) {
        if (requestedIntents.contains(MayProvideIntents.SOAP1_1)) {
            features.add(createBindingFeature(SOAPBinding.SOAP11HTTP_BINDING));
        } else if (requestedIntents.contains(MayProvideIntents.SOAP1_2)) {
            features.add(createBindingFeature(SOAPBindingImpl.X_SOAP12HTTP_BINDING));
        } else if (requestedIntents.contains(MayProvideIntents.SOAPV1_1)) {
            features.add(createBindingFeature(SOAPBinding.SOAP11HTTP_BINDING));
        } else if (requestedIntents.contains(MayProvideIntents.SOAPV1_2)) {
            features.add(createBindingFeature(SOAPBindingImpl.X_SOAP12HTTP_BINDING));
        } else if (requestedIntents.contains(MayProvideIntents.X_SOAP1_2)) {
            features.add(createBindingFeature(SOAPBindingImpl.X_SOAP12HTTP_BINDING));
        } else if (requestedIntents.contains(MayProvideIntents.REST)) {
            features.add(createBindingFeature(JAXWSProperties.REST_BINDING));
        }
    }

    private BindingTypeFeature createBindingFeature(String bindingQName) {
        BindingTypeFeature feature = new BindingTypeFeature(bindingQName);
        // hack to enable the protected field that is not properly set
        try {
            field.set(feature, true);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
        return feature;
    }

}
