/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.transport.jetty.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferUtil;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.io.WriterOutputStream;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;


public class UrlResourceHandler extends AbstractHandler {
    //    ContextHandler _context;
    Resource _baseResource;
    String[] _welcomeFiles = {"index.html"};
    MimeTypes _mimeTypes = new MimeTypes();
    ByteArrayBuffer _cacheControl;

    public void doStart() throws Exception {
//        ContextHandler.SContext scontext = ContextHandler.getCurrentContext();
//        _context = (scontext == null ? null : scontext.getContextHandler());
        super.doStart();
    }

    private Resource getResource(String path) throws MalformedURLException {
        if (path == null || !path.startsWith("/"))
            throw new MalformedURLException(path);

        Resource base = _baseResource;
//        if (base == null) {
//            if (_context == null)
//                return null;
//            base = _context.getBaseResource();
//            if (base == null)
//                return null;
//        }

        try {
            path = URIUtil.canonicalPath(path);
            return base.addPath(path);
        }
        catch (Exception e) {
            Log.ignore(e);
        }

        return null;
    }

    protected Resource getResource(HttpServletRequest request) throws MalformedURLException {
        String path_info = request.getPathInfo();
        if (path_info == null)
            return null;
        return getResource(path_info);
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Request base_request = request instanceof Request ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
        if (base_request.isHandled() || !request.getMethod().equals(HttpMethods.GET))
            return;

        Resource resource = getResource(request);

        if (resource == null || !resource.exists())
            return;

        // We are going to server something
        base_request.setHandled(true);

        if (resource.isDirectory()) {
            if (!request.getPathInfo().endsWith(URIUtil.SLASH)) {
                response.sendRedirect(URIUtil.addPaths(request.getRequestURI(), URIUtil.SLASH));
                return;
            }

            if (resource == null || !resource.exists() || resource.isDirectory()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        // set some headers
        long last_modified = resource.lastModified();
        if (last_modified > 0) {
            long if_modified = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
            if (if_modified > 0 && last_modified / 1000 <= if_modified / 1000) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }

        Buffer mime = _mimeTypes.getMimeByExtension(resource.toString());
        if (mime == null)
            mime = _mimeTypes.getMimeByExtension(request.getPathInfo());

        // set the headers
        doResponseHeaders(response, resource, mime != null ? mime.toString() : null);

        // Send the content
        OutputStream out = null;
        try {
            out = response.getOutputStream();
        }
        catch (IllegalStateException e) {
            out = new WriterOutputStream(response.getWriter());
        }

        // See if a short direct method can be used?
        if (out instanceof HttpConnection.Output) {
            // TODO file mapped buffers
            response.setDateHeader(HttpHeaders.LAST_MODIFIED, last_modified);
            ((HttpConnection.Output) out).sendContent(resource.getInputStream());
        } else {
            // Write content normally
            response.setDateHeader(HttpHeaders.LAST_MODIFIED, last_modified);
            resource.writeTo(out, 0, resource.length());
        }
    }

    /**
     * Set the response headers. This method is called to set the response headers such as content type and content length. May be extended to add
     * additional headers.
     *
     * @param response
     * @param resource
     * @param mimeType
     */
    protected void doResponseHeaders(HttpServletResponse response, Resource resource, String mimeType) {
        if (mimeType != null)
            response.setContentType(mimeType);

        long length = resource.length();

        if (response instanceof Response) {
            HttpFields fields = ((Response) response).getHttpFields();

            if (length > 0)
                fields.putLongField(HttpHeaders.CONTENT_LENGTH_BUFFER, length);

            if (_cacheControl != null)
                fields.put(HttpHeaders.CACHE_CONTROL_BUFFER, _cacheControl);
        } else {
            if (length > 0)
                response.setHeader(HttpHeaders.CONTENT_LENGTH, BufferUtil.toBuffer(length).toString());

            if (_cacheControl != null)
                response.setHeader(HttpHeaders.CACHE_CONTROL, _cacheControl.toString());
        }

    }
}

