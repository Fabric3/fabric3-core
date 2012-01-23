package org.fabric3.spi.contribution;

/**
 * Introspects a {@link ResourceElement} to determine if it references another resource element. Implementations are responsible for a specific
 * resource type, such as a composite, XSD, or WSDL.
 *
 * @version $Rev$ $Date$
 */
public interface ReferenceIntrospector<S extends Symbol, V> {

    /**
     * Returns true if the refers resource element references the referred resource element.
     *
     * @param referred the referenced resource element
     * @param refers   the resource element to check for references
     * @return true if the refers resource element references the referred resource element
     */
    boolean references(ResourceElement<S, V> referred, ResourceElement<?, ?> refers);

}
