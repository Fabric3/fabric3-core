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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
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
