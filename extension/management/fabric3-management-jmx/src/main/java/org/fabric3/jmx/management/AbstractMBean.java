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
package org.fabric3.jmx.management;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.MBeanInfo;

/**
 *
 */
public abstract class AbstractMBean implements DynamicMBean {
    protected final MBeanInfo mbeanInfo;

    public AbstractMBean(MBeanInfo mbeanInfo) {
        this.mbeanInfo = mbeanInfo;
    }

    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    public AttributeList getAttributes(String[] strings) {
        AttributeList list = new AttributeList(strings.length);
        for (String s : strings) {
            try {
                Object value = getAttribute(s);
                list.add(new Attribute(s, value));
            } catch (JMException e) {
                // ignore exceptions which means the attribute won't be in the result
            }
        }
        return list;
    }

    public AttributeList setAttributes(AttributeList attributeList) {
        AttributeList result = new AttributeList(attributeList.size());
        for (Object o : attributeList) {
            Attribute attribute = (Attribute) o;
            try {
                setAttribute(attribute);
            } catch (JMException e) {
                // ignore exceptions which means the attribute won't be in the result
            }
        }
        return result;
    }
}
