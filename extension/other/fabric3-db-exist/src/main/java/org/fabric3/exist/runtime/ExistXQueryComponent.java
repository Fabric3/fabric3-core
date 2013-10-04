/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.exist.runtime;

import java.net.URI;
import javax.xml.namespace.QName;

import org.exist.EXistException;
import org.exist.xquery.XPathException;
import org.fabric3.host.Fabric3RuntimeException;
import org.fabric3.spi.Lifecycle;
import org.fabric3.spi.container.builder.WiringException;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.xquery.runtime.XQueryComponent;

/**
 * A component whose implementation is a web applicaiton.
 */
public class ExistXQueryComponent extends XQueryComponent implements Lifecycle {

   
   
    private ExistXQueryCompiler compiler;

    public ExistXQueryComponent(URI uri,
            QName groupId,
            ExistXQueryCompiler compiler) {
        super(uri, groupId);
        this.compiler=compiler;
       
    }

    @Override
    public void start() throws Fabric3RuntimeException {
        try {
            compiler.compile();
        } catch (XPathException ex) {
            throw new Fabric3RuntimeException(ex) {
            };
        } catch (EXistException ex) {
            throw new Fabric3RuntimeException(ex) {
            };
        }
        compiler=null;
        super.start();
    }

    public void attachSourceWire(String name, InteractionType interactionType,String callbackUri, Wire wire) throws WiringException {
        compiler.linkSourceWire(name, interactionType, callbackUri, wire);

    }

    public void attachTargetWire(String name, InteractionType interactionType, Wire wire) throws WiringException {
        compiler.linkTargetWire(name, interactionType, wire);

    }

   
}
