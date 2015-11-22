package org.fabric3.hazelcast.impl;

import com.hazelcast.core.HazelcastInstance;

/**
 * Manages a Hazelcast instance for the current runtime.
 */
public interface HazelcastService {

    /**
     * Returns the Hazelcast instance.
     *
     * @return the Hazelcast instance
     */
    HazelcastInstance getInstance();
}
