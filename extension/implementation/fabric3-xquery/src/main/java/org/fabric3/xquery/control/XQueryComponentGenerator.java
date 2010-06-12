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
package org.fabric3.xquery.control;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.ComponentService;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalProperty;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalPropertyDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.policy.EffectivePolicy;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.xquery.provision.XQueryComponentDefinition;
import org.fabric3.xquery.provision.XQueryComponentSourceDefinition;
import org.fabric3.xquery.provision.XQueryComponentTargetDefinition;
import org.fabric3.xquery.scdl.XQueryComponentType;
import org.fabric3.xquery.scdl.XQueryImplementation;
import org.fabric3.xquery.scdl.XQueryProperty;
import org.fabric3.xquery.scdl.XQueryServiceContract;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class XQueryComponentGenerator implements ComponentGenerator<LogicalComponent<XQueryImplementation>> {
    private ContractMatcher matcher;

    public XQueryComponentGenerator(@Reference ContractMatcher matcher) {
        this.matcher = matcher;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<XQueryImplementation> component) throws GenerationException {
        ComponentDefinition<XQueryImplementation> definition = component.getDefinition();
        XQueryComponentDefinition physical = new XQueryComponentDefinition();
        physical.setLocation(definition.getImplementation().getLocation());
        physical.setContext(definition.getImplementation().getContext());
        processPropertyValues(component, physical);
        refineServiceContracts(component, physical);
        //create the functionDefinitions for services and references as well as property mapping

        return physical;
    }

    private void refineServiceContracts(LogicalComponent<XQueryImplementation> component, XQueryComponentDefinition physical) {
        Map<String, List<QName>> serviceFunctions = new HashMap<String, List<QName>>();
        Map<String, List<QName>> referenceFunctions = new HashMap<String, List<QName>>();
        Map<String, List<QName>> callbackFunctions = new HashMap<String, List<QName>>();
        //Map<String, List<QName>> referenceCallbackFunctions = new HashMap<String, List<QName>>();
        Map<String, ServiceContract> references = new HashMap<String, ServiceContract>();
        Map<String, ServiceContract> services = new HashMap<String, ServiceContract>();

        //TODO need to find a more optimal way to dynamically override service contracts
        //This builds up a map of service and reference service contracts to be used in case
        //the definition does not explicitly set one (the XQueryService contract can be too generic)
        for (LogicalComponent lc : component.getParent().getComponents()) {
            for (LogicalReference lr : (Collection<LogicalReference>) lc.getReferences()) {
                for (LogicalWire wire : component.getParent().getWires(lr)) {
                    URI targetUri = UriHelper.getDefragmentedName(wire.getTarget().getUri());
                    if (component.getUri().equals(targetUri)) {
                        String serviceName = wire.getTarget().getUri().getFragment();
                        services.put(serviceName, lr.getDefinition().getServiceContract());
                    }
                }
            }
        }

        for (LogicalReference lr : component.getReferences()) {
            for (LogicalWire wire : component.getParent().getWires(lr)) {
                URI sourceUri = UriHelper.getDefragmentedName(wire.getTarget().getUri());
                LogicalComponent lc = component.getParent().getComponent(sourceUri);
                String referenceName = wire.getTarget().getUri().getFragment();
                references.put(referenceName, lc.getService(referenceName).getDefinition().getServiceContract());
            }
        }

        ComponentDefinition<XQueryImplementation> definition = component.getDefinition();

        for (Map.Entry<String, ServiceDefinition> entry : definition.getImplementation().getComponentType().getServices().entrySet()) {
            String serviceName = entry.getKey();
            XQueryServiceContract service = (XQueryServiceContract) entry.getValue().getServiceContract();
            if (service.getQname() == null && "XQueryService".equals(serviceName)) {
                continue;
            }
            addFunctions(entry.getKey(), service, serviceFunctions);
            if (service.getCallbackContract() != null) {
                XQueryServiceContract callback = (XQueryServiceContract) service.getCallbackContract();
                addFunctions(callback.getQname().getLocalPart(), callback, callbackFunctions);
            }
            ComponentService compService = definition.getServices().get(serviceName);
            if (compService != null && compService.getServiceContract() != null) { //override the ServiceContract with a more specific type
                entry.getValue().setServiceContract(compService.getServiceContract());
            } else {//not explicitly set, obtain the reference to the service from the composite if available
                ServiceContract contract = services.get(serviceName);
                if (contract != null) {
                    entry.getValue().setServiceContract(contract);
                } else {
                    //System.out.println("Warning, unable to refine contract " + entry.getValue().getName());
                }
            }
        }
        for (Map.Entry<String, ReferenceDefinition> entry : definition.getImplementation().getComponentType().getReferences().entrySet()) {
            String referenceName = entry.getKey();
            XQueryServiceContract reference = (XQueryServiceContract) entry.getValue().getServiceContract();
            addFunctions(entry.getKey(), reference, referenceFunctions);
            ComponentReference compReference = definition.getReferences().get(referenceName);
            if (compReference != null && compReference.getServiceContract() != null) { //override the ServiceContract with a more specific type
                entry.getValue().setServiceContract(compReference.getServiceContract());
            } else {//not explicitly set, obtain the reference to the service from the composite if available
                ServiceContract contract = references.get(referenceName);
                if (contract != null) {
                    entry.getValue().setServiceContract(contract);
                } else {
                    //System.out.println("Warning, unable to refine contract " + entry.getValue().getName());
                }
            }
        }

        physical.setServiceFunctions(serviceFunctions);
        physical.setReferenceFunctions(referenceFunctions);
        physical.setCallbackFunctions(callbackFunctions);
    }

    private void processPropertyValues(LogicalComponent<XQueryImplementation> component, XQueryComponentDefinition physical) {
        for (LogicalProperty property : component.getAllProperties().values()) {
            Document document = property.getValue();
            if (document != null) {
                String name = property.getName();
                boolean many = property.isMany();
                PhysicalPropertyDefinition definition = new PhysicalPropertyDefinition(name, document, many);
                physical.setPropertyDefinition(definition);
            }
        }

        Map<String, QName> properties = new HashMap<String, QName>();
        ComponentDefinition<XQueryImplementation> definition = component.getDefinition();
        for (Map.Entry<String, Property> entry : definition.getImplementation().getComponentType().getProperties().entrySet()) {
            if (entry.getValue() instanceof XQueryProperty) {
                XQueryProperty property = (XQueryProperty) entry.getValue();
                properties.put(entry.getKey(), property.getVariableName());
            }
        }
        physical.setProperties(properties);
    }

    private void addFunctions(String name, XQueryServiceContract contract, Map<String, List<QName>> mappings) {
        List<QName> functions = new ArrayList<QName>();
        mappings.put(name, functions);
        for (Operation o : contract.getOperations()) {
            QName functionName = new QName(contract.getQname().getNamespaceURI(), o.getName(), contract.getQname().getPrefix());
            functions.add(functionName);
        }
    }

    public XQueryComponentSourceDefinition generateSource(LogicalReference reference, EffectivePolicy policy) throws GenerationException {
        XQueryComponentSourceDefinition sourceDefinition = new XQueryComponentSourceDefinition();
        sourceDefinition.setUri(reference.getUri());
        if (reference.getDefinition().getServiceContract().isConversational()) {
            sourceDefinition.setInteractionType(InteractionType.CONVERSATIONAL);
        }
        return sourceDefinition;
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalSourceDefinition generateCallbackSource(LogicalService service, EffectivePolicy policy) throws GenerationException {
        ServiceContract callbackContract = service.getDefinition().getServiceContract().getCallbackContract();
        XQueryComponentSourceDefinition sourceDefinition = new XQueryComponentSourceDefinition();
        LogicalComponent<XQueryImplementation> source = (LogicalComponent<XQueryImplementation>) service.getParent();
        XQueryComponentType type = source.getDefinition().getImplementation().getComponentType();
        String name = null;
        for (Map.Entry<String, ServiceDefinition> entry : type.getServices().entrySet()) {
            ServiceContract candidateContract = entry.getValue().getServiceContract();
            MatchResult result = matcher.isAssignableFrom(candidateContract, callbackContract, false);
            if (result.isAssignable()) {
                name = entry.getKey();
                break;
            }
        }
        if (name == null) {
            String interfaze = callbackContract.getQualifiedInterfaceName();
            throw new GenerationException("Callback  not found for type: " + interfaze, interfaze);
        }
        sourceDefinition.setUri(URI.create(source.getUri().toString() + "#" + name));
        sourceDefinition.setOptimizable(false);
        return sourceDefinition;
    }

    public PhysicalTargetDefinition generateTarget(LogicalService service, EffectivePolicy policy) throws GenerationException {
        XQueryComponentTargetDefinition targetDefinition = new XQueryComponentTargetDefinition();
        targetDefinition.setUri(service.getUri());
        return targetDefinition;
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) {
        throw new UnsupportedOperationException();
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException {
        XQueryComponentSourceDefinition sourceDefinition = new XQueryComponentSourceDefinition();
        sourceDefinition.setUri(resourceReference.getParent().getUri());
        return sourceDefinition;
    }
}
