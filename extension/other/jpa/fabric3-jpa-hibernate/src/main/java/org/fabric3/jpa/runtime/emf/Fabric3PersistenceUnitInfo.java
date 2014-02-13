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
package org.fabric3.jpa.runtime.emf;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.fabric3.spi.classloader.MultiParentClassLoader;

/**
 * Encapsulates a persistence unit configured in a persistence.xml file.
 */
public class Fabric3PersistenceUnitInfo implements PersistenceUnitInfo {
    private String unitName;
    private URL rootUrl;
    private ClassLoader classLoader;
    private String persistenceProviderClassName;
    private boolean exclude;
    private DataSource jtaDataSource;
    private DataSource nonJtaDataSource;
    private List<URL> jarUrls = new ArrayList<>();
    private List<String> managedClasses = new ArrayList<>();
    private List<String> mappingFiles = new ArrayList<>();
    private Properties properties = new Properties();
    private PersistenceUnitTransactionType trxType;
    private String version = "2.0";
    private SharedCacheMode sharedCacheMode = SharedCacheMode.UNSPECIFIED;
    private ValidationMode validationMode = ValidationMode.AUTO;

    public Fabric3PersistenceUnitInfo(String unitName) {
        this.unitName = unitName;
    }

    public String getPersistenceUnitName() {
        return unitName;
    }

    public URL getPersistenceUnitRootUrl() {
        return rootUrl;
    }

    public void addTransformer(ClassTransformer classTransformer) {
    }

    public void setExcludeUnlistedClasses(boolean exclude) {
        this.exclude = exclude;
    }

    public boolean excludeUnlistedClasses() {
        return exclude;
    }

    public SharedCacheMode getSharedCacheMode() {
        return sharedCacheMode;
    }

    public void setSharedCacheMode(SharedCacheMode sharedCacheMode) {
        this.sharedCacheMode = sharedCacheMode;
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void addJarFileUrl(URL url) {
        jarUrls.add(url);
    }

    public List<URL> getJarFileUrls() {
        return jarUrls;
    }

    public void setRootUrl(URL rootUrl) {
        this.rootUrl = rootUrl;
    }

    public void setJtaDataSource(DataSource dataSource) {
        this.jtaDataSource = dataSource;
    }

    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    public void setNonJtaDataSource(DataSource nonJtaDataSource) {
        this.nonJtaDataSource = nonJtaDataSource;
    }

    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public void addManagedClass(String name) {
        managedClasses.add(name);
    }

    public List<String> getManagedClassNames() {
        return managedClasses;
    }

    public void addMappingFile(String name) {
        mappingFiles.add(name);
    }

    public List<String> getMappingFileNames() {
        return mappingFiles;
    }

    public ClassLoader getNewTempClassLoader() {
        if (!(classLoader instanceof MultiParentClassLoader)) {
            return null;
        }
        MultiParentClassLoader original = (MultiParentClassLoader) classLoader;
        MultiParentClassLoader newClassLoader = new MultiParentClassLoader(URI.create("f3-temp"), original.getParent());
        for (ClassLoader parent : original.getParents()) {
            newClassLoader.addParent(parent);
        }
        for (URL url : original.getURLs()) {
            newClassLoader.addURL(url);
        }
        return newClassLoader;
    }

    public void setPersistenceProviderClassName(String persistenceProviderClassName) {
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    public void setTrxType(PersistenceUnitTransactionType trxType) {
        this.trxType = trxType;
    }

    public PersistenceUnitTransactionType getTransactionType() {
        return trxType;
    }

    public void addProperty(Object key, Object value) {
        properties.put(key, value);
    }

    public Properties getProperties() {
        return properties;
    }

    public String getPersistenceXMLSchemaVersion() {
        return version;
    }

    public void setPersistenceXMLSchemaVersion(String version) {
        this.version = version;
    }
}