/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
*/
package org.fabric3.databinding.jaxb.transform;

import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;

/**
 * Transformer that delegates to other transformers to convert an array of multiple types. If the source array is null, null will be returned.
 */
public class MultiValueArrayTransformer implements Transformer<Object[], Object[]> {
    private Transformer[] transformers;

    public MultiValueArrayTransformer(Transformer<?, ?>[] transformers) {
        this.transformers = transformers;
    }

    @SuppressWarnings({"unchecked"})
    public Object[] transform(Object[] source, ClassLoader loader) throws TransformationException {
        if (source == null) {
            return null;
        }
        if (source.length != transformers.length) {
            throw new TransformationException("Source parameter length does not match the number of transformers");
        }
        for (int i = 0; i < source.length; i++) {
            source[i] = transformers[i].transform(source[i], loader);
        }
        return source;
    }
}
