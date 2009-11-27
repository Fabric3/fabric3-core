/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.exist.transform;

import org.exist.xquery.value.Sequence;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

 /** 
 * @version $Rev$ $Date$
 */
@EagerInit
public abstract class AbstractTransformer <A,B extends Sequence>implements Transformer <A,B >{
    TransformerRegistry registry;
    /**
     * Set Registry
     * @param registry
     */
    @Reference
    public void setRegistry(TransformerRegistry registry) {
        this.registry = registry;
    }

    /** Register Transformer*/
    @Init
    public void init() {
        registry.register(source(),this);
        registry.register(valueType(), this);
    }

    /** Unregister Registry*/
    @Destroy
    public void destroy() {
        registry.unregister(source(),this);
        registry.unregister(valueType(), this);
    }

}
