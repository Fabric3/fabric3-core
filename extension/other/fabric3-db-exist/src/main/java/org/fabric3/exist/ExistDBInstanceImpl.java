/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.exist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import org.exist.EXistException;
import org.exist.security.User;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.util.Configuration;
import org.exist.util.DatabaseConfigurationException;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/** 
 * Exist DB instance. Started and stopped by the Fabric3 runtime
 * 
 * @version $Rev$ $Date$
 */
@EagerInit
public class ExistDBInstanceImpl implements ExistDBInstance {

    @Property
    public String existHome;
    @Property
    public String configFile;
    @Property
    public String instanceName;
    @Property
    public String userID;
    @Property
    public String password;
    @Property
    public int minBrokers;
    @Property
    public int maxBrokers;
    @Reference 
    public ExistDBInstanceRegistry eXistDBRegistry;
    

    public ExistDBInstanceImpl( ) {
        
    }

    @Init
    public void init()throws EXistException  {
        initializeDB();
        eXistDBRegistry.registerInstance(instanceName,this);
        

    }

    @Destroy
    public void destroy() {
        try{
        BrokerPool pool;
        if (instanceName == null) {
            pool = BrokerPool.getInstance();
        } else {
            pool = BrokerPool.getInstance(instanceName);
        }
        pool.shutdown();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public DBBroker getInstance () throws EXistException {
      /*  if (!initialized){
            initializeDB();
        }
       */
        BrokerPool pool;
        if (instanceName == null) {
            pool = BrokerPool.getInstance();
        } else {
            pool = BrokerPool.getInstance(instanceName);
        }
        User user = pool.getSecurityManager().getUser(userID);
        return pool.get(user);

    }
    
     public void releaseInstance (DBBroker broker) throws EXistException {
        BrokerPool pool;
        if (instanceName == null) {
            pool = BrokerPool.getInstance();
        } else {
            pool = BrokerPool.getInstance(instanceName);
        }
        pool.release(broker);

    }

    protected void initializeDB() throws EXistException{
        try {
            File defaultHome = new File(System.getProperty("user.dir"));
            File existHomeDir;
            File config;
            if (existHome != null) {
                existHomeDir = new File(existHome);
            } else if (configFile != null) {
                existHomeDir = new File(configFile).getParentFile();
            } else {
                existHomeDir = new File(defaultHome, "eXist");
            }
            if (!existHomeDir.isAbsolute()){
                    existHomeDir = new File(defaultHome,existHomeDir.getPath());
            }
            existHomeDir.mkdirs();

            if (configFile != null) {
                config = new File(configFile);
                if (!config.isAbsolute()){
                    throw new EXistException(String.format("Unable to locate config file %s use absolute path", configFile));
                }
            } else {
                config = new File(existHomeDir, "conf.xml");
                FileChannel out = new FileOutputStream(config).getChannel();
                ReadableByteChannel in = Channels.newChannel(getClass().getResourceAsStream("/f3-exist-conf.xml"));
                out.transferFrom(in, 0, 1024 * 100); //max 100k 
                File data = new File(existHomeDir, "data");
                data.mkdirs();

            }

            Configuration cfg = new Configuration(config.getAbsolutePath(), existHomeDir.getAbsolutePath());
            minBrokers = minBrokers == 0 ? 1 : minBrokers;
            maxBrokers = maxBrokers == 0 ? 5 : maxBrokers;
            if (instanceName == null) {
                BrokerPool.configure(minBrokers, maxBrokers, cfg);
            } else {
                BrokerPool.configure(instanceName, minBrokers, maxBrokers, cfg);
            }
            if (userID == null) {
                userID = "admin";
            }
        } catch (DatabaseConfigurationException ex) {
            throw new EXistException(ex);
        } catch (IOException ie) {
            throw new EXistException(ie);
        }
    }

  
}
