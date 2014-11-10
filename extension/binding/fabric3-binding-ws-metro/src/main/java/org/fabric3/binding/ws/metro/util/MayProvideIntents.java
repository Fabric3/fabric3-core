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

import org.oasisopen.sca.Constants;

/**
 * List of intents that may be provided by Metro.
 */
public class MayProvideIntents {

    public static QName MESSAGE_OPTIMIZATION = new QName(org.fabric3.api.Namespaces.F3, "messageOptimization");

    public static QName SOAP1_1 = new QName(Constants.SCA_NS, "SOAP.1_1");
    public static QName SOAP1_2 = new QName(Constants.SCA_NS, "SOAP.1_2");
    public static QName SOAPV1_1 = new QName(Constants.SCA_NS, "SOAP.v1_1");
    public static QName SOAPV1_2 = new QName(Constants.SCA_NS, "SOAP.v1_2");
    public static QName X_SOAP1_2 = new QName(org.fabric3.api.Namespaces.F3, "xsoap12");
    public static QName REST = new QName(org.fabric3.api.Namespaces.F3, "metroRest");

    public static QName AT_LEAST_ONCE = new QName(Constants.SCA_NS, "atLeastOnce");
    public static QName AT_MOST_ONCE = new QName(Constants.SCA_NS, "atMostOnce");
    public static QName EXACTLY_ONCE = new QName(Constants.SCA_NS, "exactlyOnce");

    public static QName SCHEMA_VALIDATION = new QName(org.fabric3.api.Namespaces.F3, "schemaValidation");

    /**
     * Private constructor for constant class.
     */
    private MayProvideIntents() {
    }

}
