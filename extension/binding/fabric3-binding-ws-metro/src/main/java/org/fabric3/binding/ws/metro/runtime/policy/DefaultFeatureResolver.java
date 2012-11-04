/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.binding.ws.metro.runtime.policy;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;

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
        List<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
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
            features.add(createBindingFeature(SOAPBindingImpl.SOAP12HTTP_BINDING));
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
