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
package org.fabric3.host.contribution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.host.RuntimeMode;

/**
 * Represents a deployable artifact in a contribution
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"SerializableHasSerializationMethods"})
public class Deployable implements Serializable {
    private static final long serialVersionUID = -710863113841788110L;
    public static final List<RuntimeMode> DEFAULT_MODES;

    static {
        DEFAULT_MODES = new ArrayList<RuntimeMode>();
        DEFAULT_MODES.add(RuntimeMode.VM);
        DEFAULT_MODES.add(RuntimeMode.CONTROLLER);
        DEFAULT_MODES.add(RuntimeMode.PARTICIPANT);
    }

    private QName name;
    private QName type;
    private List<RuntimeMode> runtimeModes;


    public Deployable(QName name, QName type) {
        this(name, type, DEFAULT_MODES);
    }

    public Deployable(QName name, QName type, List<RuntimeMode> runtimeModes) {
        this.name = name;
        this.type = type;
        this.runtimeModes = runtimeModes;
    }

    /**
     * The QName of the deployable component.
     *
     * @return QName of the deployable component
     */
    public QName getName() {
        return name;
    }

    /**
     * Returns the deployable type.
     *
     * @return the deployable type
     */
    public QName getType() {
        return type;
    }

    /**
     * Returns the runtime modes the deployable should be activated in.
     *
     * @return the runtime modes the deployable should be activated in.
     */
    public List<RuntimeMode> getRuntimeModes() {
        return runtimeModes;
    }
}
