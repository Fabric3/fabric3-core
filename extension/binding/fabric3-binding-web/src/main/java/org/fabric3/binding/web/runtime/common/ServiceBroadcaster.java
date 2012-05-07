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
package org.fabric3.binding.web.runtime.common;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.BroadcasterFuture;
import org.atmosphere.cpr.DefaultBroadcaster;

/**
 * @version $Rev$ $Date$
 */
public class ServiceBroadcaster extends DefaultBroadcaster {

    public ServiceBroadcaster(String path , AtmosphereConfig config) {
        super(path, config);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected void broadcast(AtmosphereResource resource, AtmosphereResourceEvent event) {
        super.broadcast(resource, event);
    }

    @Override
    public Future<Object> broadcast(Object msg) {
        msg = filter(msg);
        if (msg == null) {
            return null;
        }
        BroadcasterFuture<Object> future = new BroadcasterFuture<Object>(msg);
        future.done();
        push(new Entry(msg, null, future, true));
        return cast(future);
    }

    @Override
    public Future<Object> broadcast(Object msg, AtmosphereResource r) {
        msg = filter(msg);
        if (msg == null) {
            return null;
        }
        BroadcasterFuture<Object> future = new BroadcasterFuture<Object>(msg);
        future.done();
        push(new Entry(msg, r, future, true));
        return cast(future);
    }

    @Override
    public <Object> Future<Object> broadcast(Object msg, Set<AtmosphereResource> subset) {
        msg = (Object)filter(msg);
        if (msg == null) {
            return null;
        }
        BroadcasterFuture<Object> future = new BroadcasterFuture<Object>(msg);
        future.done();
        push(new Entry(msg, subset, future, true));
        return cast(future);
    }

    @Override
    public Future<Object> delayBroadcast(final Object o, long delay, TimeUnit t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Object> scheduleFixedBroadcast(final Object o, long period, TimeUnit t) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({"unchecked"})
    private <T> T cast(Object o) {
        return (T) o;
    }


}
