package org.fabric3.introspection.java.annotation;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.model.type.java.InjectingComponentType;

/**
 * Processes the {@link Key} annotation on component implementation classes.
 */
public class KeyProcessor extends AbstractAnnotationProcessor<Key> {

    public KeyProcessor() {
        super(Key.class);
    }

    public void visitType(Key annotation, Class<?> type, InjectingComponentType componentType, IntrospectionContext context) {
        if (annotation.value().length() == 0) {
            context.addError(new InvalidAnnotation("A value must be specified for @Key", type));
            return;
        }
        componentType.setKey(annotation.value());
    }
}
