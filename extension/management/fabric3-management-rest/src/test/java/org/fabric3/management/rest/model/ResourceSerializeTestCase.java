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
package org.fabric3.management.rest.model;

import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JsonMapperConfigurator;
import junit.framework.TestCase;

/**
 *
 */
public final class ResourceSerializeTestCase extends TestCase {
    private final static Annotations[] DEFAULT_ANNOTATIONS = {Annotations.JACKSON, Annotations.JAXB};
    private ObjectMapper mapper;

    public void testResourceSerialize() throws Exception {
        URL href = new URL("http://foo.com/resource");
        SelfLink link = new SelfLink(href);
        Resource resource = new Resource(link);
        resource.setProperty("foo", "bar");
        String serialized = mapper.writeValueAsString(resource);

        Resource deserialized = mapper.readValue(serialized, Resource.class);
        Link deserializedLink = deserialized.getSelfLink();
        assertEquals("self", deserializedLink.getName());
        assertEquals("self", deserializedLink.getRel());
        assertEquals(href, deserializedLink.getHref());
        assertEquals(1, deserialized.getProperties().size());
        assertEquals("bar", deserialized.getProperties().get("foo"));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JsonMapperConfigurator configurator = new JsonMapperConfigurator(null, DEFAULT_ANNOTATIONS);
        mapper = configurator.getDefaultMapper();
    }
}
