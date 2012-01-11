package org.fabric3.binding.rs.runtime;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.StringKeyIgnoreCaseMultivaluedMap;

/**
 * 
 * Jersey Client REST Response Builder
 * @author palmalcheg
 *
 */
public class RsClientResponse {
	
	private HashMap<Integer, String> queryParamNames = new HashMap<Integer, String>();
	private HashMap<Integer, String> pathParamNames = new HashMap<Integer, String>();
	private String path = "/";
	private Annotation action;
	private String[] producesTypes = new String[] {"text/plain"};
	private String[] consumesTypes = new String[] {"text/plain"};
	private final URI uri;
	private final Class<?> resultType;
	private Client clientAPI;
	

	public RsClientResponse(Class<?> resType, URI wr) {
		this.uri = wr;
		this.resultType = resType != void.class ? resType : null;
	}

	public <T extends Annotation> RsClientResponse withAction(T annotation) {
		if (annotation != null) {
			action = annotation;
		}
		return this;
	}
	
	public RsClientResponse withPath(Path annotation) {
		if (annotation != null) {
			path = annotation.value();
		}
		return this;
	}
	
	public RsClientResponse withProduces(Produces annotation) {
		if (annotation != null) {	
			producesTypes = annotation.value();
		}
		return this;
	}
	
	public RsClientResponse withConsumes(Consumes annotation) {
		if (annotation != null) {			
			consumesTypes = annotation.value();
		}
		return this;
	}

	public void withParam(int i, Annotation[] annotations) {
		for (Annotation a : annotations) {
			if (QueryParam.class == a.annotationType()){
				queryParamNames.put(i, ((QueryParam)a).value());
			} else if (PathParam.class == a.annotationType()){
				pathParamNames.put(i, ((PathParam)a).value());
			}			
		}
	}

	public Object build(Object[] paramValues) {
		
		if (uri == null) {
			throw new IllegalStateException("No web resource configured !!!");
		}

		WebResource wr = initResource(paramValues);	
		
		Builder builder = wr.accept(producesTypes);
		for (int i = 0; i < consumesTypes.length; i++) {
			builder.type(consumesTypes[i]);			
			ClientResponse response = handleAction(builder, paramValues);			
			Object result;		
			int st = response.getStatus();
			if (Status.fromStatusCode(st) == Status.UNSUPPORTED_MEDIA_TYPE){
				// try next media type if any
				continue;
			}
			if (st >= 400) {
				// TODO:  Debugging, need to be replaced by logger 
				System.err.println(response.getEntity(String.class));
				throw new WebApplicationException(st);
			}			
			if (resultType != null) {
				result = response.getEntity(resultType);
			} else {
				result = response.getEntity(String.class);
			}
			return result;		
		}		
		// Happens when all Media Types iterated without a luck =((
		throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
		
		 
	}
	
	private WebResource initResource(Object[] paramValues) {
		ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
		try {
		Thread.currentThread().setContextClassLoader(Client.class.getClassLoader());
		
		UriBuilder builder = UriBuilder.fromUri(uri);		
		HashMap<String,Object> pathElements = new HashMap<String,Object>();
		MultivaluedMap<String, String> query = new StringKeyIgnoreCaseMultivaluedMap<String>();
		
		// Configuring parameters for path and query
		if (paramValues != null) {
			int length = paramValues.length;
			for (int i = 0; i < length; i++) {
				Object value = paramValues[i];	
				
				String queryParameterName = queryParamNames.get(i);
				String pathElementName = pathParamNames.get(i);
				
				if (value != null && queryParameterName != null) {
				    query.add(queryParameterName, value.toString());
				}				
				if (value != null && pathElementName != null){
					pathElements.put(pathElementName, value);
			    }
			}
		}
		
		if (clientAPI == null) {
			//Web Resource Config details
			ClientConfig cc = new DefaultClientConfig();
	        cc.getClasses().add(JacksonJaxbJsonProvider.class);
	        clientAPI = Client.create(cc);
		}		    
        
        //Apply path params
		URI buildURI = builder.path(path).buildFromMap(pathElements);
		
		//Build Web Resource
		WebResource resource = clientAPI.resource(buildURI);
		
		//Apply query params
		resource.queryParams(query);
		return  resource;
		}
		finally {
			Thread.currentThread().setContextClassLoader(oldCl);
		}
	}
	
	private ClientResponse handleAction(Builder builder, Object[] paramValues) {
		if (action == null) {
			return null;
		} 
		else if ( action.annotationType() == PUT.class )  {
			for (int i = 0; i < paramValues.length; i++) {
				String query = queryParamNames.get(i);
				String path = pathParamNames.get(i);
				if (query == null && path == null) {
					// First non query/path parameter
					return builder.put(ClientResponse.class, paramValues[i]);
				}
			}
		}
		else if (action.annotationType() == POST.class) {
			return builder.post(ClientResponse.class);
		}
		else  if (action.annotationType() == GET.class) {
			return builder.get (ClientResponse.class);
		}
		throw new WebApplicationException(Status.PRECONDITION_FAILED.getStatusCode());
	}

}
