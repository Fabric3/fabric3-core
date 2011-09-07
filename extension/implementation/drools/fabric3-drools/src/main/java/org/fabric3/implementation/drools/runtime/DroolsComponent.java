/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.namespace.QName;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatelessKnowledgeSession;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ComponentException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * A Drools component. Each Drools component contains an associated knowledge base against which data from service invocations are evaluated.
 *
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
public class DroolsComponent implements AtomicComponent {

    private URI uri;
    private KnowledgeBase knowledgeBase;
    private Map<String, KnowledgeInjector<StatelessKnowledgeSession>> injectorMap;
    private List<KnowledgeInjector<StatelessKnowledgeSession>> injectors;
    private QName deployable;
    private URI classLoaderId;

    public DroolsComponent(URI uri, KnowledgeBase base, Map<String, KnowledgeInjector<StatelessKnowledgeSession>> injectors, QName deployable) {
        this.uri = uri;
        this.knowledgeBase = base;
        this.injectorMap = injectors;
        this.injectors = new CopyOnWriteArrayList<KnowledgeInjector<StatelessKnowledgeSession>>(injectors.values());
        this.deployable = deployable;
    }

    public QName getDeployable() {
        return deployable;
    }

    public URI getUri() {
        return uri;
    }

    public URI getClassLoaderId() {
        return classLoaderId;
    }

    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

    public String getName() {
        return uri.toString();
    }

    public boolean isEagerInit() {
        return false;
    }

    public long getMaxIdleTime() {
        return -1;
    }

    public long getMaxAge() {
        return -1;
    }

    public InstanceWrapper createInstanceWrapper(WorkContext workContext) throws ObjectCreationException {
        throw new UnsupportedOperationException();
    }

    public ObjectFactory<Object> createObjectFactory() {
        throw new UnsupportedOperationException();
    }

    public void start() throws ComponentException {

    }

    public void stop() throws ComponentException {

    }

    public void startUpdate() {

    }

    public void endUpdate() {

    }

    public MonitorLevel getLevel() {
        return null;
    }

    public void setLevel(MonitorLevel level) {

    }

    public StatelessKnowledgeSession createStatelessSession() throws ObjectCreationException {
        StatelessKnowledgeSession session = knowledgeBase.newStatelessKnowledgeSession();
        for (Injector<StatelessKnowledgeSession> injector : injectors) {
            injector.inject(session);
        }
        return session;
    }

    public void removeObjectFactory(String identifier) {
        KnowledgeInjector injector = injectorMap.get(identifier);
        if (injector == null) {
            throw new AssertionError("Injector not found: " + identifier);
        }
        injectors.remove(injector);
    }

    public void setObjectFactory(String identifier, ObjectFactory<?> factory) {
        // multiplicity references are not supported, therefore overwrite an existing factory
        StatelessInjector injector = new StatelessInjector(identifier, factory);
        injectors.add(injector);
        injectorMap.put(identifier, injector);
    }

    public ObjectFactory<?> getObjectFactory(String identifier) {
        return injectorMap.get(identifier).getObjectFactory();
    }
}
