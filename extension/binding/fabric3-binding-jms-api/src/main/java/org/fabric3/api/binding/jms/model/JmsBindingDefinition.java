/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.api.binding.jms.model;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.model.type.component.BindingDefinition;
import org.oasisopen.sca.Constants;

/**
 * Encapsulates JMS binding configuration specified in a composite.
 */
public class JmsBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = -1888120511695824132L;

    public static final QName BINDING_QNAME = new QName(Constants.SCA_NS, "binding.jms");
    private URI generatedTargetUri;
    private JmsBindingMetadata metadata;

    /**
     * Constructor.
     *
     * @param metadata the JMS metadata
     */
    public JmsBindingDefinition(JmsBindingMetadata metadata) {
        this(null, null, metadata);
    }

    /**
     * Constructor.
     *
     * @param bindingName the binding name
     * @param metadata    the JMS metadata
     */
    public JmsBindingDefinition(String bindingName, JmsBindingMetadata metadata) {
        this(bindingName, null, metadata);
    }

    /**
     * Constructor.
     *
     * @param bindingName the binding name
     * @param targetURI   the binding target URI
     * @param metadata    the JMS metadata to be initialized
     */
    public JmsBindingDefinition(String bindingName, URI targetURI, JmsBindingMetadata metadata) {
        super(bindingName, targetURI, BINDING_QNAME);
        this.metadata = metadata;
        addRequiredCapability("jms");
    }

    public JmsBindingMetadata getJmsMetadata() {
        return metadata;
    }

    public void setJmsMetadata(JmsBindingMetadata metadata) {
        this.metadata = metadata;
    }

    public void setGeneratedTargetUri(URI generatedTargetUri) {
        this.generatedTargetUri = generatedTargetUri;
    }

    @Override
    public URI getTargetUri() {
        return generatedTargetUri;
    }

}
