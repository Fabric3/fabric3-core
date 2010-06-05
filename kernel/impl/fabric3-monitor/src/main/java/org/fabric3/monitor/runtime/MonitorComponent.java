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
package org.fabric3.monitor.runtime;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.host.Fabric3RuntimeException;
import org.fabric3.host.monitor.MonitorEventDispatcher;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.monitor.DispatcherWrapper;

/**
 * @version $Rev: 9019 $ $Date: 2010-05-20 17:00:08 +0200 (Thu, 20 May 2010) $
 */
public class MonitorComponent implements Component {
    private URI uri;
    private QName deployable;
    private URI classLoaderId;
    private MonitorLevel level;
    private MonitorEventDispatcher dispatcher;
    private EventStreamHandler handler;

    public MonitorComponent(URI uri, QName deployable, MonitorEventDispatcher dispatcher) {
        this.uri = uri;
        this.deployable = deployable;
        this.dispatcher = dispatcher;
        handler = new DispatcherWrapper(dispatcher);
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

    public MonitorLevel getLevel() {
        return level;
    }

    public void setLevel(MonitorLevel level) {
        this.level = level;
    }

    public void start() {
        dispatcher.start();
    }

    public void stop() {
        dispatcher.stop();
    }

    public void attach(ChannelConnection connection) {
        for (EventStream stream : connection.getEventStreams()) {
            stream.addHandler(handler);
        }
    }
}