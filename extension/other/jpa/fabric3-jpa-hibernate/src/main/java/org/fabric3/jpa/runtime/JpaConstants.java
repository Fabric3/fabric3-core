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
package org.fabric3.jpa.runtime;

/**
 * @version $Rev$ $Date$
 */
public class JpaConstants {

    public static final String PROPERTY_VALUE = "value";

    public static final String PROPERTY_NAME = "name";

    public static final String NAMED_UNIT = "//persistence-unit[@name=''{0}'']";

    public static final String ANY_UNIT = "//persistence-unit";

    public static final String PROPERTY = "/properties/property";

    public static final String TRANSACTION_TYPE = "/@transaction-type";

    public static final String NAME = "/@name";

    public static final String PROVIDER = "/provider";

    public static final String NON_JTA_DATA_SOURCE = "/non-jta-data-source";

    public static final String MAPPING_FILE = "/mapping-file";

    public static final String CLASS = "/class";

    public static final String JTA_DATA_SOURCE = "/jta-data-source";

    public static final String JAR_FILE = "/jar-file";

    public static final String EXCLUDE_UNLISTED_CLASSES = "/exclude-unlisted-classes";

}
