package org.fabric3.implementation.spring.introspection;

import java.net.URL;
import java.util.List;

import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;

/**
 * A Source for multiple related artifacts; used to introspect multiple application contexts.
 */
public class MultiSource extends UrlSource {
    private List<Source> sources;

    public MultiSource(URL primaryUrl, List<Source> sources) {
        super(primaryUrl);
        this.sources = sources;
    }

    public List<Source> getSources() {
        return sources;
    }
}
