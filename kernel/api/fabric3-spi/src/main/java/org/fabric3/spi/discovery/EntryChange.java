package org.fabric3.spi.discovery;

/**
 * Denotes setting (add or update) deleting, and expiring a discovery key.
 */
public enum EntryChange {
    SET, DELETE, EXPIRE
}
