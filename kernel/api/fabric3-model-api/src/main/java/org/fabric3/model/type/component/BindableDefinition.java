package org.fabric3.model.type.component;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.model.type.AbstractPolicyAware;
import org.fabric3.model.type.ModelObject;

/**
 * A model object which can be configured with a binding.
 */

public abstract class BindableDefinition<P extends ModelObject> extends AbstractPolicyAware<P> {
    private static final long serialVersionUID = -7495919678050678596L;

    protected List<BindingDefinition> bindings = new ArrayList<BindingDefinition>();

    /**
     * Returns the bindings configured on this bindable.
     *
     * @return the bindings configured on this bindable
     */
    public List<BindingDefinition> getBindings() {
        return bindings;
    }

    /**
     * Adds a configured binding.
     *
     * @param binding the binding to be added
     */
    public void addBinding(BindingDefinition binding) {
        binding.setParent(this);
        this.bindings.add(binding);
    }


}
