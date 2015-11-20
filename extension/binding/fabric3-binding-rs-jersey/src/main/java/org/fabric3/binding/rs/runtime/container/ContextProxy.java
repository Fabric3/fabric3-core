package org.fabric3.binding.rs.runtime.container;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Threadsafe proxy for injected JAX-RS Context references.
 */
public class ContextProxy extends Application  implements ContainerRequestContext, Request, HttpHeaders, UriInfo, SecurityContext {
    public static final ContextProxy INSTANCE = new ContextProxy();

    public Object getProperty(String name) {
        return ContainerRequestCache.get().getProperty(name);
    }

    public Collection<String> getPropertyNames() {
        return ContainerRequestCache.get().getPropertyNames();
    }

    public void setProperty(String name, Object object) {
        ContainerRequestCache.get().setProperty(name, object);
    }

    public void removeProperty(String name) {
        ContainerRequestCache.get().removeProperty(name);
    }

    public UriInfo getUriInfo() {
        return ContainerRequestCache.get().getUriInfo();
    }

    public void setRequestUri(URI requestUri) {
        ContainerRequestCache.get().setRequestUri(requestUri);
    }

    public void setRequestUri(URI baseUri, URI requestUri) {
        ContainerRequestCache.get().setRequestUri(baseUri, requestUri);
    }

    public Request getRequest() {
        return ContainerRequestCache.get().getRequest();
    }

    public String getMethod() {
        return ContainerRequestCache.get().getMethod();
    }

    public Variant selectVariant(List<Variant> variants) {
        return ContainerRequestCache.get().selectVariant(variants);
    }

    public Response.ResponseBuilder evaluatePreconditions(EntityTag eTag) {
        return ContainerRequestCache.get().evaluatePreconditions(eTag);
    }

    public Response.ResponseBuilder evaluatePreconditions(Date lastModified) {
        return ContainerRequestCache.get().evaluatePreconditions(lastModified);
    }

    public Response.ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag eTag) {
        return ContainerRequestCache.get().evaluatePreconditions(lastModified, eTag);
    }

    public Response.ResponseBuilder evaluatePreconditions() {
        return ContainerRequestCache.get().evaluatePreconditions();
    }

    public void setMethod(String method) {
        ContainerRequestCache.get().setMethod(method);
    }

    public MultivaluedMap<String, String> getHeaders() {
        return ContainerRequestCache.get().getHeaders();
    }

    public List<String> getRequestHeader(String name) {
        return ContainerRequestCache.get().getRequestHeader(name);
    }

    public String getHeaderString(String name) {
        return ContainerRequestCache.get().getHeaderString(name);
    }

    public MultivaluedMap<String, String> getRequestHeaders() {
        return ContainerRequestCache.get().getRequestHeaders();
    }

    public Date getDate() {
        return ContainerRequestCache.get().getDate();
    }

    public Locale getLanguage() {
        return ContainerRequestCache.get().getLanguage();
    }

    public int getLength() {
        return ContainerRequestCache.get().getLength();
    }

    public MediaType getMediaType() {
        return ContainerRequestCache.get().getMediaType();
    }

    public List<MediaType> getAcceptableMediaTypes() {
        return ContainerRequestCache.get().getAcceptableMediaTypes();
    }

    public List<Locale> getAcceptableLanguages() {
        return ContainerRequestCache.get().getAcceptableLanguages();
    }

    public Map<String, Cookie> getCookies() {
        return ContainerRequestCache.get().getCookies();
    }

    public boolean hasEntity() {
        return ContainerRequestCache.get().hasEntity();
    }

    public InputStream getEntityStream() {
        return ContainerRequestCache.get().getEntityStream();
    }

    public void setEntityStream(InputStream input) {
        ContainerRequestCache.get().setEntityStream(input);
    }

    public SecurityContext getSecurityContext() {
        return ContainerRequestCache.get().getSecurityContext();
    }

    public void setSecurityContext(SecurityContext context) {
        ContainerRequestCache.get().setSecurityContext(context);
    }

    public void abortWith(Response response) {
        ContainerRequestCache.get().abortWith(response);
    }

    public String getPath() {
        return ContainerRequestCache.get().getUriInfo().getPath();
    }

    public String getPath(boolean decode) {
        return ContainerRequestCache.get().getUriInfo().getPath(decode);
    }

    public List<PathSegment> getPathSegments() {
        return ContainerRequestCache.get().getUriInfo().getPathSegments();
    }

    public List<PathSegment> getPathSegments(boolean decode) {
        return ContainerRequestCache.get().getUriInfo().getPathSegments(decode);
    }

    public URI getRequestUri() {
        return ContainerRequestCache.get().getUriInfo().getRequestUri();
    }

    public UriBuilder getRequestUriBuilder() {
        return ContainerRequestCache.get().getUriInfo().getBaseUriBuilder();
    }

    public URI getAbsolutePath() {
        return ContainerRequestCache.get().getUriInfo().getAbsolutePath();
    }

    public UriBuilder getAbsolutePathBuilder() {
        return ContainerRequestCache.get().getUriInfo().getAbsolutePathBuilder();
    }

    public URI getBaseUri() {
        return ContainerRequestCache.get().getUriInfo().getBaseUri();
    }

    public UriBuilder getBaseUriBuilder() {
        return ContainerRequestCache.get().getUriInfo().getBaseUriBuilder();
    }

    public MultivaluedMap<String, String> getPathParameters() {
        return ContainerRequestCache.get().getUriInfo().getPathParameters();
    }

    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        return ContainerRequestCache.get().getUriInfo().getPathParameters(decode);
    }

    public MultivaluedMap<String, String> getQueryParameters() {
        return ContainerRequestCache.get().getUriInfo().getQueryParameters();
    }

    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        return ContainerRequestCache.get().getUriInfo().getQueryParameters(decode);
    }

    public List<String> getMatchedURIs() {
        return ContainerRequestCache.get().getUriInfo().getMatchedURIs();
    }

    public List<String> getMatchedURIs(boolean decode) {
        return ContainerRequestCache.get().getUriInfo().getMatchedURIs(decode);
    }

    public List<Object> getMatchedResources() {
        return ContainerRequestCache.get().getUriInfo().getMatchedResources();
    }

    public URI resolve(URI uri) {
        return ContainerRequestCache.get().getUriInfo().resolve(uri);
    }

    public URI relativize(URI uri) {
        return ContainerRequestCache.get().getUriInfo().relativize(uri);
    }

    public Principal getUserPrincipal() {
        return ContainerRequestCache.get().getSecurityContext().getUserPrincipal();
    }

    public boolean isUserInRole(String role) {
        return ContainerRequestCache.get().getSecurityContext().isUserInRole(role);
    }

    public boolean isSecure() {
        return ContainerRequestCache.get().getSecurityContext().isSecure();
    }

    public String getAuthenticationScheme() {
        return ContainerRequestCache.get().getSecurityContext().getAuthenticationScheme();
    }

}
