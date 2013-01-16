package org.fabric3.binding.jms.runtime.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryConfiguration;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreationException;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreatorRegistry;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryTemplateRegistry;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryType;
import org.fabric3.binding.jms.spi.runtime.manager.ConnectionFactoryManager;
import org.fabric3.binding.jms.spi.runtime.manager.FactoryRegistrationException;
import org.fabric3.binding.jms.spi.runtime.provider.ConnectionFactoryConfigurationParser;
import org.fabric3.binding.jms.spi.runtime.provider.DefaultConnectionFactoryBuilder;
import org.fabric3.binding.jms.spi.runtime.provider.InvalidConfigurationException;

import static org.fabric3.binding.jms.common.JmsConnectionConstants.DEFAULT_CONNECTION_FACTORY;
import static org.fabric3.binding.jms.common.JmsConnectionConstants.DEFAULT_XA_CONNECTION_FACTORY;

/**
 * Creates connection factories and templates configured in the runtime system configuration in the form of:
 * <pre>
 * &lt;jms&gt;
 *    &lt;connection.factories&gt;
 *       &lt;connection.factory.activemq name='testFactory' broker.url='...' type='xa'&gt;
 *       ...
 *       &lt;/connection.factory.activemq&gt;
 *    &lt;/connection.factories&gt;
 *    &lt;connection.factory.templates&gt;
 *       &lt;connection.factory.activemq name='testFactory' broker.url='...' type='xa'&gt;
 *       ...
 *       &lt;/connection.factory.activemq&gt;
 *    &lt;/connection.factory.templates&gt;
 * &lt;/jms&gt;
 * </pre>
 * <p/>
 * Note that the unqualified forms for connection factory and template definitions may be used: <code>&lt;connection.factory ...&gt;</code>. In this
 * case, if more than one JMS provider is present, one will be selected.
 */
@EagerInit
public class ConfigurationBuilder {
    private ConnectionFactoryTemplateRegistry templateRegistry;
    private ConnectionFactoryCreatorRegistry creatorRegistry;
    private ConnectionFactoryManager manager;

    private String defaultProvider;

    private Map<String, ConnectionFactoryConfigurationParser> parsers = Collections.emptyMap();
    private Map<String, DefaultConnectionFactoryBuilder> defaultBuilders = Collections.emptyMap();

    private List<ConnectionFactoryConfiguration> factoryConfigurations = new ArrayList<ConnectionFactoryConfiguration>();
    private List<ConnectionFactoryConfiguration> templateConfigurations = new ArrayList<ConnectionFactoryConfiguration>();

    private List<ConnectionFactory> factories = new ArrayList<ConnectionFactory>();

    public ConfigurationBuilder(@Reference ConnectionFactoryTemplateRegistry templateRegistry,
                                @Reference ConnectionFactoryCreatorRegistry creatorRegistry,
                                @Reference ConnectionFactoryManager manager) {
        this.templateRegistry = templateRegistry;
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
    public void setConnectionFactories(XMLStreamReader reader) throws XMLStreamException, InvalidConfigurationException {
        factoryConfigurations.clear();
        parseConfigurations(factoryConfigurations, reader);
    }

    @Property(required = false)
    public void setConnectionFactoryTemplates(XMLStreamReader reader) throws XMLStreamException, InvalidConfigurationException {
        templateConfigurations.clear();
        parseConfigurations(templateConfigurations, reader);
    }

    @Init
    public void init() throws FactoryRegistrationException, ConnectionFactoryCreationException {
        // register templates
        for (ConnectionFactoryConfiguration configuration : templateConfigurations) {
            templateRegistry.registerTemplate(configuration);
        }


        // initialize and register the connection factories
        for (ConnectionFactoryConfiguration configuration : factoryConfigurations) {
            ConnectionFactory factory = creatorRegistry.create(configuration);
            manager.register(configuration.getName(), factory);
            factories.add(factory);
        }

        // create default factories and templates
        if (!defaultBuilders.isEmpty()) {
            DefaultConnectionFactoryBuilder builder;
            if (defaultProvider != null) {
                builder = defaultBuilders.get(defaultProvider);
                if (builder == null) {
                    throw new ConnectionFactoryCreationException("Unable to create default connection factories. Provider not found: " + defaultProvider);
                }
            } else {
                builder = defaultBuilders.values().iterator().next();
            }
            ConnectionFactoryConfiguration localConfig = builder.createDefaultFactory(DEFAULT_CONNECTION_FACTORY, ConnectionFactoryType.LOCAL);
            ConnectionFactoryConfiguration xaConfig = builder.createDefaultFactory(DEFAULT_XA_CONNECTION_FACTORY, ConnectionFactoryType.XA);

            // check if default templates registered
            if (templateRegistry.getTemplate(DEFAULT_CONNECTION_FACTORY) == null) {
                templateRegistry.registerTemplate(localConfig);
            }
            if (templateRegistry.getTemplate(DEFAULT_XA_CONNECTION_FACTORY) == null) {
                templateRegistry.registerTemplate(xaConfig);
            }

            ConnectionFactory factory = creatorRegistry.create(localConfig);
            manager.register(DEFAULT_CONNECTION_FACTORY, factory);
            ConnectionFactory xaFactory = creatorRegistry.create(xaConfig);
            manager.register(DEFAULT_XA_CONNECTION_FACTORY, xaFactory);
        }

    }

    @Destroy
    public void destroy() throws FactoryRegistrationException {
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
     * @throws InvalidConfigurationException if the configuration contains an error or is invalid
     */
    private void parseConfigurations(List<ConnectionFactoryConfiguration> configurations, XMLStreamReader reader)
            throws XMLStreamException, InvalidConfigurationException {
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                String name = reader.getName().getLocalPart();
                if (name.equals("connection.factory")) {
                    // no factory specified, pick the first one
                    if (parsers.isEmpty()) {
                        throw new InvalidConfigurationException("JMS provider not installed");
                    }
                    ConnectionFactoryConfigurationParser parser = parsers.values().iterator().next();
                    ConnectionFactoryConfiguration configuration = parser.parse(reader);
                    configurations.add(configuration);
                } else if (name.startsWith("connection.factory") && !name.equals("connection.factory.templates")) {
                    ConnectionFactoryConfigurationParser parser = parsers.get(name);
                    if (parser == null) {
                        throw new InvalidConfigurationException("No JMS provider found for: " + name);
                    }
                    ConnectionFactoryConfiguration configuration = parser.parse(reader);
                    configurations.add(configuration);
                }

                break;
            case XMLStreamConstants.END_DOCUMENT:
                return;
            }
        }
    }


}
