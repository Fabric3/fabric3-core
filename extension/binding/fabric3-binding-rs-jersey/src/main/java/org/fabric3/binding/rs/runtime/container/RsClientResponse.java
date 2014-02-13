package org.fabric3.binding.rs.runtime.container;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;

/**
 * Builds Jersey Client REST Responses.
 */
public class RsClientResponse {

    private HashMap<Integer, String> queryParamNames = new HashMap<>();
    private HashMap<Integer, String> pathParamNames = new HashMap<>();
    private String path = "/";
    private Annotation action;
    private String[] producesTypes = new String[]{MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML};
    private String[] consumesTypes = new String[]{MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML};
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
            if (QueryParam.class == a.annotationType()) {
                queryParamNames.put(i, ((QueryParam) a).value());
            } else if (PathParam.class == a.annotationType()) {
                pathParamNames.put(i, ((PathParam) a).value());
            }
        }
    }

    public Object build(Object[] paramValues) {
        if (uri == null) {
            throw new IllegalStateException("No web resource configured !!!");
        }
        WebTarget wr = initResource(paramValues);

        Invocation.Builder builder = wr.request(producesTypes);
        builder.accept(consumesTypes);
        Response response = handleAction(builder, paramValues, producesTypes[0]);
        Object result;
        int st = response.getStatus();
        if (st >= 400) {
            // TODO:  Debugging, need to be replaced by logger
            System.err.println(response.getEntity());
            throw new WebApplicationException(st);
        }
        if (resultType != null) {
            result = response.readEntity(resultType);
        } else {
            result = response.getEntity();
        }
        return result;
    }

    private WebTarget initResource(Object[] paramValues) {
        UriBuilder builder = UriBuilder.fromUri(uri);
        HashMap<String, Object> pathElements = new HashMap<>();
        MultivaluedMap<String, String> query = new StringKeyIgnoreCaseMultivaluedMap<>();

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
                if (value != null && pathElementName != null) {
                    pathElements.put(pathElementName, value);
                }
            }
        }

        if (clientAPI == null) {
            //Web Resource Config details
            clientAPI = ClientBuilder.newClient();
            clientAPI.register(JacksonJaxbJsonProvider.class);
        }

        //Apply path params
        URI buildURI = builder.path(path).buildFromMap(pathElements);

        //Build Web Resource
        WebTarget resource = clientAPI.target(buildURI);

        //Apply query params
        for (Map.Entry<String, List<String>> entry : query.entrySet()) {
            resource.queryParam(entry.getKey(), entry.getValue().toArray(new Object[entry.getValue().size()]));
        }
        return resource;
    }

    private Response handleAction(Invocation.Builder builder, Object[] paramValues, String mediaType) {
        if (action == null) {
            return null;
        } else if (action.annotationType() == PUT.class) {
            for (int i = 0; i < paramValues.length; i++) {
                String query = queryParamNames.get(i);
                String path = pathParamNames.get(i);
                if (query == null && path == null) {
                    // First non query/path parameter
                    return builder.buildPut(Entity.entity(paramValues[i], mediaType)).invoke();
                }
            }
        } else if (action.annotationType() == POST.class) {
            throw new UnsupportedOperationException();
        } else if (action.annotationType() == GET.class) {
            return builder.get();
        }
        throw new WebApplicationException(Response.Status.PRECONDITION_FAILED.getStatusCode());
    }

}
