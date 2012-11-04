/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.exist.runtime;

import org.fabric3.xquery.runtime.*;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;

import org.exist.source.URLSource;
import org.fabric3.exist.ExistDBInstance;
import org.fabric3.exist.ExistDBInstanceRegistry;
import org.fabric3.exist.transform.TransformerRegistry;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.xquery.provision.XQueryComponentDefinition;


/**
 * Instantiates a xquery component on a runtime node.
 * */
@EagerInit
public class ExistXQueryComponentBuilder implements ComponentBuilder<XQueryComponentDefinition, XQueryComponent> {

    private ComponentBuilderRegistry builderRegistry;
    private ExistDBInstanceRegistry dbRegistry;
    private ClassLoaderRegistry classLoaderRegistry;
    private TransformerRegistry trRegistry;

    public ExistXQueryComponentBuilder(@Reference ClassLoaderRegistry classLoaderRegistry,
            @Reference ComponentBuilderRegistry builderRegistry,
            @Reference ExistDBInstanceRegistry dbRegistry,
            @Reference TransformerRegistry trRegistry) {
        this.builderRegistry = builderRegistry;
        this.dbRegistry = dbRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.trRegistry=trRegistry;
    }

    @Init
    public void init() {
        builderRegistry.register(XQueryComponentDefinition.class, this);
    }

    @Destroy
    public void destroy() {
    }

    public XQueryComponent build(XQueryComponentDefinition definition) throws BuilderException {
        URI componentId = definition.getComponentId();
        QName groupId = definition.getDeployable();
        ExistDBInstance instance = dbRegistry.getInstance(definition.getContext());
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());
        URL locationURL = classLoader.getResource(definition.getLocation());
        if (locationURL == null) {
            throw new BuilderException(String.format("XQuery file %s not found for component %s",definition.getLocation(),definition.getComponentId())) {
            };
        }
        URLSource source = new URLSource(locationURL);
        ExistXQueryCompiler compiler = new ExistXQueryCompiler(classLoader, instance,trRegistry, source);
        ExistXQueryComponent component= new ExistXQueryComponent(componentId,
                groupId,
                compiler);

        compiler.includeServiceFunctionMappings(definition.getServiceFunctions());
        compiler.includeReferenceFunctionMappings(definition.getReferenceFunctions());
        compiler.includeCallbackFunctionMappings(definition.getCallbackFunctions());
        compiler.includePropertyMappings(definition.getProperties());
        compiler.linkPropertyValues(definition.getPropertyValues());
        return component;
    }
}
