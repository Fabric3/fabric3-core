/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import com.atomikos.datasource.xa.XID;
import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.TransactionManagerImp;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.RuntimeRecover;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 * Wraps an Atomikos transaction manager. Configured JDBC and JMS resource registration is handled implicitly by Atomikos.
 */
@Service(TransactionManager.class)
public class AtomikosTransactionManager implements TransactionManager, Fabric3EventListener<RuntimeRecover> {
    private static final String TM_NAME = "com.atomikos.icatch.tm_unique_name";

    private static final String ATOMIKOS_NO_FILE = "com.atomikos.icatch.no_file";
    private static final String OUTPUT_DIR_PROPERTY_NAME = "com.atomikos.icatch.output_dir";
    private static final String LOG_BASE_DIR_PROPERTY_NAME = "com.atomikos.icatch.log_base_dir";
    private static final String FACTORY_KEY = "com.atomikos.icatch.service";
    private static final String FACTORY_VALUE = "com.atomikos.icatch.standalone.UserTransactionServiceFactory";

    private static final String THREADED2PC = "com.atomikos.icatch.threaded_2pc";
    private static final String ENABLE_LOGGING = "com.atomikos.icatch.enable_logging";
    private static final String CHECKPOINT_INTERVAL = "com.atomikos.icatch.checkpoint_interval";

    private EventService eventService;
    private HostInfo info;
    private TransactionManagerImp tm;
    private UserTransactionService uts;

    private Properties properties = new Properties();

    // Transaction timeout. A value of -1 indicates the default Atomikos timeout.
    private int timeout = -1;

    // True if 2PC on a participating resource should be handled from a single thread. False by default so acknowledgements are done in parallel.  
    private boolean singleThreaded2PC;

    // Set to false if transaction logging to disk should not be done. If set to false, transaction integrity cannot be guaranteed.
    // Only for use in unit or integration tests where disk access needs to be disabled for performance.
    private boolean enableLogging = true;

    private long checkPointInterval = -1;

    public AtomikosTransactionManager(@Reference EventService eventService, @Reference HostInfo info) {
        this.eventService = eventService;
        this.info = info;
    }

    @Property(required = false)
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Property(required = false)
    public void setProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    @Property(required = false)
    public void setSingleThreaded2PC(boolean singleThreaded2PC) {
        this.singleThreaded2PC = singleThreaded2PC;
    }

    @Property(required = false)
    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    @Property(required = false)
    public void setCheckPointInterval(long checkPointInterval) {
        this.checkPointInterval = checkPointInterval;
    }

    @Init
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void init() throws IOException {
        eventService.subscribe(RuntimeRecover.class, this);

        // turn off transactions.properties search by the transaction manager since these will be supplied directly
        System.setProperty(ATOMIKOS_NO_FILE, "true");

        // configure mandatory value
        System.setProperty(FACTORY_KEY, FACTORY_VALUE);

        File dataDir = info.getDataDir();
        File trxDir = new File(dataDir, "transactions");
        if (!trxDir.exists()) {
            trxDir.mkdirs();
        }

        // set the unique TM name
        String name = info.getRuntimeName().replace(":", "_");
        // shorten to length required by Atomikos if too large
        if (name.getBytes().length + 16 > XID.MAXGTRIDSIZE) {
            name = new String(Arrays.copyOfRange(name.getBytes(), 0, XID.MAXGTRIDSIZE - 16));
        }
        properties.setProperty(TM_NAME, name);

        String path = trxDir.getCanonicalPath();
        properties.setProperty(OUTPUT_DIR_PROPERTY_NAME, path);
        properties.setProperty(LOG_BASE_DIR_PROPERTY_NAME, path);

        properties.setProperty(THREADED2PC, Boolean.toString(singleThreaded2PC));
        properties.setProperty(ENABLE_LOGGING, Boolean.toString(enableLogging));
        if (checkPointInterval != -1) {
            properties.setProperty(CHECKPOINT_INTERVAL, Long.toString(checkPointInterval));
        }

    }

    @Destroy
    public void destroy() {
        if (uts != null) {
            uts.shutdown(true);
            uts = null;
        }
    }

    /**
     * Performs initialization and transaction recovery. This is done after transactional resources (potentially in other extensions) have registered with the
     * transaction manager.
     */
    public void onEvent(RuntimeRecover event) {
        synchronized (TransactionManagerImp.class) {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                tm = (TransactionManagerImp) TransactionManagerImp.getTransactionManager();
                if (tm == null) {
                    uts = new UserTransactionServiceImp(properties);
                    uts.init(properties);
                    tm = (TransactionManagerImp) TransactionManagerImp.getTransactionManager();
                }
                if (timeout != -1) {
                    try {
                        tm.setTransactionTimeout(timeout);
                    } catch (SystemException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                Thread.currentThread().setContextClassLoader(old);
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
