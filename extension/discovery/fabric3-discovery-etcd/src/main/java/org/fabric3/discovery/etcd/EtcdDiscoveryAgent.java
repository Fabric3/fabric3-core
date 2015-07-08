package org.fabric3.discovery.etcd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.spi.discovery.AbstractEntry;
import org.fabric3.spi.discovery.ChannelEntry;
import org.fabric3.spi.discovery.ConfigurationAgent;
import org.fabric3.spi.discovery.DiscoveryAgent;
import org.fabric3.spi.discovery.EntryChange;
import org.fabric3.spi.discovery.ServiceEntry;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Agent that works with an etcd cluster.
 *
 * Service and channel entries are placed under {@code [domain]/services/} and {@code [domain]/channels} respectively. The top-level {@code [domain]} directory
 * is watched for changes in a background thread, which are dispatched to registered listeners. Each entry has a TTL, which is periodically updated in another
 * background thread.
 *
 * This implementation also supports leader election based on the ZooKeeper algorithm described here:
 *
 * https://zookeeper.apache.org/doc/trunk/recipes.html#sc_leaderElection
 *
 * Each runtime posts an in-order key with its name to {@code [domain]/[leader/[zone]}. The runtime which has posted the first key (i.e. the one with the lowest
 * created index) is elected leader. Leadership changes are watched by the same background thread which monitors service and channel entry changes. The
 * background TTL thread also periodically updates leadership entries.
 */
@EagerInit
public class EtcdDiscoveryAgent implements DiscoveryAgent, ConfigurationAgent {
    public static final String V2_KEYS = "/v2/keys/";

    private String[] addresses = {"http://127.0.0.1:4001"};

    @Property(required = false)
    @Source("$systemConfig/f3:etcd/@retries")
    protected int retries = 3;

    @Property(required = false)
    @Source("$systemConfig/f3:etcd/@retry.interval")
    protected long retryInterval = 2000;

    @Property(required = false)
    @Source("$systemConfig/f3:etcd/@sleep.interval")
    protected long sleepInterval = 2000;

    @Property(required = false)
    @Source("$systemConfig/f3:etcd/@ttl")
    protected long ttl = 10000; // time-to-live im milliseconds

    protected boolean leaderElectionEnabled = true;  // for testing

    private volatile boolean active;

    private String authority;
    private OkHttpClient client;
    private ObjectMapper mapper;
    private int index;

    private Map<String, List<BiConsumer<EntryChange, ServiceEntry>>> serviceListeners = new HashMap<>(); // service name to listeners
    private Map<String, List<BiConsumer<EntryChange, ChannelEntry>>> channelListeners = new HashMap<>(); // channel name to listeners
    private List<Consumer<Boolean>> leaderListeners = new ArrayList<>();
    private Map<String, List<Consumer<String>>> configurationListeners = new HashMap<>(); // key to listeners

    private Map<String, Request> updatingEntries = new HashMap<>();

    private volatile String pinnedAddress;
    private volatile String currentLeader;

    @Reference
    protected HostInfo info;

    @Reference
    protected ExecutorService executorService;

    @Monitor
    protected MonitorChannel monitor;

    @Property(required = false)
    @Source("$systemConfig/f3:etcd/@addresses")
    public void setAddresses(String value) {
        List<String> list = Arrays.asList(value.split(" "));
        Collections.shuffle(list);  // randomize the array so different addresses are picked on runtimes when booted
        addresses = list.toArray(new String[list.size()]);
    }

    @Init
    public void init() {
        authority = info.getDomain().getAuthority();
        active = true;
        client = new OkHttpClient();
        client.setConnectTimeout(0, TimeUnit.MILLISECONDS);
        mapper = new ObjectMapper();
        pinnedAddress = getAddress();

        if (RuntimeMode.NODE == info.getRuntimeMode() && leaderElectionEnabled) {
            // only enable leader election on node runtime
            createLeaderEntry();
            checkLeader();
        }

        executorService.submit(this::ttlUpdateTask);
        executorService.submit(this::changeListenerTask);
    }

    @Destroy
    void destroy() {
        active = false;
    }

    public boolean isLeader() {
        String leader = currentLeader;
        return leader != null && leader.equals(info.getRuntimeName());
    }

    @SuppressWarnings("unchecked")
    public List<ServiceEntry> getServiceEntries(String name) {
        return getEntries(ServiceEntry.class, name);
    }

    public List<ChannelEntry> getChannelEntries(String name) {
        return getEntries(ChannelEntry.class, name);
    }

    public void register(ServiceEntry entry) {
        monitor.debug("Registering {0} with etcd", entry.getName());
        register(entry, "services");
    }

    public void unregisterService(String name) {
        unregister(name, "services");
    }

    public void unregisterChannel(String name) {
        unregister(name, "channels");
    }

    public void register(ChannelEntry entry) {
        monitor.debug("Registering {0} with etcd", entry.getName());
        register(entry, "channels");
    }

    public void registerLeadershipListener(Consumer<Boolean> consumer) {
        leaderListeners.add(consumer);
    }

    public void unRegisterLeadershipListener(Consumer<Boolean> consumer) {
        leaderListeners.remove(consumer);
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

    @SuppressWarnings("unchecked")
    public String getValue(String key) {
        String address = pinnedAddress;
        while (true) {
            try {
                Request request = new Request.Builder().url(address + V2_KEYS + authority + "/configuration/" + key).build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    monitor.debug("Response retrieving configuration {0}: {1}", key, response.code());
                    return null;
                }
                Map<String, Object> body = mapper.readValue(response.body().string(), Map.class);
                Map<String, Object> node = (Map<String, Object>) body.get("node");
                if (node == null) {
                    monitor.severe("Error retrieving configuration from etcd for: {0}. Node property not found.", key);
                    return null;
                }
                return (String) node.get("value");
            } catch (IOException e) {
                monitor.severe("Error retrieving configuration from etcd for: {0}. Trying next etcd instance.", key, e);
                synchronized (this) {
                    pinnedAddress = getAddress();
                    if (pinnedAddress.equals(address)) {
                        // all of the available addresses have been recycled
                        monitor.severe("No available etcd instance retrieving configuration for: {0}", key);
                        return null;
                    }
                }
            }
        }
    }

    public void registerListener(String key, Consumer<String> listener) {
        List<Consumer<String>> list = configurationListeners.computeIfAbsent(key, k -> new ArrayList<>());
        list.add(listener);
    }

    public void unregisterListener(String key, Consumer<String> listener) {
        List<Consumer<String>> list = configurationListeners.get(key);
        if (list != null) {
            list.remove(listener);
            if (list.isEmpty()) {
                channelListeners.remove(key);
            }
        }
    }

    /**
     * Listens for changes to etcd keys and notifies registered listeners.
     */
    @SuppressWarnings({"unchecked", "StatementWithEmptyBody"})
    private void changeListenerTask() {
        while (active) {
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
                    } else if ("create".equals(action)) {
                        // ignore creates
                    } else if ("update".equals(action)) {
                        // ignore updates as they will be triggered by ttl updates
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
        while (active) {
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

    /**
     * Processes a change received from etcd. Changes may be a modification of a service or channel entry, or a change in leadership.
     *
     * @param node   the change node
     * @param change the change type
     */
    @SuppressWarnings("unchecked")
    private void processChange(Map<String, Object> node, EntryChange change) {
        String value = (String) node.get("value");
        String key = (String) node.get("key");
        if (value != null) {
            if (key.startsWith("/" + authority + "/services/")) {
                notifyServiceChange(value, change);
            } else if (key.startsWith("/" + authority + "/channels/")) {
                notifyChannelChange(value, change);
            } else if (key.startsWith("/" + authority + "/leader/")) {
                checkLeader();
            }
        } else {
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) node.getOrDefault("nodes", Collections.emptyList());
            for (Map<String, Object> nodeEntry : nodes) {
                String nodeValue = (String) nodeEntry.get("value");
                if (nodeValue != null) {
                    if (key.startsWith("/" + authority + "/services/")) {
                        notifyServiceChange(value, change);
                    } else if (key.startsWith("/" + authority + "/channels/")) {
                        notifyChannelChange(value, change);
                    }
                }
            }
        }
    }

    /**
     * Notifies listeners of a service change.
     *
     * @param value  the key value containing the service entry data
     * @param change the change type
     */
    private void notifyServiceChange(String value, EntryChange change) {
        try {
            ServiceEntry entry = mapper.readValue(value, ServiceEntry.class);
            List<BiConsumer<EntryChange, ServiceEntry>> listeners = serviceListeners.getOrDefault(entry.getName(), Collections.emptyList());
            listeners.forEach(l -> l.accept(change, entry));
        } catch (IOException e) {
            monitor.severe("Error deserializing service entry update", e);
        }
    }

    /**
     * Notifies listeners of a channel change.
     *
     * @param value  the key value containing the channel entry data
     * @param change the change type
     */
    private void notifyChannelChange(String value, EntryChange change) {
        try {
            ChannelEntry entry = mapper.readValue(value, ChannelEntry.class);
            List<BiConsumer<EntryChange, ChannelEntry>> listeners = channelListeners.getOrDefault(entry.getName(), Collections.emptyList());
            listeners.forEach(l -> l.accept(change, entry));
        } catch (IOException e) {
            monitor.severe("Error deserializing channel entry update", e);
        }
    }

    /**
     * Returns service or channel entries matching a name.
     *
     * @param type the service or channel type
     * @param name the name
     * @return the entries
     */
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

    /**
     * POSTs a leader entry under {@code [domain]/leader/[zone]} as part of the leader election process.
     */
    private void createLeaderEntry() {
        String expiration = String.valueOf(ttl / 1000); // convert to seconds used by etcd
        RequestBody body = new FormEncodingBuilder().add("value", info.getRuntimeName()).add("ttl", expiration).build();
        String key = "leader:" + info.getRuntimeName();

        String address = pinnedAddress;
        Request request = new Request.Builder().url(address + V2_KEYS + authority + "/leader/" + info.getZoneName()).post(body).build();
        Optional<Response> optional = executeUpdate(key, request);
        // get the key
        optional.ifPresent(this::scheduleLeaderUpdate);

    }

    /**
     * Schedules a leader update request to be run periodically
     *
     * @param response the response containing a newly created leader entry
     */
    @SuppressWarnings("unchecked")
    private void scheduleLeaderUpdate(Response response) {
        try {
            Map<String, Object> body = mapper.readValue(response.body().string(), Map.class);
            Map<String, Object> node = (Map<String, Object>) body.get("node");
            if (node == null) {
                monitor.debug("Leader node for zone {0} not found in etcd", info.getZoneName());
                return;
            }
            String key = ((String) node.get("key")).substring(1);  // strip off leading '/'
            String runtime = (String) node.get("value");
            if (info.getRuntimeName().equals(runtime)) {
                String expiration = String.valueOf(ttl / 1000); // convert to seconds used by etcd
                FormEncodingBuilder builder = new FormEncodingBuilder();
                RequestBody lBody = builder.add("value", info.getRuntimeName()).add("ttl", expiration).add("prevExist", "true").build();
                String address = pinnedAddress;
                Request lRequest = new Request.Builder().url(address + V2_KEYS + key).put(lBody).build();
                updatingEntries.put(key, lRequest);
            }
        } catch (Exception e) {
            monitor.debug("Error performing leader election with etcd", e);
        }
    }

    /**
     * Checks and records the zone leader.
     */
    @SuppressWarnings("unchecked")
    private void checkLeader() {
        String address = pinnedAddress;
        while (true) {
            try {
                String url = address + V2_KEYS + authority + "/leader/" + info.getZoneName() + "?recursive=true&sorted=true";
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                Map<String, Object> body = mapper.readValue(response.body().string(), Map.class);

                Map<String, Object> leaderDir = (Map<String, Object>) body.get("node");
                if (leaderDir == null) {
                    monitor.debug("Leader directory {0} not found in etcd", info.getZoneName());
                    return;
                }
                List<Map<String, Object>> nodes = (List<Map<String, Object>>) leaderDir.get("nodes");
                if (nodes == null || nodes.isEmpty()) {
                    return;
                }
                String previousLeader = currentLeader;
                currentLeader = (String) nodes.get(0).get("value");
                boolean isLeader = isLeader();
                if (!currentLeader.equals(previousLeader)) {
                    leaderListeners.forEach(c -> c.accept(isLeader));
                    if (isLeader) {
                        monitor.debug("Runtime elected leader for zone {0}", info.getZoneName());
                    } else if (info.getRuntimeName().equals(previousLeader)) {
                        monitor.debug("Runtime relinquishing leader role for zone {0}", info.getZoneName());
                    }
                }
                return;
            } catch (IOException e) {
                monitor.severe("Error retrieving leadership information from etcd. Trying next etcd instance.", e);
                synchronized (this) {
                    pinnedAddress = getAddress();
                    if (pinnedAddress.equals(address)) {
                        // all of the available addresses have been recycled
                        monitor.severe("No available etcd instance while retrieving leadership information");
                        return;
                    }
                }
            }
        }

    }

    /**
     * Registers a service or channel entry.
     *
     * @param entry     the entry
     * @param entryType the type
     */
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

    private Optional<Response> executeUpdate(String key, Request request) {
        int retryCount = retries;
        while (retryCount > 0) {
            retryCount--;
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    monitor.severe("Error registering {0} with etcd: {1}", key, response.code());
                } else {
                    return Optional.of(response);
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
        return Optional.empty();
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
