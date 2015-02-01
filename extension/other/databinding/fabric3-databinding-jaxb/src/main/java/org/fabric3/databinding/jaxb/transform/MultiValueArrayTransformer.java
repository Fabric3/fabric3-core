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
package org.fabric3.databinding.jaxb.transform;

import org.fabric3.api.host.Fabric3Exception;
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
    public Object[] transform(Object[] source, ClassLoader loader) throws Fabric3Exception {
        if (source == null) {
            return null;
        }
        if (source.length != transformers.length) {
            throw new Fabric3Exception("Source parameter length does not match the number of transformers");
        }
        for (int i = 0; i < source.length; i++) {
            source[i] = transformers[i].transform(source[i], loader);
        }
        return source;
    }
}
