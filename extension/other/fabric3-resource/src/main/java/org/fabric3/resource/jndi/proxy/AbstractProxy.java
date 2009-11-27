/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.resource.jndi.proxy;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class AbstractProxy<D> {

    private static final String COMP_ENV = "java:comp/env/";

    private D delegate;
    private String jndiName;
    private String providerUrl;
    private String initialContextFactory;
    private boolean env;

    /**
     * Gets a reference to the delegate that is proxied.
     *
     * @return Delegate that is proxied.
     */
    protected D getDelegate() {
        return delegate;
    }

    @Init
    @SuppressWarnings("unchecked")
    public void init() throws NamingException {

        Context ctx = null;

        try {

            Properties prop = new Properties();
            if (providerUrl != null) {
                prop.put(Context.PROVIDER_URL, providerUrl);
            }
            if (initialContextFactory != null) {
                prop.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
            }

            if (env) {
                jndiName = COMP_ENV + jndiName;
            }

            ctx = new InitialContext(prop);

            delegate = (D) ctx.lookup(jndiName);

        } finally {
            if (ctx != null) {
                ctx.close();
            }
        }

    }

    /**
     * @param jndiName the jndiName to set
     */
    @Property(required = true)
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    /**
     * @param providerUrl the providerUrl to set
     */
    @Property
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    /**
     * @param initialContextFactory the initialContextFactory to set
     */
    @Property
    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    /**
     * @param env the env to set
     */
    @Property
    public void setEnv(boolean env) {
        this.env = env;
    }


}
