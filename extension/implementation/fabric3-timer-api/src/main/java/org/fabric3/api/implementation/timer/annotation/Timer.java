/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
