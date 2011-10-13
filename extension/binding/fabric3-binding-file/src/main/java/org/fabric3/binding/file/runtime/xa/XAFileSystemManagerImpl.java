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
package org.fabric3.binding.file.runtime.xa;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.bridge.proxies.interfaces.XAFileSystemProxy;
import org.xadisk.bridge.proxies.interfaces.XASession;
import org.xadisk.filesystem.standalone.StandaloneFileSystemConfiguration;

/**
 * @version $Revision: 10798 $ $Date: 2011-10-12 18:37:16 +0200 (Wed, 12 Oct 2011) $
 */
@EagerInit
public class XAFileSystemManagerImpl implements XAFileSystemManager {
    private String journalDir;


    private TransactionManager tm;

    public XAFileSystemManagerImpl(@Reference TransactionManager tm) {
        this.tm = tm;
    }

    public void init() {
        XAFileSystem xaFileSystem;
        try {
            StandaloneFileSystemConfiguration configuration = new StandaloneFileSystemConfiguration(journalDir, "1");
            xaFileSystem = XAFileSystemProxy.bootNativeXAFileSystem(configuration);
            xaFileSystem.waitForBootup(-1);
            xaFileSystem.getXAResourceForRecovery();

           //tm.
            System.out.println("Starting an XA transaction...\n");
//            tm.begin();
            Transaction tx1 = tm.getTransaction();

            XASession xaSession = xaFileSystem.createSessionForXATransaction();

            System.out.println("Enlisting XADisk in the XA transaction.");
            XAResource xarXADisk = xaSession.getXAResource();
            tx1.enlistResource(xarXADisk);

            System.out.println("Enlisting other XA-enabled resources (e.g. Oracle, MQ) in the XA transaction.\n");
            /*XAResource xarOracle = null;
            tx1.enlistResource(xarOracle);*/

            /*XAResource xarMQ = null;
            tx1.enlistResource(xarMQ);*/

            System.out.println("Performing transactional work over XADisk and other involved resources (e.g. Oracle, MQ)\n");
         //   xaSession.createFile(new File(testFile), false);

            tm.commit();

//            xaFileSystem.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

