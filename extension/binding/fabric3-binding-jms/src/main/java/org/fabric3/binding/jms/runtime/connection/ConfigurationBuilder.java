package org.fabric3.binding.jms.runtime.connection;

import javax.jms.ConnectionFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryType;
import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.binding.jms.spi.introspection.ConnectionFactoryConfigurationParser;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreatorRegistry;
import org.fabric3.binding.jms.spi.runtime.manager.ConnectionFactoryManager;
import org.fabric3.binding.jms.spi.runtime.provider.DefaultConnectionFactoryBuilder;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.binding.jms.common.JmsConnectionConstants.DEFAULT_CONNECTION_FACTORY;
import static org.fabric3.binding.jms.common.JmsConnectionConstants.DEFAULT_XA_CONNECTION_FACTORY;

/**
 * Creates connection factories configured in the runtime system configuration in the form of:
 * <pre>
 * &lt;jms&gt;
 *    &lt;connection.factories&gt;
 *       &lt;connection.factory.activemq name='testFactory' broker.url='...' type='xa'&gt;
 *       ...
 *       &lt;/connection.factory.activemq&gt;
 *    &lt;/connection.factories&gt;
 * &lt;/jms&gt;
 * </pre>
 * <p/>
 * Note that the unqualified forms for connection factory and template definitions may be used: <code>&lt;connection.factory ...&gt;</code>. In this case, if
 * more than one JMS provider is present, one will be selected.
 */
@EagerInit
public class ConfigurationBuilder {
    private ConnectionFactoryCreatorRegistry creatorRegistry;
    private ConnectionFactoryManager manager;

    private String defaultProvider;

    private Map<String, ConnectionFactoryConfigurationParser> parsers = Collections.emptyMap();
    private Map<String, DefaultConnectionFactoryBuilder> defaultBuilders = Collections.emptyMap();

    private List<ConnectionFactoryConfiguration> factoryConfigurations = new ArrayList<>();

    private List<ConnectionFactory> factories = new ArrayList<>();

    public ConfigurationBuilder(@Reference ConnectionFactoryCreatorRegistry creatorRegistry, @Reference ConnectionFactoryManager manager) {
        this.creatorRegistry = creatorRegistry;
        this.manager = manager;
    }

    @Reference(required = false)
    public void setParsers(Map<String, ConnectionFactoryConfigurationParser> parsers) {
        this.parsers = parsers;
    }

    @Reference(required = false)
    public void setDefaultBuilders(Map<String, DefaultConnectionFactoryBuilder> defaultBuilders) {
        this.defaultBuilders = defaultBuilders;
    }

    @Property(required = false)
    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    @Property(required = false)
    public void setConnectionFactories(XMLStreamReader reader) throws XMLStreamException, ContainerException {
        factoryConfigurations.clear();
        parseConfigurations(factoryConfigurations, reader);
    }

    @Init
    public void init() throws ContainerException {
        // initialize and register the connection factories
        for (ConnectionFactoryConfiguration configuration : factoryConfigurations) {
            ConnectionFactory factory = creatorRegistry.create(configuration);
            manager.register(configuration.getName(), factory, getProperties(configuration));
            factories.add(factory);
        }

        // create default factories and templates
        if (!defaultBuilders.isEmpty()) {
            DefaultConnectionFactoryBuilder builder;
            if (defaultProvider != null) {
                builder = defaultBuilders.get(defaultProvider);
                if (builder == null) {
                    throw new ContainerException("Unable to create default connection factories. Provider not found: " + defaultProvider);
                }
            } else {
                builder = defaultBuilders.values().iterator().next();
            }
            ConnectionFactoryConfiguration localConfig = builder.createDefaultFactory(DEFAULT_CONNECTION_FACTORY, ConnectionFactoryType.LOCAL);
            ConnectionFactoryConfiguration xaConfig = builder.createDefaultFactory(DEFAULT_XA_CONNECTION_FACTORY, ConnectionFactoryType.XA);

            if (manager.get(DEFAULT_CONNECTION_FACTORY) == null) {
                // default connection factory was not configured, create one
                ConnectionFactory factory = creatorRegistry.create(localConfig);
                manager.register(DEFAULT_CONNECTION_FACTORY, factory, getProperties(localConfig));
            }

            if (manager.get(DEFAULT_XA_CONNECTION_FACTORY) == null) {
                // default XA connection factory was not configured, create one
                ConnectionFactory xaFactory = creatorRegistry.create(xaConfig);
                manager.register(DEFAULT_XA_CONNECTION_FACTORY, xaFactory, getProperties(localConfig));
            }
        }

    }

    @Destroy
    public void destroy() throws ContainerException {
        for (ConnectionFactoryConfiguration configuration : factoryConfigurations) {
            manager.unregister(configuration.getName());
        }

        Iterator<ConnectionFactory> iterator = factories.iterator();
        while (iterator.hasNext()) {
            ConnectionFactory factory = iterator.next();
            creatorRegistry.release(factory);
            iterator.remove();
        }
    }

    /**
     * Parses connection factory and connection factory templates from an XML stream.
     *
     * @param configurations the collection to populate
     * @param reader         the XML stream
     * @throws XMLStreamException            if there is an error parsing the stream
     * @throws ContainerException if the configuration contains an error or is invalid
     */
    private void parseConfigurations(List<ConnectionFactoryConfiguration> configurations, XMLStreamReader reader)
            throws XMLStreamException, ContainerException {
        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    String name = reader.getName().getLocalPart();
                    if (name.equals("connection.factory")) {
                        // no factory specified, pick the first one
                        if (parsers.isEmpty()) {
                            throw new ContainerException("JMS provider not installed");
                        }
                        String provider = reader.getAttributeValue(null, "provider");
                        ConnectionFactoryConfigurationParser parser;
                        if (provider == null) {
                            parser = parsers.values().iterator().next();
                        } else {
                            parser = parsers.get(provider);
                            if (parser == null) {
                                throw new ContainerException("JMS provider not installed: " + provider);
                            }
                        }

                        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
                        ConnectionFactoryConfiguration configuration = parser.parse(reader, context);
                        checkErrors(context);
                        configurations.add(configuration);
                    }

                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    return;
            }
        }
    }

    private void checkErrors(DefaultIntrospectionContext context) throws ContainerException {
        if (context.hasErrors()) {
            StringBuilder builder = new StringBuilder();
            builder.append("The following errors were found:\n");
            for (ValidationFailure error : context.getErrors()) {
                builder.append(error.getMessage()).append("\n");
            }
            throw new ContainerException(builder.toString());
        }
    }

    private Map<String, String> getProperties(ConnectionFactoryConfiguration configuration) {
        Properties properties = configuration.getFactoryProperties();
        Map<String, String> factoryProperties = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            factoryProperties.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return factoryProperties;
    }

}
