package org.fabric3.api.host.runtime;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.fabric3.api.host.os.OperatingSystem;
import org.fabric3.api.model.type.RuntimeMode;

public class DefaultHostInfoBuilder {
    private String runtimeName;
    private String zoneName;
    private RuntimeMode runtimeMode;
    private String environment;
    private URI domain;
    private File baseDir;
    private File sharedDirectory;
    private File dataDirectory;
    private File tempDirectory;
    private List<File> deployDirectories;
    private OperatingSystem operatingSystem;
    private boolean javaEEXAEnabled;

    public DefaultHostInfoBuilder runtimeName(String runtimeName) {
        this.runtimeName = runtimeName;
        return this;
    }

    public DefaultHostInfoBuilder zoneName(String zoneName) {
        this.zoneName = zoneName;
        return this;
    }

    public DefaultHostInfoBuilder runtimeMode(RuntimeMode runtimeMode) {
        this.runtimeMode = runtimeMode;
        return this;
    }

    public DefaultHostInfoBuilder environment(String environment) {
        this.environment = environment;
        return this;
    }

    public DefaultHostInfoBuilder domain(URI domain) {
        this.domain = domain;
        return this;
    }

    public DefaultHostInfoBuilder baseDir(File baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    public DefaultHostInfoBuilder sharedDirectory(File sharedDirectory) {
        this.sharedDirectory = sharedDirectory;
        return this;
    }

    public DefaultHostInfoBuilder dataDirectory(File dataDirectory) {
        this.dataDirectory = dataDirectory;
        return this;
    }

    public DefaultHostInfoBuilder tempDirectory(File tempDirectory) {
        this.tempDirectory = tempDirectory;
        return this;
    }

    public DefaultHostInfoBuilder deployDirectories(List<File> deployDirectories) {
        this.deployDirectories = deployDirectories;
        return this;
    }

    public DefaultHostInfoBuilder operatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
        return this;
    }

    public DefaultHostInfoBuilder javaEEXAEnabled(boolean javaEEXAEnabled) {
        this.javaEEXAEnabled = javaEEXAEnabled;
        return this;
    }

    public DefaultHostInfo build() {
        return new DefaultHostInfo(runtimeName,
                                   zoneName,
                                   runtimeMode,
                                   environment,
                                   domain,
                                   baseDir,
                                   sharedDirectory,
                                   dataDirectory,
                                   tempDirectory,
                                   deployDirectories,
                                   operatingSystem,
                                   javaEEXAEnabled);
    }
}