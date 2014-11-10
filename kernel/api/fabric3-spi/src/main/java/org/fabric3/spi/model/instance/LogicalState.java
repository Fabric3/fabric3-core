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
package org.fabric3.spi.model.instance;

/**
 * Used to track the state of a logical instance such as a component or wire. Three states are defined:
 * <pre>
 * <ul>
 * <li> NEW - The component has been instantiated but has not been provisioned to a zone
 * <li> PROVISIONED - The component is running in a zone
 * <li> MARKED - The component has been marked for removal from the domain
 * </ul>
 * </pre>
 */
public enum LogicalState {

    NEW, PROVISIONED, MARKED
}
