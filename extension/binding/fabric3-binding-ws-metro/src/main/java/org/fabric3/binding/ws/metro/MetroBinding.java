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
package org.fabric3.binding.ws.metro;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;

/**
 * Reports the status of the Metro binding extension and sets the log level for the underlying Metro stack.
 *
 * @version $Rev$ $Date$
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