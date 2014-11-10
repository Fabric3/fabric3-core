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
package org.fabric3.fabric.contract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.ContractMatcherExtension;
import org.fabric3.spi.contract.MatchResult;

/**
 * The default ContractMatcher implementation.
 */
public class DefaultContractMatcher implements ContractMatcher {
    private Map<Key, ContractMatcherExtension<?, ?>> cache = new HashMap<>();

    @Reference
    public void setMatcherExtensions(List<ContractMatcherExtension<?, ?>> matchers) {
        cache.clear();
        for (ContractMatcherExtension<?, ?> matcher : matchers) {
            addMatcherExtension(matcher);
        }
    }

    public void addMatcherExtension(ContractMatcherExtension<?, ?> matcher) {
        Key key = new Key(matcher.getSource(), matcher.getTarget());
        cache.put(key, matcher);
    }

    @SuppressWarnings({"unchecked"})
    public MatchResult isAssignableFrom(ServiceContract source, ServiceContract target, boolean reportErrors) {
        Key key = new Key(source.getClass(), target.getClass());
        ContractMatcherExtension matcher = cache.get(key);
        if (matcher == null) {
            // this is a programming error
            String name = ContractMatcherExtension.class.getSimpleName();
            throw new AssertionError(name + " not found for converting from " + source.getClass() + " to " + target.getClass());
        }
        return matcher.isAssignableFrom(source, target, reportErrors);
    }

    private class Key {
        private Class<? extends ServiceContract> source;
        private Class<? extends ServiceContract> target;

        private Key(Class<? extends ServiceContract> source, Class<? extends ServiceContract> target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key that = (Key) o;

            return !(source != null ? !source.equals(that.source) : that.source != null)
                    && !(target != null ? !target.equals(that.target) : that.target != null);

        }

        @Override
        public int hashCode() {
            int result = source != null ? source.hashCode() : 0;
            result = 31 * result + (target != null ? target.hashCode() : 0);
            return result;
        }
    }
}
