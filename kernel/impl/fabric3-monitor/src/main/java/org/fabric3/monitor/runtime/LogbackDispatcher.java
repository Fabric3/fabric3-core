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
package org.fabric3.monitor.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.fabric3.host.monitor.MonitorConfigurationException;
import org.fabric3.host.monitor.MonitorEvent;
import org.fabric3.host.monitor.MonitorEventDispatcher;
import org.fabric3.host.runtime.HostInfo;

/**
 * Dispatches to one or more Logback appenders. If a configuration is not set, a default one will be created that logs to the console.
 *
 * @version $Rev$ $Date$
 */
public class LogbackDispatcher implements MonitorEventDispatcher {
    private static final String DEFAULT_PATTERN = "[%level %thread %d{YY:MM:DD HH:mm:ss.SSS}] %msg%n%ex";
    private HostInfo hostInfo;
    private boolean configured;
    private LoggerContext context;
    private Logger logger;

    static {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Logger.ROOT_LOGGER_NAME).detachAndStopAllAppenders();
    }

    /**
     * Constructor which delegates to a log context that uses a private appender configuration and does not send messages to parent appenders.
     *
     * @param name     the log context name
     * @param hostInfo the host info
     */
    public LogbackDispatcher(String name, HostInfo hostInfo) {
        // by default do not send log messages to parent appenders
        this(name, false, hostInfo);
    }

    /**
     * Constructor.
     *
     * @param name     the log context name
     * @param additive true if the log context should use parent appenders; otherwise the log context uses a private appender configuration and does
     *                 not send messages to parent appenders.
     * @param hostInfo the host info
     */
    public LogbackDispatcher(String name, boolean additive, HostInfo hostInfo) {
        this.hostInfo = hostInfo;
        context = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger = context.getLogger(name);
        logger.setAdditive(additive);
    }

    public void configure(Element element) throws MonitorConfigurationException {
        try {
            expandLogFileNames(element);
            // wrap the configuration in a document that LogBack accepts
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            document.adoptNode(element);
            document.appendChild(element);
            InputSource source = transform(document);

            logger.detachAndStopAllAppenders();
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            configurator.doConfigure(source);
            configured = true;
        } catch (ParserConfigurationException e) {
            throw new MonitorConfigurationException("Error parsing monitor configuration", e);
        } catch (TransformerException e) {
            throw new MonitorConfigurationException("Error parsing monitor configuration", e);
        } catch (JoranException e) {
            throw new MonitorConfigurationException("Error parsing monitor configuration", e);
        }
    }

    public void start() {
        if (!configured) {
            configureDefaultAppender(context);
        }
    }

    public void stop() {
        // stop appenders
        logger.detachAndStopAllAppenders();
    }

    public void onEvent(MonitorEvent event) {
        if (!(event instanceof ILoggingEvent)) {
            throw new AssertionError("Event must implement " + ILoggingEvent.class.getName());
        }
        logger.callAppenders((ILoggingEvent) event);
    }

    /**
     * Replace relative file names in appender configurations with absolute paths under the runtime data/logs directory.
     *
     * @param element the log configuration
     */
    private void expandLogFileNames(Element element) {
        NodeList files = element.getElementsByTagName("file");
        File dir = new File(hostInfo.getDataDir(), "log");
        for (int i = 0; i < files.getLength(); i++) {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Element fileElement = (Element) files.item(i);
            fileElement.setTextContent(new File(dir, fileElement.getTextContent()).getAbsolutePath());
        }
    }

    /**
     * Creates a default console appender if a configuration is not set.
     *
     * @param context the context to configure
     */
    private void configureDefaultAppender(LoggerContext context) {
        logger.detachAndStopAllAppenders();
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(context);
        appender.setName("fabric3-console");
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(DEFAULT_PATTERN);
        encoder.start();
        appender.setEncoder(encoder);
        appender.start();
        logger.addAppender(appender);
    }

    /**
     * Transforms a document to an InputSource.
     *
     * @param document the document
     * @return the InputSource
     * @throws TransformerException if the document cannot be transformed
     */
    private InputSource transform(Document document) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        DOMSource source = new DOMSource(document);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(stream);
        transformer.transform(source, result);
        return new InputSource(new ByteArrayInputStream(stream.toByteArray()));
    }

}
