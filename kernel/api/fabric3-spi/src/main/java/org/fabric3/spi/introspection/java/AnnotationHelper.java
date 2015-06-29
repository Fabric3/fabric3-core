package org.fabric3.spi.introspection.java;

import java.lang.annotation.Annotation;

/**
 *
 */
public class AnnotationHelper {

    /**
     * Returns the annotation if it is present on the type or it is present on one of the type annotations.
     *
     * @param annotationClass the annotation type
     * @param type            the type
     * @return the annotation or null if not present
     */
    public static <A extends Annotation> A findAnnotation(Class<A> annotationClass, Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            if (annotationClass.equals(annotation.annotationType())) {
                return annotationClass.cast(annotation);
            }
            for (Annotation metaAnnotation : annotation.annotationType().getDeclaredAnnotations()) {
                if (annotationClass.equals(metaAnnotation.annotationType())) {
                    return annotationClass.cast(metaAnnotation);
                }
            }
        }
        return null;
    }

    /**
     * Returns true if the annotation is present on the type or it is present on one of the type annotations.
     *
     * @param annotationClass the annotation type
     * @param type            the type
     * @return true if present
     */
    public static <A extends Annotation> boolean isPresent(Class<A> annotationClass, Class<?> type) {
        return findAnnotation(annotationClass, type) != null;
    }

    private AnnotationHelper() {
    }
}
