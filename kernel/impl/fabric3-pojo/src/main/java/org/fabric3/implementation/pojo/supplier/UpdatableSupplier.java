package org.fabric3.implementation.pojo.supplier;

import java.util.function.Supplier;

/**
 * A supplier that can be updated.
 */
public class UpdatableSupplier<T> implements Supplier<T> {
    private Supplier<T> delegate;

    public UpdatableSupplier(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    public T get() {
        return delegate.get();
    }

    public void update(Supplier<T> delegate) {
        this.delegate = delegate;
    }
}
