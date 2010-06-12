/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.resource.introspection;

import java.lang.reflect.Member;
import javax.sql.DataSource;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Resource;
import org.fabric3.model.type.component.ResourceReferenceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.resource.model.DataSourceResourceReference;
import org.fabric3.resource.spi.ResourceTypeHandler;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;

/**
 * Handles resource injection for <code>DataSource</code> types.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JSR250DataSourceTypeHandler implements ResourceTypeHandler {
    private ServiceContract contract;
    private JavaContractProcessor contractProcessor;

    public JSR250DataSourceTypeHandler(@Reference JavaContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
    }

    @Init
    public void init() {
        // introspect the interface once
        contract = contractProcessor.introspect(DataSource.class, new DefaultIntrospectionContext());
    }

    public ResourceReferenceDefinition createResourceReference(String resourceName, Resource annotation, Member member, IntrospectionContext context) {
        String dataSourceName = annotation.mappedName();
        if (dataSourceName.length() == 0) {
            MissingDataSourceName error = new MissingDataSourceName(member.getDeclaringClass());
            context.addError(error);
            return new DataSourceResourceReference(resourceName, contract, annotation.optional(), "error");
        }
        return new DataSourceResourceReference(resourceName, contract, annotation.optional(), dataSourceName);
    }
}