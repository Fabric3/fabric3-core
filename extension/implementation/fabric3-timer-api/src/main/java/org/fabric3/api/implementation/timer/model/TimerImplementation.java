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
package org.fabric3.api.implementation.timer.model;

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.java.JavaImplementation;

/**
 * A timer component implementation type.
 */
public class TimerImplementation extends JavaImplementation {
    private static final long serialVersionUID = -911919528396189874L;

    public static final QName IMPLEMENTATION_TIMER = new QName(org.fabric3.api.Namespaces.F3, "implementation.timer");
    private TimerData timerData;

    public QName getType() {
        return IMPLEMENTATION_TIMER;
    }

    public TimerData getTimerData() {
        return timerData;
    }

    public void setTimerData(TimerData timerData) {
        this.timerData = timerData;
    }
}