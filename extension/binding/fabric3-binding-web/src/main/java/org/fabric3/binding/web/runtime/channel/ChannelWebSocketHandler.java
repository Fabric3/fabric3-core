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
package org.fabric3.binding.web.runtime.channel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.atmosphere.websocket.WebSocketEventListener;
import org.fabric3.binding.web.runtime.common.BroadcasterManager;
import org.fabric3.binding.web.runtime.common.ContentTypes;
import org.fabric3.binding.web.runtime.common.InvalidContentTypeException;
import org.fabric3.host.util.IOHelper;
import org.fabric3.spi.channel.EventWrapper;

/**
 * Manages incoming requests destined for a channel. This includes setting the broadcaster associated with the request, forwarding HTTP requests to
 * additional handlers or suspending the request it is websocket-based.
 */
public class ChannelWebSocketHandler extends AbstractReflectorAtmosphereHandler implements WebSocketEventListener {
	
	private static final String ISO_8859_1 = "ISO-8859-1";
	private static final String RESPONSE_CONTENT_TYPE = ContentTypes.TEXT_PLAIN+";charset="+ISO_8859_1;
	
	private BroadcasterManager broadcasterManager;
	private ChannelMonitor monitor;
	private PubSubManager pubSubManager;
    
    public ChannelWebSocketHandler(BroadcasterManager broadcasterManager, PubSubManager pubSubManager, ChannelMonitor monitor) {
    	this.broadcasterManager = broadcasterManager;
    	this.pubSubManager = pubSubManager;
    	this.monitor = monitor;
	}

	public void onRequest(AtmosphereResource resource) throws IOException {
		AtmosphereRequest req = resource.getRequest();
        AtmosphereResponse res = resource.getResponse();
        String method = req.getMethod();
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null)
        	return;
        
        //get channel name
        String channel = pathInfo.substring(pathInfo.lastIndexOf("/")+1);
        
        // Suspend the response.
        if ("GET".equalsIgnoreCase(method)) {

        	// Log all events on the console, including WebSocket events.
            resource.addEventListener( this );

            res.setContentType(RESPONSE_CONTENT_TYPE);
            
            Broadcaster b = broadcasterManager.getChannelBroadcaster(channel, resource.getAtmosphereConfig());
            resource.setBroadcaster(b);
            ChannelSubscriber subscriber = pubSubManager.getSubscriber(channel);
            try {
				subscriber.subscribe(req);
			} catch (PublishException e) {
				res.setStatus(500);
				monitor.error(e);
			}
            
        } else if ("POST".equalsIgnoreCase(method)) {
        	String contentType = req.getContentType();
        	if (contentType == null || contentType == "text/html"){
    			contentType = ContentTypes.DEFAULT;
    		}
        	String encoding = req.getCharacterEncoding();
            if (encoding == null) {
                encoding = ISO_8859_1;
            }
    		try {
    			ServletInputStream stream = req.getInputStream();
                String data = read(stream, encoding);
    			EventWrapper wrapper = ChannelUtils.createWrapper(contentType, data);
    			ChannelPublisher publisher = pubSubManager.getPublisher(channel);
    			publisher.publish(wrapper);
    		} catch (PublishDeniedException e) {
                res.setStatus(403);   // forbidden
            } catch (PublishException e) {
                res.setStatus(500);
                monitor.error(e);
            } catch (InvalidContentTypeException e) {
                res.setStatus(400);
                monitor.error(e);
            }       	
        }        
        
    }
	
	private String read(InputStream stream, String encoding) throws IOException {
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        IOHelper.copy(stream, outputStream);
	        return outputStream.toString(encoding);
	}

	public void destroy() {
	}
	
	public void onSuspend(AtmosphereResourceEvent event) {
		monitor.eventing(event.toString());		
	}

	public void onResume(AtmosphereResourceEvent event) {
		monitor.eventing(event.toString());
	}

	public void onDisconnect(AtmosphereResourceEvent event) {
		monitor.eventing(event.toString());
	}

	public void onBroadcast(AtmosphereResourceEvent event) {
		monitor.eventing(event.toString());
	}

	public void onThrowable(AtmosphereResourceEvent event) {
		monitor.error(event.throwable());
	}

	public void onHandshake(WebSocketEvent event) {
		monitor.eventingWS(event.toString());
	}	

	public void onClose(WebSocketEvent event) {
		monitor.eventingWS(event.toString());
	}

	public void onControl(WebSocketEvent event) {
		monitor.eventingWS(event.toString());
	}

	public void onDisconnect(WebSocketEvent event) {
		monitor.eventingWS(event.toString());
	}

	public void onConnect(WebSocketEvent event) {
		monitor.eventingWS(event.toString());
	}

	public void onMessage(WebSocketEvent event) {
		monitor.eventingWS(event.toString());
	}
	
}
