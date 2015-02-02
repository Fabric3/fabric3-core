package org.fabric3.api.model.type.component;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.ModelObject;

/**
 * A model object which can be configured with a binding.
 */
public abstract class Bindable<P extends ModelObject> extends ModelObject<P> {

    protected List<Binding> bindings = new ArrayList<>();

    /**
     * Returns the bindings configured on this bindable.
     *
     * @return the bindings configured on this bindable
     */
    public List<Binding> getBindings() {
        return bindings;
    }

    /**
     * Adds a configured binding.
     *
     * @param binding the binding to be added
     */
    public void addBinding(Binding binding) {
        binding.setParent(this);
        this.bindings.add(binding);
    }

}
