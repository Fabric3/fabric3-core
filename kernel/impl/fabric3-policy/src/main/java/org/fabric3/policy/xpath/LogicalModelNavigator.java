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
package org.fabric3.policy.xpath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jaxen.DefaultNavigator;
import org.jaxen.FunctionCallException;
import org.jaxen.JaxenConstants;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPath;
import org.jaxen.javabean.Element;
import org.jaxen.util.SingleObjectIterator;

import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Interface for navigating the domain logical model using Jaxen.
 */
public class LogicalModelNavigator extends DefaultNavigator implements NamedAccessNavigator {
    private static final long serialVersionUID = 1755511737841941331L;

    /**
     * Singleton implementation.
     */
    private static final LogicalModelNavigator instance = new LogicalModelNavigator();

    /**
     * Retrieve the singleton instance of this DocumentNavigator.
     *
     * @return the singleton
     */
    public static Navigator getInstance() {
        return instance;
    }

    public boolean isElement(Object obj) {
        return (obj instanceof LogicalComponent);
    }

    public boolean isComment(Object obj) {
        return false;
    }

    public boolean isText(Object obj) {
        return (obj instanceof String);
    }

    public boolean isAttribute(Object obj) {
        return false;
    }

    public boolean isProcessingInstruction(Object obj) {
        return false;
    }

    public boolean isDocument(Object obj) {
        return false;
    }

    public boolean isNamespace(Object obj) {
        return false;
    }

    public String getElementName(Object obj) {
        return ((Element) obj).getName();
    }

    public String getElementNamespaceUri(Object obj) {
        return "";
    }

    public String getElementQName(Object obj) {
        return "";
    }

    public String getAttributeName(Object obj) {
        return "";
    }

    public String getAttributeNamespaceUri(Object obj) {
        return "";
    }

    public String getAttributeQName(Object obj) {
        return "";
    }

    public Iterator getChildAxisIterator(Object contextNode) {
        if (contextNode instanceof LogicalCompositeComponent) {
            final LogicalCompositeComponent composite = (LogicalCompositeComponent) contextNode;
            return new Iterator() {
                int pos = 0;

                public boolean hasNext() {
                    return pos < composite.getComponents().size();
                }

                public Object next() {
                    if (!hasNext()) {
                        throw new IndexOutOfBoundsException();
                    }
                    int i = 0;
                    for (LogicalComponent<?> component : composite.getComponents()) {
                        if (i == pos) {
                            pos++;
                            return component;
                        }
                        i++;
                    }
                    throw new AssertionError();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getChildAxisIterator(Object contextNode, String localName, String namespacePrefix, String namespaceURI) {
        if (contextNode instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) contextNode;
            if ("component".equals(localName) || "implementation".equals(localName)) {
                Collection<LogicalComponent<?>> result = composite.getComponents();
                if (result == null) {
                    return JaxenConstants.EMPTY_ITERATOR;
                }
                return result.iterator();
            }
        } else if (contextNode instanceof LogicalComponent) {
            // handle keywords: binding, implementation, reference, and service
            if (localName.startsWith("binding.")) {
                LogicalComponent<?> component = (LogicalComponent<?>) contextNode;
                List<LogicalBinding> bindings = new ArrayList<>();
                for (LogicalService service : component.getServices()) {
                    for (LogicalBinding<?> binding : service.getBindings()) {
                        if (localName.equals(binding.getDefinition().getType().getLocalPart())) {
                            bindings.add(binding);
                        }
                    }
                }
                for (LogicalReference reference : component.getReferences()) {
                    for (LogicalBinding<?> binding : reference.getBindings()) {
                        if (localName.equals(binding.getDefinition().getType().getLocalPart())) {
                            bindings.add(binding);
                        }
                    }
                }
                return bindings.iterator();
            } else if (localName.startsWith("implementation.")) {
                LogicalComponent<?> component = (LogicalComponent<?>) contextNode;
                if (localName.equals(component.getDefinition().getImplementation().getType().getLocalPart())) {
                    return new SingleObjectIterator(component);
                }
            } else if (localName.equals("reference")) {
                LogicalComponent<?> component = (LogicalComponent<?>) contextNode;
                return component.getReferences().iterator();
            } else if (localName.equals("service")) {
                LogicalComponent<?> component = (LogicalComponent<?>) contextNode;
                return component.getServices().iterator();
            }
        } else if (contextNode instanceof Bindable) {
            Bindable bindable = (Bindable) contextNode;
            if (localName.startsWith("binding.")) {
                List<LogicalBinding<?>> bindings = new ArrayList<>();
                for (LogicalBinding<?> binding : bindable.getBindings()) {
                    // TODO use strict namespaces?
                    if (localName.equals(binding.getDefinition().getType().getLocalPart())) {
                        bindings.add(binding);
                    }
                }
                return bindings.iterator();
            } else {
                // assume it is an operation name

            }
        } else if (contextNode instanceof LogicalBinding) {
            LogicalBinding<?> binding = (LogicalBinding<?>) contextNode;
            if (localName.equals(binding.getDefinition().getType().getLocalPart())) {
                List<LogicalBinding<?>> bindings = new ArrayList<>();
                bindings.add(binding);
                return bindings.iterator();
            }
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getParentAxisIterator(Object contextNode) {
        if (contextNode instanceof LogicalComponent) {
            return new SingleObjectIterator(((LogicalComponent) contextNode).getParent());
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getAttributeAxisIterator(Object contextNode) {
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getAttributeAxisIterator(Object contextNode, String localName, String namespacePrefix, String namespaceURI)
            throws UnsupportedAxisException {
        if (contextNode instanceof LogicalComponent) {
            LogicalComponent<?> component = (LogicalComponent) contextNode;
            Object attr;
            if ("uri".equals(localName)) {
                attr = component.getUri().getSchemeSpecificPart();
            } else if ("name".equals(localName)) {
                attr = component.getDefinition().getName();
            } else {
                throw new UnsupportedAxisException("Unknown attribute specified: " + localName);
            }

            if (attr == null) {
                return JaxenConstants.EMPTY_ITERATOR;
            }
            return new SingleObjectIterator(attr);
        } else if (contextNode instanceof LogicalService) {
            LogicalService service = (LogicalService) contextNode;
            Object attr;
            if ("uri".equals(localName)) {
                attr = service.getUri().getSchemeSpecificPart();
            } else if ("name".equals(localName)) {
                attr = service.getDefinition().getName();
            } else {
                throw new UnsupportedAxisException("Unknown attribute specified: " + localName);
            }
            return new SingleObjectIterator(attr);
        } else if (contextNode instanceof LogicalReference) {
            LogicalReference reference = (LogicalReference) contextNode;
            Object attr;
            if ("uri".equals(localName)) {
                attr = reference.getUri().getSchemeSpecificPart();
            } else if ("name".equals(localName)) {
                attr = reference.getDefinition().getName();
            } else {
                throw new UnsupportedAxisException("Unknown attribute specified: " + localName);
            }
            return new SingleObjectIterator(attr);
        } else if (contextNode instanceof Bindable) {
            Bindable bindable = (Bindable) contextNode;
            List<LogicalBinding<?>> bindings = new ArrayList<>();
            for (LogicalBinding<?> binding : bindable.getBindings()) {
                // TODO use strict namespaces?
                if (localName.equals(binding.getDefinition().getType().getLocalPart())) {
                    bindings.add(binding);
                }
            }
            return bindings.iterator();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Iterator getNamespaceAxisIterator(Object contextNode) {
        return JaxenConstants.EMPTY_ITERATOR;
    }

    public Object getDocumentNode(Object contextNode) {
        if (contextNode instanceof LogicalScaArtifact) {
            LogicalScaArtifact<?> artifact = (LogicalScaArtifact<?>) contextNode;
            while (artifact.getParent() != null) {
                artifact = artifact.getParent();
            }
            return artifact;
        }
        return contextNode;
    }

    public Object getParentNode(Object contextNode) {
        if (contextNode instanceof LogicalScaArtifact) {
            return ((LogicalScaArtifact) contextNode).getParent();
        }

        return null;
    }

    public String getTextStringValue(Object obj) {
        return obj.toString();
    }

    public String getElementStringValue(Object obj) {
        return obj.toString();
    }

    public String getAttributeStringValue(Object obj) {
        return obj.toString();
    }

    public String getNamespaceStringValue(Object obj) {
        return obj.toString();
    }

    public String getNamespacePrefix(Object obj) {
        return null;
    }

    public String getCommentStringValue(Object obj) {
        return null;
    }

    public String translateNamespacePrefixToUri(String prefix, Object context) {
        return null;
    }

    public short getNodeType(Object node) {
        return 0;
    }

    public Object getDocument(String uri) throws FunctionCallException {
        return null;
    }

    public String getProcessingInstructionTarget(Object obj) {
        return null;
    }

    public String getProcessingInstructionData(Object obj) {
        return null;
    }

    public XPath parseXPath(String xpath) throws org.jaxen.saxpath.SAXPathException {
        return new LogicalModelXPath(xpath);
    }

}