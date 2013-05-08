package org.fabric3.binding.web.runtime.service;

import java.util.UUID;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.websocket.WebSocketEventListener;

import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

public class WebSocketServiceListener implements WebSocketEventListener {

    private final UUID uuid;
    private final ServiceMonitor monitor;
    private final ServiceManager serviceManager;

    public WebSocketServiceListener(UUID uuid, ServiceMonitor monitor, ServiceManager serviceManager) {
        this.uuid = uuid;
        this.monitor = monitor;
        this.serviceManager = serviceManager;
    }

    public void onSuspend(AtmosphereResourceEvent event) {
        monitor.eventing(event.toString());
        onMessage(event.getResource(), (String) event.getMessage());
    }

    public void onResume(AtmosphereResourceEvent event) {
        monitor.eventing(event.toString());
        onMessage(event.getResource(), (String) event.getMessage());
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
        monitor.eventing(event.toString());
    }

    public void onMessage(WebSocketEvent event) {
        monitor.eventing(event.toString());
        onMessage(event.webSocket().resource(), event.message());
    }

    public void onClose(WebSocketEvent event) {
        monitor.eventing(event.toString());
    }

    public void onControl(WebSocketEvent event) {
        monitor.eventing(event.toString());
    }

    public void onDisconnect(WebSocketEvent event) {
        monitor.eventing(event.toString());
    }

    public void onConnect(WebSocketEvent event) {
        monitor.eventing(event.toString());
    }

    private void onMessage(AtmosphereResource r, String _message) {
        if (_message == null) {
            return;
        }

        String path = r.getRequest().getPathInfo().substring(1);
        ChainPair chainPair = serviceManager.get(path);

        Object[] content = new Object[]{_message};
        WorkContext context = WorkContextTunnel.getAndResetThreadWorkContext();
        CallFrame frame = new CallFrame(chainPair.getCallbackUri(), uuid.toString());
        context.addCallFrame(frame);
        // As an optimization, we add the callframe twice instead of two different frames for representing the service call and the binding invocation 
        context.addCallFrame(frame);
        MessageImpl message = new MessageImpl(content, false, context);

        // Invoke the service and return a response using the broadcaster for this web socket
        chainPair.getChain().getHeadInterceptor().invoke(message);
    }

}
