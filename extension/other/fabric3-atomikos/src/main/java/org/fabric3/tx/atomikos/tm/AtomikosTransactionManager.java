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
package org.fabric3.tx.atomikos.tm;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.TransactionManagerImp;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.RuntimeRecover;

/**
 * Wraps an Atomikos transaction manager. Configured JDBC and JMS resource registration is handled implicity by Atomikos.
 *
 * @version $Rev$ $Date$
 */
@Service(javax.transaction.TransactionManager.class)
public class AtomikosTransactionManager implements TransactionManager, Fabric3EventListener<RuntimeRecover> {
    private static final String ATOMIKOS_NO_FILE = "com.atomikos.icatch.no_file";
    private static final String OUTPUT_DIR_PROPERTY_NAME = "com.atomikos.icatch.output_dir";
    private static final String LOG_BASE_DIR_PROPERTY_NAME = "com.atomikos.icatch.log_base_dir";
    private static final String FACTORY_KEY = "com.atomikos.icatch.service";
    private static final String FACTORY_VALUE = "com.atomikos.icatch.standalone.UserTransactionServiceFactory";

    private EventService eventService;
    private HostInfo info;
    private TransactionManagerImp tm;
    private UserTransactionService uts;
    private Properties properties = new Properties();

    public AtomikosTransactionManager(@Reference EventService eventService, @Reference HostInfo info) {
        this.eventService = eventService;
        this.info = info;
    }

    @Init
    public void init() throws IOException {
        eventService.subscribe(RuntimeRecover.class, this);
        // turn off transactions.properties search by the transaction manager since these will be supplied directly
        System.setProperty(ATOMIKOS_NO_FILE, "true");
        // configure mandatory value
        System.setProperty(FACTORY_KEY, FACTORY_VALUE);
        // set defaults
        File dataDir = info.getDataDir();
        File trxDir = new File(dataDir, "transactions");
        if (!trxDir.exists()) {
            trxDir.mkdirs();
        }
        String path = trxDir.getCanonicalPath();
        properties.setProperty(OUTPUT_DIR_PROPERTY_NAME, path);
        properties.setProperty(LOG_BASE_DIR_PROPERTY_NAME, path);
//        PrintStreamConsole console = new PrintStreamConsole(System.out);
//        console.setLevel(Console.DEBUG);
//        Configuration.addConsole(console);
    }

    @Destroy
    public void destroy() {
        if (uts != null) {
            uts.shutdown(true);
            uts = null;
        }
    }

    @Property(required = false)
    public void setProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    /**
     * Performs initialization and transaction recovery. This is done after transactional resources (potentially in other extensions) have registered
     * with the transaction manager.
     */
    public void onEvent(RuntimeRecover event) {
        synchronized (TransactionManagerImp.class) {
            tm = (TransactionManagerImp) TransactionManagerImp.getTransactionManager();
            if (tm == null) {
                uts = new UserTransactionServiceImp(properties);
                uts.init(properties);
                tm = (TransactionManagerImp) TransactionManagerImp.getTransactionManager();
            }
        }
    }

    public void begin() throws NotSupportedException, SystemException {
        tm.begin();
    }

    public void commit()
            throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        tm.commit();
    }

    public int getStatus() throws SystemException {
        return tm.getStatus();
    }

    public Transaction getTransaction() throws SystemException {
        return tm.getTransaction();
    }

    public void resume(Transaction tx) throws InvalidTransactionException, IllegalStateException, SystemException {
        tm.resume(tx);
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        tm.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        tm.setRollbackOnly();
    }

    public void setTransactionTimeout(int secs) throws SystemException {
        tm.setTransactionTimeout(secs);
    }

    public Transaction suspend() throws SystemException {
        return tm.suspend();
    }

}
