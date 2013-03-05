/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.policy.infoset;

import java.util.Collection;
import java.util.List;

import org.fabric3.policy.xpath.LogicalModelXPath;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.jaxen.JaxenException;

/**
 *
 */
public class PolicyEvaluatorImpl implements PolicyEvaluator {

    @SuppressWarnings({"unchecked"})
    public Collection<LogicalScaArtifact<?>> evaluate(String xpathExpression, LogicalComponent<?> component) throws PolicyEvaluationException {
        try {
            LogicalModelXPath xpath = new LogicalModelXPath(xpathExpression);
            Object ret = xpath.evaluate(component);
            if (ret instanceof Collection) {
                return (Collection<LogicalScaArtifact<?>>) ret;
            }
            throw new PolicyEvaluationException("Invalid select expression: " + xpathExpression);
        } catch (JaxenException e) {
            throw new PolicyEvaluationException(e);
        }
    }

    public boolean doesApply(String appliesToXPath, LogicalScaArtifact<?> target) throws PolicyEvaluationException {
        try {
            LogicalModelXPath xpath = new LogicalModelXPath(appliesToXPath);
            Object selected = xpath.evaluate(target);
            if (selected instanceof Boolean) {
                return (Boolean) selected;
            } else if (selected instanceof List) {
                return !((List) selected).isEmpty();
            }
            return false;
        } catch (JaxenException e) {
            throw new PolicyEvaluationException(e);
        }

    }

    public boolean doesAttach(String attachesToXPath, LogicalComponent<?> target, LogicalComponent<?> context) throws PolicyEvaluationException {
        try {
            LogicalModelXPath xpath = new LogicalModelXPath(attachesToXPath);
            Object selected = xpath.evaluate(context);
            if (selected instanceof List) {
                List<?> list = (List<?>) selected;
                if (list.isEmpty()) {
                    return false;
                }
                for (Object entry : list) {
                    if (entry instanceof LogicalComponent) {
                        if (entry == target) {
                            return true;
                        }
                    } else if (entry instanceof Bindable) {
                        if (((Bindable) entry).getParent() == target) {
                            return true;
                        }
                    } else if (entry instanceof LogicalBinding) {
                        if (((LogicalBinding) entry).getParent().getParent() == target) {
                            return true;
                        }
                    } else if (entry instanceof LogicalOperation) {
                        if (((LogicalOperation) entry).getParent().getParent() == target) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (JaxenException e) {
            throw new PolicyEvaluationException(e);
        }

    }

}