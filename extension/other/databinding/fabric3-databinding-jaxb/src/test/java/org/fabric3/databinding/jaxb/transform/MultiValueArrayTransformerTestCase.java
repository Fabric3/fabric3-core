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

import junit.framework.TestCase;

import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;

/**
 *
 */
public class MultiValueArrayTransformerTestCase extends TestCase {
    private static final Transformer<?, ?>[] TRANSFORMERS = new Transformer<?, ?>[]{new MockTransformer()};

    public void testNull() throws Exception {
        MultiValueArrayTransformer transformer = new MultiValueArrayTransformer(TRANSFORMERS);
        Object[] ret = transformer.transform(null, getClass().getClassLoader());
        assertNull(ret);

    }

    public void testTransform() throws Exception {
        MultiValueArrayTransformer transformer = new MultiValueArrayTransformer(TRANSFORMERS);
        Object[] ret = transformer.transform(new Object[]{1}, getClass().getClassLoader());
        assertEquals(1, ret[0]);

    }

    private static class MockTransformer implements Transformer<Object, Object> {

        public Object transform(Object o, ClassLoader loader) throws TransformationException {
            return o;
        }
    }
}
