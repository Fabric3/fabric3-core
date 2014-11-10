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
package org.fabric3.spi.model.physical;

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.contract.DataType;

/**
 * Used to attach the source side of a channel connection. The source may be a producer, channel binding or channel.
 */
public class PhysicalConnectionSourceDefinition extends PhysicalAttachPointDefinition {
    private static final long serialVersionUID = 3395589699751449558L;

    public static final int NO_SEQUENCE = 0;

    private int sequence = NO_SEQUENCE;
    private QName deployable;

    public PhysicalConnectionSourceDefinition() {
    }

    public PhysicalConnectionSourceDefinition(DataType... types) {
        super(types);
    }

    /**
     * Returns the sequence a consumer should be passed events, if supported by the channel type.
     *
     * @return the sequence a consumer should be passed events, if supported by the channel type
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * Sets the sequence a consumer should be passed events, if supported by the channel type.
     *
     * @param sequence the sequence a consumer should be passed events, if supported by the channel type.
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public QName getDeployable() {
        return deployable;
    }

    public void setDeployable(QName deployable) {
        this.deployable = deployable;
    }

}