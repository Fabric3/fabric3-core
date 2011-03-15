package org.fabric3.federation.jgroups.zmq;

import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.MessageReceiver;
import org.fabric3.spi.federation.ZoneChannelException;
import org.fabric3.spi.federation.ZoneTopologyService;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;

@EagerInit
@Scope("COMPOSITE")
public class ZeroMQZoneTopologyService implements MessageReceiver {

    @Reference(required = false)
    protected ZoneTopologyService topologyService;

    public ZeroMQZoneTopologyService() {
    }

    @Init
    public void init() {
        try {
            if (topologyService != null && topologyService.supportsDynamicChannels())
                topologyService.openChannel("hudri", null, this);
        } catch (ZoneChannelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // System.out.println("Got TopologyService :" + topologyService);
        try {
            if (topologyService != null)
                topologyService.sendAsynchronous("hudri", "helloho des funtzt");
        } catch (MessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Object object) {
        // TODO Auto-generated method stub
        System.out.println("Received :" + object.toString());
    }

}
