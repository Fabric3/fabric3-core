/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.spring.runtime.tx;

import java.util.Map;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.jta.JtaTransactionManager;

import org.fabric3.datasource.spi.DataSourceRegistry;
import org.fabric3.jpa.api.EntityManagerFactoryResolver;
import org.fabric3.spring.spi.ApplicationContextListener;

import static org.fabric3.implementation.spring.api.SpringConstants.EMF_RESOLVER;
import static org.fabric3.implementation.spring.api.SpringConstants.TRX_ALIAS;

/**
 * If a JTA transaction manager and datasources are configured on the runtime, they will be aliased as <code>transactionManager</code> and their
 * datasource name respectively.
 */
@EagerInit
public class TxApplicationContextListener implements ApplicationContextListener {

    private TransactionManager tm;
    private DataSourceRegistry dataSourceRegistry;
    private EntityManagerFactoryResolver emfResolver;

    @Reference(required = false)
    public void setDataSourceRegistry(DataSourceRegistry dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
    }

    @Reference(required = false)
    public void setTm(TransactionManager tm) {
        this.tm = tm;
    }

    @Reference(required = false)
    public void setEmfBuilder(EntityManagerFactoryResolver emfResolver) {
        this.emfResolver = emfResolver;
    }

    public void onCreate(ConfigurableApplicationContext context) {
        if (tm != null) {
            JtaTransactionManager platformTm = new JtaTransactionManager(tm);
            context.getBeanFactory().registerSingleton(TRX_ALIAS, platformTm);
            for (Map.Entry<String, DataSource> entry : dataSourceRegistry.getDataSources().entrySet()) {
                context.getBeanFactory().registerSingleton(entry.getKey(), entry.getValue());
            }
            if (emfResolver != null) {
                context.getBeanFactory().registerSingleton(EMF_RESOLVER, emfResolver);
            }
        }
    }

    public void onDispose(ConfigurableApplicationContext context) {
        // no-op
    }


}