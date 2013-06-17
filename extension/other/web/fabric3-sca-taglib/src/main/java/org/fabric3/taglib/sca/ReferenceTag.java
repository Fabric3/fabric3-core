/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.taglib.sca;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Implements the SCA <reference> tag.
 */
public class ReferenceTag extends SimpleTagSupport {
    private String name;
    private Class<?> type;

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) throws JspException {
        try {
            this.type = Class.forName(type, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new JspException("Reference interface not found: " + name, e);
        }
    }

    public void doTag() throws JspException, IOException {
        // look in application scope first to avoid session creation
        Object ref = getJspContext().getAttribute(name, PageContext.APPLICATION_SCOPE);
        if (ref == null) {
            ref = getJspContext().getAttribute(name, PageContext.SESSION_SCOPE);
            if (ref == null) {
                throw new JspException("Reference not found: " + name);
            }
        }
        if (!(type.isInstance(ref))) {
            throw new JspException("Reference " + name + " not of type: " + type.getName());
        }
        getJspContext().setAttribute(name, ref);
    }

}
