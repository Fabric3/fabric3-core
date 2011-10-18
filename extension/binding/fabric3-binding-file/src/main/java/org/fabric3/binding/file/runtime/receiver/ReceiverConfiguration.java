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
package org.fabric3.binding.file.runtime.receiver;

import java.io.File;
import java.util.regex.Pattern;

import org.fabric3.binding.file.api.ServiceAdapter;
import org.fabric3.binding.file.common.Strategy;
import org.fabric3.spi.wire.Interceptor;

/**
 * Configuration for instantiating a file binding receiver.
 *
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
public class ReceiverConfiguration {
    private String id;
    private File location;
    private Pattern filePattern;
    private File lockDirectory;
    Strategy strategy;
    File errorLocation;
    File archiveLocation;

    private long delay;

    private Interceptor interceptor;
    private ReceiverMonitor monitor;
    private ServiceAdapter adapter;

    public ReceiverConfiguration(String id,
                                 File location,
                                 String filePattern,
                                 Strategy strategy,
                                 File errorLocation,
                                 File archiveLocation,
                                 Interceptor interceptor,
                                 ServiceAdapter adapter,
                                 long delay,
                                 ReceiverMonitor monitor) {
        this.id = id;
        this.location = location;
        this.strategy = strategy;
        this.errorLocation = errorLocation;
        this.archiveLocation = archiveLocation;
        this.filePattern = Pattern.compile(filePattern);
        this.interceptor = interceptor;
        this.adapter = adapter;
        this.delay = delay;
        this.monitor = monitor;
        this.lockDirectory = new File(location, "locks");
    }

    public String getId() {
        return id;
    }

    public File getLocation() {
        return location;
    }

    public Pattern getFilePattern() {
        return filePattern;
    }

    public File getLockDirectory() {
        return lockDirectory;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public File getErrorLocation() {
        return errorLocation;
    }

    public File getArchiveLocation() {
        return archiveLocation;
    }

    public long getDelay() {
        return delay;
    }

    public Interceptor getInterceptor() {
        return interceptor;
    }

    public ServiceAdapter getAdapter() {
        return adapter;
    }

    public ReceiverMonitor getMonitor() {
        return monitor;
    }
}
