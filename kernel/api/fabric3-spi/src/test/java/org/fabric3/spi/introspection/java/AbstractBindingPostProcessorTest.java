package org.fabric3.spi.introspection.java;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.annotation.model.Binding;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 */
public class AbstractBindingPostProcessorTest extends TestCase {
    private TestBindingProcessor processor;
    private IntrospectionContext context;

    public void testSingleBindingAnnotationOnReference() throws Exception {
        EasyMock.expect(processor.info.getEnvironment()).andReturn("production");
        EasyMock.replay(processor.info);

        Field field = TestComponent.class.getField("singleBinding");

        Reference reference = new Reference("singleBinding");
        reference.setServiceContract(new JavaServiceContract());

        processor.processBindingAnnotations(field, reference, TestComponent.class, context);

        assertEquals(1, reference.getBindings().size());
    }

    public void testMultipleBindingAnnotationsOnReference() throws Exception {
        EasyMock.expect(processor.info.getEnvironment()).andReturn("production").atLeastOnce();
        EasyMock.replay(processor.info);

        Field field = TestComponent.class.getField("multipleBindings");
        Reference reference = new Reference("multipleBindings");
        reference.setServiceContract(new JavaServiceContract());

        processor.processBindingAnnotations(field, reference, TestComponent.class, context);

        assertEquals(1, reference.getBindings().size());
    }

    public void testMultipleBindingMetaAnnotationsOnReference() throws Exception {
        EasyMock.expect(processor.info.getEnvironment()).andReturn("production").atLeastOnce();
        EasyMock.replay(processor.info);

        Field field = TestComponent.class.getField("metaAnnotation");
        Reference reference = new Reference("metaAnnotation");
        reference.setServiceContract(new JavaServiceContract());

        processor.processBindingAnnotations(field, reference, TestComponent.class, context);

        assertEquals(1, reference.getBindings().size());
    }

    public void setUp() throws Exception {
        super.setUp();
        processor = new TestBindingProcessor();
        processor.info = EasyMock.createNiceMock(HostInfo.class);
        context = new DefaultIntrospectionContext();
    }

    private class TestComponent {

        @TestBinding(environments = {"test"})
        @TestBinding(environments = {"production"})
        public String multipleBindings;

        @TestBinding
        public String singleBinding;

        @MetaAnnotation
        public String metaAnnotation;

    }

    @Target({TYPE, FIELD, METHOD, PARAMETER})
    @Retention(RUNTIME)
    @TestBinding(environments = {"test"})
    @TestBinding(environments = {"production"})
    public @interface MetaAnnotation {
    }

    @Target({TYPE, FIELD, METHOD, PARAMETER})
    @Retention(RUNTIME)
    @Binding("{foo}binding.bar")
    @Repeatable(TestBindings.class)
    public @interface TestBinding {
        Class<?> service() default Void.class;

        String[] environments() default {};
    }

    @Target({TYPE, FIELD, METHOD, PARAMETER})
    @Retention(RUNTIME)
    public @interface TestBindings {
        TestBinding[] value();
    }

    private class TestBindingProcessor extends AbstractBindingPostProcessor<TestBinding> {

        protected TestBindingProcessor() {
            super(TestBinding.class);
        }

        protected org.fabric3.api.model.type.component.Binding processService(TestBinding annotation,
                                                                              Service<ComponentType> service,
                                                                              InjectingComponentType componentType,
                                                                              Class<?> implClass,
                                                                              IntrospectionContext context) {
            return isActiveForEnvironment(annotation.environments()) ? new TestBindingModel() : null;

        }

        protected org.fabric3.api.model.type.component.Binding processServiceCallback(TestBinding annotation,
                                                                                      Service<ComponentType> service,
                                                                                      InjectingComponentType componentType,
                                                                                      Class<?> implClass,
                                                                                      IntrospectionContext context) {
            return null;

        }

        protected org.fabric3.api.model.type.component.Binding processReference(TestBinding annotation,
                                                                                Reference reference,
                                                                                Class<?> implClass,
                                                                                IntrospectionContext context) {
            return isActiveForEnvironment(annotation.environments()) ? new TestBindingModel() : null;

        }

        protected org.fabric3.api.model.type.component.Binding processReferenceCallback(TestBinding annotation,
                                                                                        Reference reference,
                                                                                        Class<?> implClass,
                                                                                        IntrospectionContext context) {

            return null;
        }
    }

    private class TestBindingModel extends org.fabric3.api.model.type.component.Binding {

        public TestBindingModel() {
            super(null, null);
        }
    }
}