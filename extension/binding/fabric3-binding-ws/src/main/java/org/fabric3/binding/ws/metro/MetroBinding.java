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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.ws.metro;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;

/**
 * Reports the status of the Metro binding extension and sets the log level for the underlying Metro stack.
 */
@EagerInit
public class MetroBinding {
    private static final String META_FACTORY_CLASS_PROPERTY = "javax.xml.soap.MetaFactory";
    private static final String MESSAGE_FACTORY_CLASS_PROPERTY = "javax.xml.soap.MessageFactory";

    private Level logLevel = Level.WARNING;

    @Property(required = false)
    public void setLogLevel(String logLevel) {
        this.logLevel = Level.parse(logLevel);
    }

    @Init
    public void init() {
        // setup the SAAJ implementation
        String vm = System.getProperty("java.vm.name");
        if (vm != null && vm.contains("IBM J9 VM")) {
            // the Metro SAAJ implementation is incompatible with the IBM J9 VM, use the one provided by J9 instead
            System.setProperty(META_FACTORY_CLASS_PROPERTY, "com.sun.xml.internal.messaging.saaj.soap.SAAJMetaFactoryImpl");
            System.setProperty(MESSAGE_FACTORY_CLASS_PROPERTY, "com.sun.xml.internal.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl");
        } else {
            // set the SAAJ implementation to the one provided by Metro
            System.setProperty(META_FACTORY_CLASS_PROPERTY, "com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");
            System.setProperty(MESSAGE_FACTORY_CLASS_PROPERTY, "com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl");
        }

        // turn down Metro logging
        Logger.getLogger("javax.enterprise.resource.webservices").setLevel(logLevel);
        // turn monitoring off as management is handled by the Fabric3 JMX infrastructure
        System.setProperty("com.sun.xml.ws.monitoring.endpoint", "false");
    }


}