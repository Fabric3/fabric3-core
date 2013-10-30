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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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
