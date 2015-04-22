package org.fabric3.binding.zeromq.runtime.message;

import java.util.List;

import org.fabric3.binding.zeromq.runtime.broker.SpecifiedPort;
import org.fabric3.spi.discovery.AbstractEntry;
import org.fabric3.spi.discovery.EntryChange;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.spi.host.Port;
import static java.util.stream.Collectors.toList;

/**
 *
 */
public class AddressUpdater {

    public static List<SocketAddress> accept(EntryChange change, AbstractEntry entry, List<SocketAddress> old) {
        if (change == EntryChange.DELETE || change == EntryChange.EXPIRE) {
            return old.stream().filter(address -> !address.getAddress().equals(entry.getAddress()) && address.getPort().getNumber() == entry.getPort()).collect(
                    toList());

        } else {
            Port port = new SpecifiedPort(entry.getPort());
            SocketAddress address = new SocketAddress("", "", entry.getTransport(), entry.getAddress(), port);
            old.add(address);
            return old;
        }
    }

    private AddressUpdater() {
    }
}
