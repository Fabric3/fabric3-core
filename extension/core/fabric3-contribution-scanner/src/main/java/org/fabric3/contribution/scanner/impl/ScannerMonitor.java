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
package org.fabric3.contribution.scanner.impl;

import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;

/**
 * Monitoring interface for the DirectoryScanner
 */
public interface ScannerMonitor {

    /**
     * Called when a contribution is deployed.
     *
     * @param name the name of the contribution
     */
    @Info("Processed {0}")
    void processed(String name);

    /**
     * Called when a contribution is removed
     *
     * @param name the name of the contribution
     */
    @Info("Removed {0}")
    void removed(String name);

    /**
     * Called when a file type is not recognized and ignored.
     *
     * @param name the file name
     */
    @Info("Contribution type not recognized: {0}. If this is a valid type, ensure runtime extensions are installed.")
    void ignored(String name);

    /**
     * Called when a general error is encountered processing a contribution
     *
     * @param e the error
     */
    @Severe("An error was encountered deploying a contribution")
    void error(Throwable e);

    /**
     * Called when an error is encountered removing a contribution
     *
     * @param filename the file being removed
     * @param e        the error
     */
    @Severe("Error removing {0}")
    void removalError(String filename, Throwable e);

    /**
     * Called when errors are encountered processing contributions
     *
     * @param description a description of the errors
     */
    @Severe("The following contribution errors were found:\n\n {0}")
    void contributionErrors(String description);

    /**
     * Called when errors are encountered during deployments
     *
     * @param description a description of the errors
     */
    @Severe("The following deployment errors were raised:\n\n {0}")
    void deploymentErrors(String description);

}
