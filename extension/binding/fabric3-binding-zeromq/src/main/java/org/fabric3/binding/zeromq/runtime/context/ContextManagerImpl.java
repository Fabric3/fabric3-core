/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime.context;

import org.fabric3.host.runtime.HostInfo;
import java.io.IOException;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

/**
 * @version $Revision$ $Date: 2011-06-25 18:27:25 +0200 (Sat, 25 Jun
 *          2011) $
 */
@EagerInit
public class ContextManagerImpl implements ContextManager {
	private Context context;

	@Reference
	protected HostInfo hostInfo;

	public Context getContext() {
		return context;
	}

	@Init
	public void init() {
		// Windows requires the ZMQ library to be loaded as the JZMQ library is
		// linked to it and Windows is unable to
		// resolve it relative to the JZMQ library
		// System.loadLibrary("zmq");

		ZMQLibraryInitializer.loadLibrary(hostInfo);
		context = ZMQ.context(1);
	}

	@Destroy
	public void destroy() {
		context.term();
	}

	/**
	 * Initializes the ZeroMQ library on Windows and Linux. If the ZeroMQ
	 * Library is not initialized before the Context is created the loading of
	 * the library is delegated to the Operating System. This causes problems
	 * since then F3 can't control where to load the libraries from. To work
	 * around this problem we initialize ZeroMQ base library (libzmq.dll or
	 * libzmq.so) prior to the JZMQ (which happens when a Context is created).
	 * This workaround is currently tested on Windows and Linux.
	 */
	protected enum ZMQLibraryInitializer {
		WINDOWS("libzmq"), LINUX("zmq"), OTHER("");

		private String libName;

		private ZMQLibraryInitializer(String libName) {
			this.libName = libName;
		}

		/**
		 * Uses the OperatingSystem information of the HostInfo to decide what
		 * library to load.
		 * On Windows the library name is "libzmq".
		 * On Linux the library name is "zmq".
		 * 
		 * @param hostInfo
		 *            Based on the OperatingSystem member the needed Library
		 *            will be loaded.
		 */
		public static void loadLibrary(HostInfo hostInfo) {
			if (hostInfo == null)
				return;
			String osName = hostInfo.getOperatingSystem().getName();

			for (ZMQLibraryInitializer lib : values()) {
				if (lib.name().equalsIgnoreCase(osName)) {
					lib.loadLibrary();
					return;
				}
			}
		}

		private void loadLibrary() {
			if (!this.equals(OTHER))
				System.loadLibrary(libName);
		}
	}
}
