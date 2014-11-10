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
package org.fabric3.binding.zeromq.runtime.message;

import java.util.Arrays;

/**
 * Used for storing a byte array in a collection.
 */
public class ByteArrayKey {
    private byte[] data;

    public ByteArrayKey(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ByteArrayKey && Arrays.equals(data, ((ByteArrayKey) other).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

}
