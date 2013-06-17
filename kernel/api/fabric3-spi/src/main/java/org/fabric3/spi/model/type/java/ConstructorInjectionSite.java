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

import java.lang.reflect.Constructor;

/**
 * Represents a constructor that is injected into when a component implementation instance is instantiated.
 */
public class ConstructorInjectionSite extends InjectionSite {
    private static final long serialVersionUID = -6543986170145816234L;
    private Signature signature;
    private int param;

    public ConstructorInjectionSite(Constructor<?> constructor, int param) {
        super(constructor.getParameterTypes()[param].getName());
        this.signature = new Signature(constructor);
        this.param = param;
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

    public String toString() {
        return signature.toString() + '[' + param + ']';
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConstructorInjectionSite that = (ConstructorInjectionSite) o;

        return param == that.param && signature.equals(that.signature);

    }

    public int hashCode() {
        return 31 * signature.hashCode() + param;
    }
}