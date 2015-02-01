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
package org.fabric3.monitor.appender.file;

import java.io.File;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.appender.AppenderBuilder;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Instantiates a {@link FileAppender} from a {@link PhysicalFileAppenderDefinition}.
 */
@EagerInit
public class FileAppenderBuilder implements AppenderBuilder<PhysicalFileAppenderDefinition> {
    private HostInfo hostInfo;

    public FileAppenderBuilder(@Reference HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Appender build(PhysicalFileAppenderDefinition definition) throws Fabric3Exception {
        File outputDir = new File(hostInfo.getDataDir(), "logs");
        outputDir.mkdirs();
        File outputFile = new File(outputDir, definition.getFileName());

        String rollType = definition.getRollType();

        if (FileAppenderConstants.ROLL_STRATEGY_NONE.equals(rollType)) {
            RollStrategy strategy = new NoRollStrategy();
            return new FileAppender(outputFile, strategy, false);
        } else if (FileAppenderConstants.ROLL_STRATEGY_SIZE.equals(rollType)) {
            long rollSize = definition.getRollSize();
            int maxBackups = definition.getMaxBackups();
            RollStrategy strategy = new SizeRollStrategy(rollSize, maxBackups);
            return new FileAppender(outputFile, strategy, false);
        } else {
            throw new Fabric3Exception("Unknown roll type: " + rollType);
        }

    }
}
