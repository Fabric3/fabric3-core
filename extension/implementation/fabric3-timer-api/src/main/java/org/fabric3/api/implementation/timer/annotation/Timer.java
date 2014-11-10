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
package org.fabric3.api.implementation.timer.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.fabric3.api.annotation.model.Implementation;
import org.fabric3.api.implementation.timer.model.TimerType;

/**
 * Configures a timer component.
 */
@Implementation("{urn:fabric3.org}implementation.timer")
@Retention(RetentionPolicy.RUNTIME)
public @interface Timer {

    /**
     * Specifies the timer type.
     *
     * @return the timer type
     */
    TimerType type() default TimerType.INTERVAL;

    /**
     * Specifies the timer pool.
     *
     * @return the timer pool name
     */
    String pool() default "default";

    /**
     * Specifies the initial delay before the timer will fire for the first time.
     *
     * @return the initial delay in milliseconds.
     */
    long initialDelay() default 100;

    /**
     * Specifies the fixed rate for a {@link TimerType#FIXED_RATE} timer.
     *
     * @return the fixed rate in milliseconds.
     */
    long fixedRate() default -1;

    /**
     * Specifies the firing interval for an {@link TimerType#INTERVAL} timer.
     *
     * @return the interval in milliseconds.
     */
    long repeatInterval() default -1;

    /**
     * Specifies the delay before firing a {@link TimerType#ONCE} timer.
     *
     * @return the interval in milliseconds.
     */
    long fireOnce() default -1;

}
