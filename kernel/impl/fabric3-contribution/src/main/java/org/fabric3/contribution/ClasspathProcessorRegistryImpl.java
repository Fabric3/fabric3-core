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
package org.fabric3.contribution;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.spi.model.os.Library;
import org.fabric3.spi.contribution.archive.ClasspathProcessor;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;

/**
 *
 */
@EagerInit
public class ClasspathProcessorRegistryImpl implements ClasspathProcessorRegistry {
    private List<ClasspathProcessor> processors = new ArrayList<>();

    public void register(ClasspathProcessor processor) {
        processors.add(processor);
    }

    public void unregister(ClasspathProcessor processor) {
        processors.remove(processor);
    }

    public List<URL> process(URL url, List<Library> libraries) throws IOException {
        for (ClasspathProcessor processor : processors) {
            if (processor.canProcess(url)) {
                return processor.process(url, libraries);
            }
        }
        // artifact does not need to be expanded, just return its base url
        List<URL> urls = new ArrayList<>();
        urls.add(url);
        return urls;
    }
}
