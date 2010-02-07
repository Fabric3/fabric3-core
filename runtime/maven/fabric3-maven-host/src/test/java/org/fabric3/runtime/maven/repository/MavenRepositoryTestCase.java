package org.fabric3.runtime.maven.repository;

import java.net.URI;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class MavenRepositoryTestCase extends TestCase {

    public void testFind() throws Exception {
        MavenRepository repository = new MavenRepository();
        repository.init();
        assertNotNull(repository.find(URI.create("junit:junit:3.8.1")));
    }

}
