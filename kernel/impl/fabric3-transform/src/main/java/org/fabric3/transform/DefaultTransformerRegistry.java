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
package org.fabric3.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerFactory;
import org.fabric3.spi.transform.TransformerRegistry;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default TransformerRegistry implementation.
 */
public class DefaultTransformerRegistry implements TransformerRegistry {
    private static final Comparator<TransformerFactory> COMPARATOR = new Comparator<TransformerFactory>() {
        public int compare(TransformerFactory first, TransformerFactory second) {
            return first.getOrder() - second.getOrder();
        }
    };

    // cache of single type transformers
    private Map<Key, SingleTypeTransformer<?, ?>> transformers = new HashMap<>();

    // cache of transformer factories
    private List<TransformerFactory> factories = new ArrayList<>();

    @Reference(required = false)
    public void setTransformers(List<SingleTypeTransformer<?, ?>> transformers) {
        for (SingleTypeTransformer<?, ?> transformer : transformers) {
            Key pair = new Key(transformer.getSourceType(), transformer.getTargetType());
            this.transformers.put(pair, transformer);
        }
    }

    @Reference(required = false)
    public void setFactories(List<TransformerFactory> factories) {
        List<TransformerFactory> sorted = new ArrayList<>(factories);
        Collections.sort(sorted, COMPARATOR);
        this.factories = sorted;
    }

    public Transformer<?, ?> getTransformer(DataType source, DataType target, List<Class<?>> inTypes, List<Class<?>> outTypes) throws Fabric3Exception {
        Key key = new Key(source, target);
        Transformer<?, ?> transformer = transformers.get(key);
        if (transformer != null) {
            return transformer;
        }
        for (TransformerFactory factory : factories) {
            boolean canTransform = factory.canTransform(source, target);
            if (canTransform) {
                return factory.create(source, target, inTypes, outTypes);
            }
        }
        return null;
    }

    private static class Key {
        private final DataType source;
        private final DataType target;

        public Key(DataType source, DataType target) {
            this.source = source;
            this.target = target;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Key that = (Key) o;

            return source.equals(that.source) && target.equals(that.target);

        }

        public int hashCode() {
            int result;
            result = source.hashCode();
            result = 31 * result + target.hashCode();
            return result;
        }
    }

}
