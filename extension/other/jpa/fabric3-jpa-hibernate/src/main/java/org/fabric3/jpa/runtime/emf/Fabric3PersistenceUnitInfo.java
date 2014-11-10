/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
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