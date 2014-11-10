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
package org.fabric3.spi.introspection.validation;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.fabric3.api.host.failure.ValidationFailure;

/**
 *
 */
public class ValidationUtilsTestCase extends TestCase {
    private List<ValidationFailure> failures;

    public void testWriteErrors() throws Exception {
        String output = ValidationUtils.outputErrors(failures);
        assertTrue(output.indexOf("this is a test") > 0);
    }

    public void testWriteWarnings() throws Exception {
        String output = ValidationUtils.outputWarnings(failures);
        assertTrue(output.indexOf("this is a test") > 0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        failures = new ArrayList<>();
        failures.add(new Failure());
    }

    private static class Failure extends ValidationFailure {

        public Failure() {
            super();
        }

        public String getMessage() {
            return "this is a test";
        }
    }
}
