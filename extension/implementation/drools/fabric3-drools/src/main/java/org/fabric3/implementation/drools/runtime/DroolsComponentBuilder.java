/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.implementation.drools.runtime;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.runtime.StatelessKnowledgeSession;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.implementation.drools.provision.DroolsComponentDefinition;
import org.fabric3.implementation.drools.provision.DroolsPropertyDefinition;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilder;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.util.ParamTypes;

/**
 * Builds a DroolsComponent from a physical definition.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DroolsComponentBuilder implements ComponentBuilder<DroolsComponentDefinition, DroolsComponent> {
    private ClassLoaderRegistry classLoaderRegistry;
    private PropertyObjectFactoryBuilder propertyBuilder;

    public DroolsComponentBuilder(@Reference PropertyObjectFactoryBuilder propertyBuilder,
                                  @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.propertyBuilder = propertyBuilder;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public DroolsComponent build(DroolsComponentDefinition definition) throws BuilderException {
        URI classLoaderId = definition.getClassLoaderId();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);

        KnowledgeBaseConfiguration configuration = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(new Properties(), classLoader);
        KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(configuration);
        knowledgeBase.addKnowledgePackages(definition.getPackages());

        URI componentUri = definition.getComponentUri();
        QName deployable = definition.getDeployable();
        Map<String, KnowledgeInjector<StatelessKnowledgeSession>> injectors = createInjectors(definition, classLoader);
        return new DroolsComponent(componentUri, knowledgeBase, injectors, deployable);
        // TODO hook into management
    }


    public void dispose(DroolsComponentDefinition definition, DroolsComponent component) throws BuilderException {
        // no-op
    }

    private Map<String, KnowledgeInjector<StatelessKnowledgeSession>> createInjectors(DroolsComponentDefinition definition, ClassLoader classLoader)
            throws BuilderException {
        Map<String, KnowledgeInjector<StatelessKnowledgeSession>> injectors =
                new ConcurrentHashMap<String, KnowledgeInjector<StatelessKnowledgeSession>>();

        List<DroolsPropertyDefinition> propertyDefinitions = definition.getDroolsPropertyDefinitions();
        for (DroolsPropertyDefinition propertyDefinition : propertyDefinitions) {
            ObjectFactory<?> factory = createPropertyFactories(propertyDefinition, classLoader);
            String name = propertyDefinition.getName();
            StatelessInjector injector = new StatelessInjector(name, factory);
            injectors.put(name, injector);
        }
        return injectors;
    }

    private ObjectFactory<?> createPropertyFactories(DroolsPropertyDefinition definition, ClassLoader classLoader) throws BuilderException {
        try {
            String name = definition.getName();
            Document value = definition.getValue();
            Class<?> type = classLoader.loadClass(definition.getType());
            DataType<?> dataType = getDataType(type);

            boolean many = definition.isMany();
            return propertyBuilder.createFactory(name, dataType, value, many, classLoader);
        } catch (ClassNotFoundException e) {
            throw new BuilderException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private DataType<?> getDataType(Class<?> type) {
        if (type.isPrimitive()) {
            // convert primitive representation to its object equivalent
            type = ParamTypes.PRIMITIVE_TO_OBJECT.get(type);
        }
        return new JavaClass(type);
    }

}
