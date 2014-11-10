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
 */
package org.fabric3.plugin.contribution;

import java.io.File;

import org.fabric3.api.host.failure.ValidationFailure;

/**
 * Validation warning indicating that the possible contribution file with the given File could not be loaded.
 */
public class ContributionIndexingFailure extends ValidationFailure {
    private File file;
    private Exception ex;

    public ContributionIndexingFailure(File file, Exception ex) {
        this.file = file;
        this.ex = ex;
    }

    /**
     * Retrieves the message for the failure that includes both the standard ValidationFailure message along with details of the exception.
     *
     * @return the message.
     */
    public String getMessage() {
        if (ex == null) {
            return "Error indexing file " + file;
        }
        return "Error indexing file " + file + "\n " + ex;
    }

    public String getShortMessage() {
        if (ex == null) {
            return "Error indexing file " + file;
        }
        return "Error indexing file " + file + ":  " + ex.getMessage();
    }

}
