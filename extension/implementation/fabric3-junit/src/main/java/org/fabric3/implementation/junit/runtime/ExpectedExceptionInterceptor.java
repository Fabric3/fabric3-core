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
package org.fabric3.implementation.junit.runtime;

import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.wire.Interceptor;

/**
 *
 */
public class ExpectedExceptionInterceptor implements Interceptor {
    private Class<? extends Throwable> expected;
    private Interceptor next;

    public ExpectedExceptionInterceptor(Class<? extends Throwable> expected) {
        this.expected = expected;
    }

    public Message invoke(Message msg) {
        Message ret = next.invoke(msg);
        if (ret.isFault() && expected.equals(ret.getBody().getClass())) {
            ret.setBody(null);
        }
        return ret;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Interceptor getNext() {
        return next;
    }
}
