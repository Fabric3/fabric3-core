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
