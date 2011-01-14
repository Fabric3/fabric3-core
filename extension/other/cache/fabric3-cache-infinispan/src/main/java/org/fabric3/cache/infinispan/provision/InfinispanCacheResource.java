package org.fabric3.cache.infinispan.provision;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.spi.model.physical.PhysicalResourceDefinition;
import org.w3c.dom.Document;

/**
 * 
 * @version $Rev$ $Date$
 */
public class InfinispanCacheResource extends PhysicalResourceDefinition {

	private static final long serialVersionUID = -6400612928297999316L;

    private List<Document> configurations = new ArrayList<Document>();

    public InfinispanCacheResource(List<Document> configurations) {
		super();
		this.configurations = configurations;
	}

    public void addCacheConfiguration(Document configuration) {
    	configurations.add(configuration);
    }
    
    public List<Document> getCacheConfigurations() {
    	return configurations;
    }    
}




