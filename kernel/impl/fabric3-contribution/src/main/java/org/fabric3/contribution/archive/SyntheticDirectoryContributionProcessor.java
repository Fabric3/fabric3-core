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
package org.fabric3.contribution.archive;

import java.io.File;
import java.net.URL;

import org.fabric3.api.host.util.FileHelper;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Synthesizes a special contribution from a directory that is configured to extend an extension point derived from the name of the directory. For example, a
 * contribution can be synthesized that extends an extension point provided by a datasource extension by making JDBC drivers available.
 */
@EagerInit
public class SyntheticDirectoryContributionProcessor extends AbstractContributionProcessor {
    private static final String CONTENT_TYPE = "application/vnd.fabric3.synthetic";

    public boolean canProcess(Contribution contribution) {
        return CONTENT_TYPE.equals(contribution.getContentType());
    }

    public void processManifest(Contribution contribution, final IntrospectionContext context) {
        URL sourceUrl = contribution.getLocation();
        File root = FileHelper.toFile(sourceUrl);
        ContributionManifest manifest = contribution.getManifest();
        manifest.setExtension(true);
        manifest.addExtend(root.getName());
    }

    public void index(Contribution contribution, IntrospectionContext context) {

    }

    public void process(Contribution contribution, IntrospectionContext context) {

    }

}