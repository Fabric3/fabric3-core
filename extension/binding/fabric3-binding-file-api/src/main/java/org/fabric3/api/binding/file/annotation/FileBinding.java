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
package org.fabric3.api.binding.file.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.fabric3.api.annotation.model.Binding;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Configures a reference or service with the File binding.
 */
@Target({TYPE, FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
@Binding("{urn:fabric3.org}binding.file")
public @interface FileBinding {

    /**
     * Specifies the service interface to bind.
     *
     * @return the service interface to bind
     */
    public Class<?> service() default Void.class;

    /**
     * Specifies the directory to scan for incoming files relative to the runtime data directory.
     *
     * @return the directory to scan for incoming files
     */
    public String location();

    /**
     * Specifies the archive handling strategy.
     *
     * @return the archive handling strategy
     */
    public Strategy strategy() default Strategy.DELETE;

    /**
     * Specifies the archive directory when the {@link Strategy#ARCHIVE} is used.
     *
     * @return the archive directory
     */
    public String archiveLocation() default "";

    /**
     * Specifies the location where files than cannot be processed are sent.
     *
     * @return the location where files than cannot be processed are sent
     */
    public String errorLocation() default "";

    /**
     * Specifies a file name regex filter pattern.
     *
     * @return the pattern
     */
    public String pattern() default "";

    /**
     * Specifies an adapter URI.
     *
     * @return the adapter URI
     */
    public String adaptor() default "";

    /**
     * Specifies the initial delay in milliseconds to wait before processing files.
     *
     * @return the initial delay
     */
    public long delay() default -1;

    /**
     * Specifies the binding name.
     *
     * @return the binding name
     */
    public String name() default "";

}
