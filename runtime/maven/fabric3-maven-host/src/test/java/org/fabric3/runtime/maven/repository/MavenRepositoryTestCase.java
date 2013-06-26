package org.fabric3.runtime.maven.repository;

import java.net.URI;

import junit.framework.TestCase;

/**
 *
 */
public class MavenRepositoryTestCase extends TestCase {

    public void testFind() throws Exception {
        MavenRepository repository = new MavenRepository();
        repository.init();
        assertNotNull(repository.find(URI.create("junit:junit:4.11")));
    }

}
