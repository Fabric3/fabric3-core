package org.fabric3.tx;

import javax.transaction.TransactionManager;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.TransactionDecorator;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class JtaTransactionDecorator implements TransactionDecorator {
    private TxInterceptor interceptor;

    public JtaTransactionDecorator(@Reference TransactionManager transactionManager, @Monitor TxMonitor monitor) {
        interceptor = new TxInterceptor(transactionManager, TxAction.BEGIN, monitor);
    }

    public void transactional(InvocationChain chain) {
        chain.addInterceptor(interceptor);
    }
}
