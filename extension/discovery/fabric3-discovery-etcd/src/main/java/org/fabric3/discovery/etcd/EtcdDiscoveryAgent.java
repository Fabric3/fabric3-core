package org.fabric3.discovery.etcd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.fabric3.api.MonitorChannel;
import org.fabric3.api.annotation.Source;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.discovery.AbstractEntry;
import org.fabric3.spi.discovery.ChannelEntry;
import org.fabric3.spi.discovery.DiscoveryAgent;
import org.fabric3.spi.discovery.EntryChange;
import org.fabric3.spi.discovery.ServiceEntry;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.JoinDomain;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Agent that works with an etcd cluster.
 */
public class EtcdDiscoveryAgent implements DiscoveryAgent {
    public static final String V2_KEYS = "/v2/keys/";

    private String[] addresses;
    private volatile String pinnedAddress;

    private int retries = 3;
    private long retryInterval = 2000;
    private long sleepInterval = 2000;
    private long ttl = 10000; // time-to-live im milliseconds

    private AtomicBoolean active;

    private String authority;
    private OkHttpClient client;
    private ObjectMapper mapper;
    private int index;

    private Map<String, List<BiConsumer<EntryChange, ServiceEntry>>> serviceListeners = new HashMap<>(); // service name to listeners
    private Map<String, List<BiConsumer<EntryChange, ChannelEntry>>> channelListeners = new HashMap<>(); // channel name to listeners
    private Map<String, Request> updatingEntries = new HashMap<>();

    @Reference
    protected HostInfo info;

    @Reference
    protected ExecutorService executorService;

    @Reference
    protected EventService eventService;

    @Monitor
    protected MonitorChannel monitor;

    @Property(required = false)
    @Source("$systemConfig/f3:etcd/@addresses")
    public void setAddresses(String value) {
        List<String> list = Arrays.asList(value.split(" "));
        Collections.shuffle(list);  // randomize the array so different addresses are picked on runtimes
        addresses = list.toArray(new String[list.size()]);
    }

    @Init
    public void init() {
        authority = info.getDomain().getAuthority();
        active = new AtomicBoolean(true);
        client = new OkHttpClient();
        client.setConnectTimeout(0, TimeUnit.MILLISECONDS);
        mapper = new ObjectMapper();
        pinnedAddress = getAddress();

        executorService.submit(this::ttlUpdateTask);
        executorService.submit(this::changeListenerTask);

        eventService.subscribe(JoinDomain.class, (event) -> {

        });
    }

    @Destroy
    void destroy() {
        active.set(false);
    }

    public boolean isLeader() {
        return false;
    }

    @SuppressWarnings("unchecked")
    public List<ServiceEntry> getServiceEntries(String name) {
        return getEntries(ServiceEntry.class, name);
    }

    public List<ChannelEntry> getChannelEntries(String name) {
        return getEntries(ChannelEntry.class, name);
    }

    public void register(ServiceEntry entry) {
        register(entry, "services");
    }

    public void unregisterService(String name) {
        unregister(name, "services");
    }

    public void unregisterChannel(String name) {
        unregister(name, "channels");
    }

    public void register(ChannelEntry entry) {
        register(entry, "channels");
    }

    public void registerLeadershipListener(Consumer<Boolean> consumer) {
    }

    public void registerServiceListener(String name, BiConsumer<EntryChange, ServiceEntry> listener) {
        List<BiConsumer<EntryChange, ServiceEntry>> list = serviceListeners.computeIfAbsent(name, k -> new ArrayList<>());
        list.add(listener);
    }

    public void unregisterServiceListener(String name, BiConsumer<EntryChange, ServiceEntry> listener) {
        List<BiConsumer<EntryChange, ServiceEntry>> list = serviceListeners.get(name);
        if (list != null) {
            list.remove(listener);
            if (list.isEmpty()) {
                serviceListeners.remove(name);
            }
        }
    }

    public void registerChannelListener(String name, BiConsumer<EntryChange, ChannelEntry> listener) {
        List<BiConsumer<EntryChange, ChannelEntry>> list = channelListeners.computeIfAbsent(name, k -> new ArrayList<>());
        list.add(listener);
    }

    public void unregisterChannelListener(String name, BiConsumer<EntryChange, ChannelEntry> listener) {
        List<BiConsumer<EntryChange, ChannelEntry>> list = channelListeners.get(name);
        if (list != null) {
            list.remove(listener);
            if (list.isEmpty()) {
                channelListeners.remove(name);
            }
        }
    }

    /**
     * Listens for changes to etcd keys and notifies registered listeners.
     */
    @SuppressWarnings("unchecked")
    private void changeListenerTask() {
        while (active.get()) {
            try {
                String address = pinnedAddress;
                Request request = new Request.Builder().url(address + "/v2/keys/" + authority + "?wait=true&recursive=true").get().build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    Map<String, Object> data = mapper.readValue(response.body().byteStream(), Map.class);
                    String action = (String) data.get("action");
                    if ("set".equals(action)) {
                        processChange((Map) data.get("node"), EntryChange.SET);
                    } else if ("delete".equals(action)) {
                        processChange((Map) data.get("prevNode"), EntryChange.DELETE);
                    } else if ("expire".equals(action)) {
                        processChange((Map) data.get("prevNode"), EntryChange.EXPIRE);
                    } else {
                        monitor.debug("Invalid action returned from etcd key watch: " + action);
                    }
                } else {
                    Thread.sleep(sleepInterval);
                }
            } catch (Exception e) {
                monitor.severe("Error listening to etcd", e);
                synchronized (this) {
                    pinnedAddress = getAddress();
                }
            }

        }
    }

    /**
     * Updates service and channel entries periodically (half the TTL value).
     */
    private void ttlUpdateTask() {
        while (active.get()) {
            for (Map.Entry<String, Request> entry : updatingEntries.entrySet()) {
                executeUpdate(entry.getKey(), entry.getValue());
            }
            try {
                Thread.sleep(ttl / 2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processChange(Map<String, Object> node, EntryChange change) {
        String value = (String) node.get("value");
        String key = (String) node.get("key");
        if (value != null) {
            if (key.startsWith("/services/")) {
                notifyServiceChange(value, change);
            } else if (key.startsWith("/channels/")) {
                notifyChannelChange(value, change);
            }
        } else {
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) node.getOrDefault("nodes", Collections.emptyList());
            for (Map<String, Object> nodeEntry : nodes) {
                String nodeValue = (String) nodeEntry.get("value");
                if (nodeValue != null) {
                    if (key.startsWith("/services/")) {
                        notifyServiceChange(value, change);
                    } else if (key.startsWith("/channels/")) {
                        notifyChannelChange(value, change);
                    }
                }
            }
        }
    }

    private void notifyServiceChange(String value, EntryChange change) {
        try {
            ServiceEntry entry = mapper.readValue(value, ServiceEntry.class);
            List<BiConsumer<EntryChange, ServiceEntry>> listeners = serviceListeners.getOrDefault(entry.getName(), Collections.emptyList());
            listeners.forEach(l -> l.accept(change, entry));
        } catch (IOException e) {
            monitor.severe("Error deserializing service entry update", e);
        }
    }

    private void notifyChannelChange(String value, EntryChange change) {
        try {
            ChannelEntry entry = mapper.readValue(value, ChannelEntry.class);
            List<BiConsumer<EntryChange, ChannelEntry>> listeners = channelListeners.getOrDefault(entry.getName(), Collections.emptyList());
            listeners.forEach(l -> l.accept(change, entry));
        } catch (IOException e) {
            monitor.severe("Error deserializing channel entry update", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractEntry> List<T> getEntries(Class<T> type, String name) {
        String prefix = ServiceEntry.class.equals(type) ? "services" : "channels";
        String address = pinnedAddress;
        while (true) {
            try {
                Request request = new Request.Builder().url(address + V2_KEYS + authority + "/" + prefix).build();
                Response response = client.newCall(request).execute();
                Map<String, Object> body = mapper.readValue(response.body().string(), Map.class);

                Map<String, Object> servicesDir = (Map<String, Object>) body.get("node");
                if (servicesDir == null) {
                    monitor.debug("Directory {0} not found in etcd", prefix);
                    return Collections.emptyList();
                }
                List<Map<String, Object>> nodes = (List<Map<String, Object>>) servicesDir.get("nodes");
                List<T> entries = new ArrayList<>();
                for (Map<String, Object> node : nodes) {
                    String value = (String) node.get("value");
                    T entry = parseEntry(type, value);
                    if (name.equals("*") || name.equals(entry.getName())) { // filter on requested name
                        entries.add(entry);
                    }
                }
                return entries;
            } catch (IOException e) {
                monitor.severe("Error retrieving addresses from etcd for: {0}. Trying next etcd instance.", name, e);
                synchronized (this) {
                    pinnedAddress = getAddress();
                    if (pinnedAddress.equals(address)) {
                        // all of the available addresses have been recycled
                        monitor.severe("No available etcd instance retrieving addresses for: {0}", name);
                        return Collections.emptyList();
                    }
                }
            }
        }
    }

    private void register(AbstractEntry entry, String entryType) {
        entry.freeze();
        String value;
        try {
            value = mapper.writeValueAsString(entry);
        } catch (JsonProcessingException e) {
            monitor.severe("Error serializing entry", e);
            return;
        }

        String expiration = String.valueOf(ttl / 1000); // convert to seconds used by etcd
        RequestBody body = new FormEncodingBuilder().add("value", value).add("ttl", expiration).build();
        String key = info.getRuntimeName() + ":" + entry.getName();

        String address = pinnedAddress;
        Request request = new Request.Builder().url(address + V2_KEYS + authority + "/" + entryType + "/" + key).put(body).build();
        updatingEntries.put(key, request);

        executeUpdate(key, request);
    }

    private void executeUpdate(String key, Request request) {
        int retryCount = retries;
        while (retryCount > 0) {
            retryCount--;
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    monitor.severe("Error registering {0} with etc: {1}", key, response.code());
                } else {
                    monitor.debug("Registered {0} with etcd", key);
                    return;
                }
            } catch (IOException e) {
                monitor.severe("Error registering {0}. Waiting to retry.", key, e);
            }
            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void unregister(String name, String entryType) {
        int retryCount = retries;
        while (retryCount > 0) {
            retryCount--;
            try {
                String key = info.getRuntimeName() + ":" + name;

                String address = pinnedAddress;
                Request request = new Request.Builder().url(address + V2_KEYS + authority + "/" + entryType + "/" + key).delete().build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    monitor.severe("Error un-registering entry {0} from etc: {1}", name, response.code());
                } else {
                    monitor.debug("Un-registered entry from etcd {0}", name);
                    return;
                }
            } catch (IOException e) {
                monitor.severe("Error un-registering entry {0} from etcd. Waiting to retry.", name, e);
            }
            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractEntry> T parseEntry(Class<T> type, String value) throws IOException {
        return ServiceEntry.class.equals(type) ? (T) mapper.readValue(value, ServiceEntry.class) : (T) mapper.readValue(value, ChannelEntry.class);
    }

    /**
     * Round-robins between configured etcd addresses.
     *
     * @return the next address
     */
    private synchronized String getAddress() {
        if (index == addresses.length) {
            index = 0;
        }
        return addresses[index++];
    }

}
