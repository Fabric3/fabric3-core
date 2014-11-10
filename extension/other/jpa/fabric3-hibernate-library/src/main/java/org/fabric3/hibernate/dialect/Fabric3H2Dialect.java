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
package org.fabric3.hibernate.dialect;

import org.hibernate.dialect.H2Dialect;

/**
 * Workaround for the following Hibernate bug when using the H2 dialect:
 * <p/>
 * https://hibernate.atlassian.net/browse/HHH-7002
 * <p/>
 * Note that the fix was never applied to 4.3.5.Final, although the issue states it was applied to 4.3.0.Beta4.
 */
public class Fabric3H2Dialect extends H2Dialect {

    public String getDropSequenceString(String sequenceName) {
        return "drop sequence if exists " + sequenceName;
    }

    public boolean dropConstraints() {
        return false;
    }

}
