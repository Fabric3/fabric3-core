package org.fabric3.hazelcast.discovery;

import java.io.IOException;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryMergedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import org.fabric3.api.MonitorChannel;
import org.fabric3.spi.discovery.EntryChange;

/**
 *
 */
public class EntryListenerAdapter<T> implements EntryUpdatedListener<String, String>, EntryEvictedListener<String, String>, EntryMergedListener<String, String>,
                                                EntryRemovedListener<String, String>, EntryAddedListener<String, String> {

    private Class<T> type;
    private BiConsumer<EntryChange, T> listener;
    private ObjectMapper mapper;
    private MonitorChannel monitor;

    public EntryListenerAdapter(Class<T> type, BiConsumer<EntryChange, T> listener, ObjectMapper mapper, MonitorChannel monitor) {
        this.type = type;
        this.listener = listener;
        this.mapper = mapper;
        this.monitor = monitor;
    }

    public void entryAdded(EntryEvent<String, String> event) {
        try {
            listener.accept(EntryChange.SET, mapper.readValue(event.getValue(), type));
        } catch (IOException e) {
            monitor.severe("Error deserializing entry", e);
        }
    }

    public void entryEvicted(EntryEvent<String, String> event) {
        try {
            listener.accept(EntryChange.DELETE, mapper.readValue(event.getValue(), type));
        } catch (IOException e) {
            monitor.severe("Error deserializing entry", e);
        }
    }

    public void entryRemoved(EntryEvent<String, String> event) {
        try {
            listener.accept(EntryChange.DELETE, mapper.readValue(event.getValue(), type));
        } catch (IOException e) {
            monitor.severe("Error deserializing entry", e);
        }

    }

    public void entryMerged(EntryEvent<String, String> event) {
        try {
            listener.accept(EntryChange.SET, mapper.readValue(event.getValue(), type));
        } catch (IOException e) {
            monitor.severe("Error deserializing entry", e);
        }
    }

    public void entryUpdated(EntryEvent<String, String> event) {
        try {
            listener.accept(EntryChange.SET, mapper.readValue(event.getValue(), type));
        } catch (IOException e) {
            monitor.severe("Error deserializing entry", e);
        }
    }
}