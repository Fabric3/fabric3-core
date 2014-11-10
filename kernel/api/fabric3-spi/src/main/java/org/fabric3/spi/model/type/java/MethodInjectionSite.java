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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.model.type.java;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;

import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.api.model.type.java.Signature;

/**
 * Represents a setter method that is injected into when a component implementation instance is instantiated.
 * <p/>
 * Note this class implements <code>Externalizable</code> to support deserialization of containing <code>HashMap</code>s. During deserialization, {@link
 * #hashCode()} is called by the containing map before a <code>Signature</code> has been set, leading to a null pointer. Implement Externalizable avoids this by
 * setting the Signature before <code>hashCode</code> is invoked.
 */
public class MethodInjectionSite extends InjectionSite implements Externalizable {
    private static final long serialVersionUID = -2222837362065034249L;
    private Signature signature;
    private int param;
    private transient Method method;

    public MethodInjectionSite(Method method, int param) {
        super(method.getParameterTypes()[param].getName());
        this.signature = new Signature(method);
        this.param = param;
        this.method = method;
    }

    public MethodInjectionSite() {
        // ctor for deserialization
    }

    /**
     * Returns the signature that identifies the method.
     *
     * @return the signature that identifies the method
     */
    public Signature getSignature() {
        return signature;
    }

    /**
     * Returns the index of the parameter being injected.
     * <p/>
     * This will be 0 for a normal setter method.
     *
     * @return the index of the parameter being injected
     */
    public int getParam() {
        return param;
    }

    /**
     * Returns the method or null if this class has been deserialized.
     *
     * @return the method or null
     */
    public Method getMethod() {
        return method;
    }

    public String toString() {
        return signature.toString() + '[' + param + ']';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MethodInjectionSite that = (MethodInjectionSite) o;

        return signature.equals(that.signature) && param == that.param;

    }

    public int hashCode() {
        return signature.hashCode() * 31 + param;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(signature);
        out.writeInt(param);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        signature = (Signature) in.readObject();
        param = in.readInt();
    }
}
