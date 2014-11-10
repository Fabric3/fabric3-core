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
package org.fabric3.runtime.weblogic.federation;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * Helper class for JNDI operations.
 */
public final class JndiHelper {

    private JndiHelper() {
    }

    /**
     * Returns the named subcontext, creating it if it does not exist.
     *
     * @param name   the subcontext name
     * @param parent the parent context
     * @return the subcontext
     * @throws NamingException if there is an error resolving or creating the subcontext
     */
    public static Context getContext(String name, Context parent) throws NamingException {
        Context context;
        try {
            context = (Context) parent.lookup(name);
        } catch (NameNotFoundException e) {
            context = parent.createSubcontext(name);
        }
        return context;
    }

    /**
     * Closes JNDI contexts.
     *
     * @param contexts the context(s) to close.
     */
    public static void close(Context... contexts) {
        if (contexts != null) {
            for (Context context : contexts) {
                try {
                    if (context != null) {
                        context.close();
                    }
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
