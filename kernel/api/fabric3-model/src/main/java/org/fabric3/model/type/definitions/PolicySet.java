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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.model.type.definitions;

import java.net.URI;
import java.util.Set;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * A policy set.
 *
 * @version $Rev$ $Date$
 */
public final class PolicySet extends AbstractPolicyDefinition {
    private static final long serialVersionUID = -4507145141780962741L;

    private final Set<QName> providedIntents;
    private final Element expression;
    private final String appliesTo;
    private final String attachTo;
    private final PolicyPhase phase;
    private URI contributionUri;

    /**
     * Initializes the state for the policy set.
     *
     * @param name            the name of the policy set.
     * @param providedIntents the intents provided by this policy set.
     * @param appliesTo       the XPath expression for the applies to attribute.
     * @param attachTo        the XPath expression for the attach to attribute.
     * @param expression      the policy set expression. The expression is a DOM representing the parsed policy configuration.
     * @param phase           the phase at which the policy is applied.
     * @param uri             the URI of the contribution this policy set is contained in
     */
    public PolicySet(QName name, Set<QName> providedIntents, String appliesTo, String attachTo, Element expression, PolicyPhase phase, URI uri) {
        super(name);
        this.providedIntents = providedIntents;
        this.attachTo = "".equals(attachTo) ? null : attachTo;
        this.appliesTo = "".equals(appliesTo) ? null : appliesTo;
        this.expression = expression;
        this.phase = phase;
        this.contributionUri = uri;
    }

    /**
     * Returns the XPath expression to the element to which the policy set applies.
     *
     * @return the apples to XPath expression.
     */
    public String getAppliesTo() {
        return appliesTo;
    }

    /**
     * Return the XPath expression to the element to which the policy set attaches.
     *
     * @return the attaches to XPath expression.
     */
    public String getAttachTo() {
        return attachTo;
    }

    /**
     * Checks whether the specified intent is provided by this policy set.
     *
     * @param intent Intent that needs to be checked.
     * @return True if this policy set provides to the specified intent.
     */
    public boolean doesProvide(QName intent) {
        return providedIntents.contains(intent);
    }

    /**
     * Returns the policy set expression. The expression is an opaque DOM containing the parsed policy expression, which may be a Fabric3 policy
     * expression, a WS-Policy expression, or a custom policy language.
     *
     * @return the policy set expression.
     */
    public Element getExpression() {
        return expression;
    }

    /**
     * Returns the qualified name of the policy expression element.
     *
     * @return the qualified name of the policy expression element
     */
    public QName getExpressionName() {
        return new QName(expression.getNamespaceURI(), expression.getLocalName());
    }

    /**
     * Returns the policy phase.
     *
     * @return the policy phase
     */
    public PolicyPhase getPhase() {
        return phase;
    }

    /**
     * Returns the contribution this policy set is contained in.
     *
     * @return the contribution this policy set is contained in
     */
    public URI getContributionUri() {
        return contributionUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PolicySet policySet = (PolicySet) o;

        if (appliesTo != null ? !appliesTo.equals(policySet.appliesTo) : policySet.appliesTo != null) return false;
        if (attachTo != null ? !attachTo.equals(policySet.attachTo) : policySet.attachTo != null) return false;
        if (contributionUri != null ? !contributionUri.equals(policySet.contributionUri) : policySet.contributionUri != null) return false;
        if (expression != null ? !expression.equals(policySet.expression) : policySet.expression != null) return false;
        if (phase != policySet.phase) return false;
        if (providedIntents != null ? !providedIntents.equals(policySet.providedIntents) : policySet.providedIntents != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (providedIntents != null ? providedIntents.hashCode() : 0);
        result = 31 * result + (contributionUri != null ? contributionUri.hashCode() : 0);
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        result = 31 * result + (appliesTo != null ? appliesTo.hashCode() : 0);
        result = 31 * result + (attachTo != null ? attachTo.hashCode() : 0);
        result = 31 * result + (phase != null ? phase.hashCode() : 0);
        return result;
    }
}
