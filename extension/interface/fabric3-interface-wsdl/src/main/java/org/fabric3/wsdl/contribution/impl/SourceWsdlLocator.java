package org.fabric3.wsdl.contribution.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.xml.WSDLLocator;

import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.host.stream.Source;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.xml.sax.InputSource;

/**
 * Implementation of javax.wsdl.xml.WSDLLocator that delegates import resolution
 * to the Source 
 */
public class SourceWsdlLocator implements WSDLLocator {
	private Source mSource;
	private IntrospectionContext mContext;
	private String mLatestImportURI;
	private List<InputStream> mStreams;
	
	public SourceWsdlLocator(Source aSource, IntrospectionContext aContext)	{
		mSource = aSource;
		mContext = aContext;
		mStreams = new ArrayList();
	}

	public InputSource getBaseInputSource() {
        try {
            return new InputSource(addStream(mSource.openStream()));
        }
        catch (IOException ex) {
            mContext.addError(new InvalidWsdl(ex.getLocalizedMessage(), ex));
            return null;
        }
    }

    public String getBaseURI() {
        return mSource.getSystemId();
    }

    public InputSource getImportInputSource(String aParentLocation, String aImportLocation) {
       
        try {
            Source importSource = mSource.getImportSource(aParentLocation, aImportLocation);
            if (importSource != null) {
                mLatestImportURI = importSource.getSystemId();
                return new InputSource(addStream(importSource.openStream()));
            }
        }
        catch (IOException ex) {
            mContext.addError(new InvalidWsdl(ex.getLocalizedMessage(), ex));
        }
        return null;
    }

    public String getLatestImportURI() {
        return mLatestImportURI;
    }

    public void close() {
        // close all closeable streams we created for this locator
        for (InputStream stream : mStreams) {
            try {
                stream.close();
            }
            catch(Throwable t) {
                // ignore since some may have been closed already
            }
        }
    }
    
    /**
     * Adds a stream to the collection
     * @param aStream
     * @return stream that was given
     */
    private InputStream addStream(InputStream aStream) {
        if (aStream != null) {
            mStreams.add(aStream);
        }
        return aStream;
    }
}
