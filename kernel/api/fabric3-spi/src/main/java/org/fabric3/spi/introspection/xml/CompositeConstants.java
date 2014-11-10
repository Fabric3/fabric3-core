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
package org.fabric3.spi.introspection.xml;

import javax.xml.namespace.QName;

import static org.oasisopen.sca.Constants.SCA_NS;

/**
 */
public interface CompositeConstants  {

   QName COMPOSITE = new QName(SCA_NS, "composite");
   QName INCLUDE = new QName(SCA_NS, "include");
   QName CHANNEL = new QName(SCA_NS, "channel");
   QName PROPERTY = new QName(SCA_NS, "property");
   QName SERVICE = new QName(SCA_NS, "service");
   QName REFERENCE = new QName(SCA_NS, "reference");
   QName COMPONENT = new QName(SCA_NS, "component");
   QName WIRE = new QName(SCA_NS, "wire");


}
