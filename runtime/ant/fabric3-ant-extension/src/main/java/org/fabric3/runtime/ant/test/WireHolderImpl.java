package org.fabric3.runtime.ant.test;

import java.util.LinkedHashMap;
import java.util.Map;

import org.fabric3.spi.container.wire.Wire;
import org.fabric3.test.spi.TestWireHolder;

/**
 * TestWireHolder implementation for the Ant runtime.
 */
public class WireHolderImpl implements TestWireHolder {
    Map<String, Wire> wires = new LinkedHashMap<String, Wire>();

    public Map<String, Wire> getWires() {
        return wires;
    }

    public void add(String testName, Wire wire) {
        wires.put(testName, wire);
    }
}