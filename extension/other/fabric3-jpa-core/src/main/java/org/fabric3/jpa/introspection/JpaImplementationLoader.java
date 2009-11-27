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
package org.fabric3.jpa.introspection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.Namespaces;
import org.fabric3.implementation.java.introspection.JavaImplementationProcessor;
import org.fabric3.implementation.java.model.JavaImplementation;
import org.fabric3.jpa.ConversationalDaoImpl;
import org.fabric3.jpa.scdl.PersistenceContextResource;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Implementation loader for JPA component.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JpaImplementationLoader implements TypeLoader<JavaImplementation> {

    public static final QName IMPLEMENTATION_JPA = new QName(Namespaces.IMPLEMENTATION, "implementation.jpa");

    private final JavaImplementationProcessor implementationProcessor;
    private final ServiceContract factoryServiceContract;

    public JpaImplementationLoader(@Reference JavaImplementationProcessor implementationProcessor, @Reference JavaContractProcessor contractProcessor) {
        this.implementationProcessor = implementationProcessor;
        IntrospectionContext context = new DefaultIntrospectionContext();
        factoryServiceContract = contractProcessor.introspect(EntityManager.class, context);
        assert !context.hasErrors();  // should not happen
    }

    /**
     * Creates the instance of the implementation type.
     *
     * @param reader  Stax XML stream reader used for reading data.
     * @param context Introspection context.
     * @return An instance of the JPA implemenation.
     */
    public JavaImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);

        try {
            JavaImplementation implementation = new JavaImplementation();
            String persistenceUnit = reader.getAttributeValue(null, "persistenceUnit");
            if (persistenceUnit == null) {
                MissingAttribute failure = new MissingAttribute("Missing attribute: persistenceUnit", reader);
                context.addError(failure);
                return implementation;
            }

            implementation.setImplementationClass(ConversationalDaoImpl.class.getName());

            implementationProcessor.introspect(implementation, context);
            InjectingComponentType pojoComponentType = implementation.getComponentType();

            PersistenceContextResource resource = new PersistenceContextResource(
                    "unit", persistenceUnit, PersistenceContextType.TRANSACTION, factoryServiceContract, false);
            FieldInjectionSite site = new FieldInjectionSite(ConversationalDaoImpl.class.getDeclaredField("entityManager"));
            pojoComponentType.add(resource, site);
            LoaderUtil.skipToEndElement(reader);

            return implementation;

        } catch (NoSuchFieldException e) {
            // this should not happen
            throw new AssertionError(e);
        }

    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"persistenceUnit".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }


}
