package org.fabric3.contribution.introspector;

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.spi.contribution.ReferenceIntrospector;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Determines if a composite references another artifact. Currently only composite references from child component implementations
 * (implementation.composite) and includes are supported.
 */
@EagerInit
public class CompositeReferenceIntrospector implements ReferenceIntrospector<QNameSymbol, Composite> {


    public boolean references(ResourceElement<QNameSymbol, Composite> referred, ResourceElement<?, ?> refers) {
        if (!(refers.getValue() instanceof Composite)) {
            return false;
        }
        Composite composite = (Composite) refers.getValue();
        QName name = referred.getSymbol().getKey();
        for (Include include : composite.getIncludes().values()) {
            if (name.equals(include.getIncluded().getName())) {
                return true;
            }
        }
        for (Component<?> component : composite.getComponents().values()) {
            if (component.getComponentType() instanceof Composite) {
                Composite type = (Composite) component.getComponentType();
                if (name.equals(type.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
