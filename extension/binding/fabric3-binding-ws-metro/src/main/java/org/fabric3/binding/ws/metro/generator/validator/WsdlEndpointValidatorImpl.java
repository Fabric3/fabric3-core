package org.fabric3.binding.ws.metro.generator.validator;

import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.binding.ws.metro.provision.AbstractEndpointDefinition;
import org.fabric3.binding.ws.model.WsBindingDefinition;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.wsdl.contribution.BindingSymbol;
import org.fabric3.wsdl.contribution.PortSymbol;
import org.fabric3.wsdl.contribution.WsdlServiceContractSymbol;
import org.fabric3.wsdl.model.WsdlServiceContract;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class WsdlEndpointValidatorImpl implements WsdlEndpointValidator {
    private static final String SOAP_HTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";

    private MetaDataStore store;
    private ContractMatcher matcher;
    private boolean enabled;  // default is not enabled

    public WsdlEndpointValidatorImpl(@Reference MetaDataStore store, @Reference ContractMatcher matcher) {
        this.store = store;
        this.matcher = matcher;
    }

    @Property(required = false)
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void validate(URI contributionUri, LogicalBinding<WsBindingDefinition> binding, AbstractEndpointDefinition endpointDefinition)
            throws EndpointValidationException {
        if (!enabled) {
            return;
        }
        try {
            ServiceContract otherContract = binding.getParent().getServiceContract();

            DefaultIntrospectionContext context = new DefaultIntrospectionContext();
            PortSymbol portSymbol = new PortSymbol(endpointDefinition.getPortName());
            QName name = store.resolve(contributionUri, Port.class, portSymbol, context).getValue().getBinding().getPortType().getQName();

            WsdlServiceContractSymbol contractSymbol = new WsdlServiceContractSymbol(name);
            WsdlServiceContract contract = store.resolve(contributionUri, WsdlServiceContract.class, contractSymbol, context).getValue();

            MatchResult result = matcher.isAssignableFrom(contract, otherContract, true);
            if (!result.isAssignable()) {
                throw new EndpointValidationException(result.getError());
            }
        } catch (StoreException e) {
            throw new EndpointValidationException(e);
        }
    }

    public void validateBinding(URI contributionUri, LogicalBinding<WsBindingDefinition> binding, QName bindingName) throws EndpointValidationException {
        if (!enabled) {
            return;
        }
        try {

            // validate contracts
            ServiceContract otherContract = binding.getParent().getServiceContract();

            DefaultIntrospectionContext context = new DefaultIntrospectionContext();
            BindingSymbol bindingSymbol = new BindingSymbol(bindingName);
            Binding wsdlBinding = store.resolve(contributionUri, Binding.class, bindingSymbol, context).getValue();
            QName portTypeName = wsdlBinding.getPortType().getQName();

            WsdlServiceContractSymbol contractSymbol = new WsdlServiceContractSymbol(portTypeName);
            WsdlServiceContract contract = store.resolve(contributionUri, WsdlServiceContract.class, contractSymbol, context).getValue();

            MatchResult result = matcher.isAssignableFrom(otherContract, contract, true);
            if (!result.isAssignable()) {
                throw new EndpointValidationException(result.getError());
            }

            // validate binding type
            for (Object element : wsdlBinding.getExtensibilityElements()) {
                if (element instanceof SOAPBinding) {
                    SOAPBinding soapBinding = (SOAPBinding) element;
                    if (!SOAP_HTTP_TRANSPORT.equals(soapBinding.getTransportURI())) {
                        throw new EndpointValidationException("Invalid SOAP binding transport specified for: " + binding.getParent().getUri());
                    }
                } else if (element instanceof SOAP12Binding) {
                    if (binding.getIntents().contains(new QName(Constants.SCA_NS, "SOAP.v1_1"))) {
                        throw new EndpointValidationException("Invalid intents configuration: SOAP 1.1 and SOAP 1.2 specified");
                    }
                }
            }

        } catch (StoreException e) {
            throw new EndpointValidationException(e);
        }

    }
}
