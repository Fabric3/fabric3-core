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
import java.lang.reflect.Constructor;

import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.api.model.type.java.Signature;

/**
 * Represents a constructor that is injected into when a component implementation instance is instantiated.
 */
public class ConstructorInjectionSite extends InjectionSite implements Externalizable {
    private static final long serialVersionUID = -6543986170145816234L;
    private Signature signature;
    private int param;
    private transient Constructor constructor;

    public ConstructorInjectionSite(Constructor<?> constructor, int param) {
        super(constructor.getParameterTypes()[param].getName());
        this.signature = new Signature(constructor);
        this.param = param;
        this.constructor = constructor;
    }

    public ConstructorInjectionSite(Signature signature, int param) {
        super(signature.getParameterTypes().get(param));
        this.signature = signature;
        this.param = param;
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
     *
     * @return the index of the parameter being injected
     */
    public int getParam() {
        return param;
    }

    /**
     * Returns the constructor, or null if this class has been deserialized.
     *
     * @return the constructor or null
     */
    public Constructor getConstructor() {
        return constructor;
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

        ConstructorInjectionSite that = (ConstructorInjectionSite) o;

        return param == that.param && signature.equals(that.signature);

    }

    public int hashCode() {
        return 31 * signature.hashCode() + param;
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