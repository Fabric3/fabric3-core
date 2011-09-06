package org.fabric3.implementation.drools.provision;

import java.util.Collection;

import org.drools.definition.KnowledgePackage;

import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

public class DroolsComponentDefinition extends PhysicalComponentDefinition {
    private static final long serialVersionUID = 7417967412654697630L;

    private Collection<KnowledgePackage> packages;

    public DroolsComponentDefinition(Collection<KnowledgePackage> packages) {
        this.packages = packages;
    }

    public Collection<KnowledgePackage> getPackages() {
        return packages;
    }
}
