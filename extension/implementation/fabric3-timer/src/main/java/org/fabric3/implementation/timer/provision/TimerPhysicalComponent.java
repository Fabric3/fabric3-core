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
package org.fabric3.implementation.timer.provision;

import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.implementation.java.provision.PhysicalJavaComponent;

/**
 *
 */
public class TimerPhysicalComponent extends PhysicalJavaComponent {
    private TimerData timerData;
    private boolean transactional;

    public TimerData getTriggerData() {
        return timerData;
    }

    public void setTriggerData(TimerData timerData) {
        this.timerData = timerData;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }
             
}
