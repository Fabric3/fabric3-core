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
package org.fabric3.implementation.drools.runtime;

import org.drools.runtime.StatelessKnowledgeSession;

import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Injects a value in a stateless knowledge session.
 *
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
public class StatelessInjector implements KnowledgeInjector<StatelessKnowledgeSession> {
    private String identifier;
    private ObjectFactory<?> factory;

    public StatelessInjector(String identifier, ObjectFactory<?> factory) {
        this.identifier = identifier;
        this.factory = factory;
    }

    public void inject(StatelessKnowledgeSession session) throws ObjectCreationException {
        session.setGlobal(identifier, factory.getInstance());
    }

    public void setObjectFactory(ObjectFactory<?> factory, Object key) {
        this.factory = factory;
    }

    public void clearObjectFactory() {
        this.factory = null;
    }

    public ObjectFactory<?> getObjectFactory() {
        return factory;
    }
}
