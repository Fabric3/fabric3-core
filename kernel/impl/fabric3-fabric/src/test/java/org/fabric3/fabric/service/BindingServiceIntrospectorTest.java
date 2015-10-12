package org.fabric3.fabric.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import junit.framework.TestCase;
import org.fabric3.api.annotation.model.Binding;

/**
 *
 */
public class BindingServiceIntrospectorTest extends TestCase {
    private BindingServiceIntrospector introspector = new BindingServiceIntrospector();

    public void testExportsEndpoints() throws Exception {
        assertTrue(introspector.exportsEndpoints(TestService.class));
    }

    @SpecialBinding
    private class TestService {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Binding("binding.special")
    @interface SpecialBinding {
    }
}