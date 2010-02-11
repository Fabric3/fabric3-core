package org.fabric3.runtime.maven.itest;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;

public class MavenBootConfiguration {
    private Properties properties;
    private File outputDirectory;
    private String systemConfigDir;
    private String systemConfig;
    private ClassLoader bootClassLoader;
    private ClassLoader hostClassLoader;
    private Set<URL> moduleDependencies;
    private Set<Dependency> extensions;
    private Log log;
    private ExtensionHelper extensionHelper;
    private List<URL> policyUrls;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getSystemConfigDir() {
        return systemConfigDir;
    }

    public void setSystemConfigDir(String systemConfigDir) {
        this.systemConfigDir = systemConfigDir;
    }

    public String getSystemConfig() {
        return systemConfig;
    }

    public void setSystemConfig(String systemConfig) {
        this.systemConfig = systemConfig;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public void setBootClassLoader(ClassLoader bootClassLoader) {
        this.bootClassLoader = bootClassLoader;
    }

    public ClassLoader getHostClassLoader() {
        return hostClassLoader;
    }

    public void setHostClassLoader(ClassLoader hostClassLoader) {
        this.hostClassLoader = hostClassLoader;
    }

    public Set<URL> getModuleDependencies() {
        return moduleDependencies;
    }

    public void setModuleDependencies(Set<URL> moduleDependencies) {
        this.moduleDependencies = moduleDependencies;
    }

    public Set<Dependency> getExtensions() {
        return extensions;
    }

    public void setExtensions(Set<Dependency> extensions) {
        this.extensions = extensions;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public ExtensionHelper getExtensionHelper() {
        return extensionHelper;
    }

    public void setExtensionHelper(ExtensionHelper extensionHelper) {
        this.extensionHelper = extensionHelper;
    }

    public List<URL> getPolicyUrls() {
        return policyUrls;
    }

    public void setPolicyUrls(List<URL> policyUrls) {
        this.policyUrls = policyUrls;
    }

}
