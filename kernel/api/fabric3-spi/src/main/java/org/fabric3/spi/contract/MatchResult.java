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
package org.fabric3.spi.contract;

/**
 * The result of a contract matching operation.
 */
public class MatchResult {
    public static final MatchResult MATCH = new MatchResult(true);
    public static final MatchResult NO_MATCH = new MatchResult(false);

    private boolean assignable;
    private String error;

    public MatchResult(boolean assignable) {
        this.assignable = assignable;
    }

    public MatchResult(String error) {
        this.assignable = false;
        this.error = error;
    }

    /**
     * True if the contracts are compatible.
     *
     * @return if the contracts are compatible.
     */
    public boolean isAssignable() {
        return assignable;
    }

    /**
     * Returns the error or null if the contracts match
     *
     * @return the error  or null
     */
    public String getError() {
        return error;
    }
}
