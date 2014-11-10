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
 */
package org.fabric3.binding.ws.metro.runtime.policy;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import java.util.List;

/**
 * Resolves the set of features corresponding to intents and policy sets for a bound service or reference.
 */
public interface FeatureResolver {

    /**
     * Translates the requested intents and policy sets to web service features.
     *
     * @param requestedIntents Requested intents.
     * @return Resolved feature sets.
     */
    WebServiceFeature[] getFeatures(List<QName> requestedIntents);

}
