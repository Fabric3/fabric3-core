/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.file.runtime.receiver;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.fabric3.api.binding.file.InvalidDataException;
import org.fabric3.api.binding.file.ServiceAdapter;
import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.host.util.IOHelper;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.Interceptor;

/**
 * Periodically scans a directory for new files. When a new file is detected, the service bound to the directory is invoked with expected data types associated
 * with each file. After an invocation completes, the detected file is either archived or deleted according to the configured {@link Strategy} value. If an
 * error is encountered, the file will be moved to the configured error location.
 * <p/>
 * This receiver is non-transactional but supports clustered locking through the use of file locks placed in the &lt;location&gt;/locks directory.
 */
@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class FileSystemReceiver implements Runnable {
    private File location;
    private File lockDirectory;
    private File errorDirectory;
    private File archiveDirectory;
    private Strategy strategy;
    private Pattern filePattern;

    private long delay;

    private Interceptor interceptor;
    private ScheduledExecutorService executorService;
    private ServiceAdapter adapter;
    private ReceiverMonitor monitor;

    private Map<String, FileEntry> cache = new ConcurrentHashMap<>();
    private ScheduledFuture<?> future;

    public FileSystemReceiver(ReceiverConfiguration configuration) {
        this.location = configuration.getLocation();
        this.strategy = configuration.getStrategy();
        this.errorDirectory = configuration.getErrorLocation();
        this.archiveDirectory = configuration.getArchiveLocation();
        this.filePattern = configuration.getFilePattern();
        this.interceptor = configuration.getInterceptor();
        this.monitor = configuration.getMonitor();
        this.lockDirectory = configuration.getLockDirectory();
        this.adapter = configuration.getAdapter();
        this.delay = configuration.getDelay();
    }

    public void start() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        future = executorService.scheduleWithFixedDelay(this, delay, delay, TimeUnit.MILLISECONDS);
        createDirectories();
    }

    public void stop() {
        if (future != null) {
            future.cancel(true);
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public synchronized void run() {
        List<File> files = new ArrayList<>();
        if (!location.isDirectory()) {
            // there is no drop directory, return without processing
            return;
        }
        File[] pathFiles = location.listFiles();
        for (File file : pathFiles) {
            // add non-ignorable files
            if (!ignore(file)) {
                files.add(file);
            }
        }
        if (files.isEmpty()) {
            // there are no files to process
            return;
        }
        try {
            processFiles(files);
        } catch (RuntimeException e) {
            monitor.error(e);
        } catch (Error e) {
            monitor.error(e);
            throw e;
        }
    }

    void createDirectories() {
        lockDirectory.mkdirs();
        errorDirectory.mkdirs();
        if (archiveDirectory != null) {
            // archive directory is optional if the strategy is delete
            archiveDirectory.mkdirs();
        }
    }

    private synchronized void processFiles(List<File> files) {
        for (File file : files) {
            String name = file.getName();
            FileEntry cached = cache.get(name);
            if (cached == null) {
                // the file is new, cache it and wait for next run in case it is in the process of being updated
                cached = new FileEntry(file);
                cache.put(name, cached);
            } else {
                if (!cached.isChanged()) {
                    // file has finished being updated, process it
                    processFile(file);
                }
            }
        }
    }

    private boolean ignore(File file) {
        String name = file.getName();
        return name.startsWith(".") || file.isDirectory() || !filePattern.matcher(name).matches();
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private void processFile(File file) {
        String name = file.getName();
        // remove file from the cache as it will either be processed by this runtime or skipped
        cache.remove(name);
        // attempt to lock the file
        FileChannel lockChannel;
        FileLock fileLock;
        File lockFile = new File(lockDirectory, file.getName() + ".f3");
        try {
            // Always attempt to lock since a lock file could have been created by this or another VM before it crashed. In this case,
            // the lock file is orphaned and another VM must continue processing.
            lockChannel = new RandomAccessFile(lockFile, "rw").getChannel();
            fileLock = lockChannel.tryLock();
            if (fileLock == null) {
                // file lock is held bu another VM. ignore
                return;
            }
        } catch (OverlappingFileLockException e) {
            // already being processed by this VM, ignore
            return;
        } catch (IOException e) {
            // error acquiring the lock, skip processing
            monitor.error(e);
            return;
        }
        try {
            Object[] payload;
            try {
                payload = adapter.beforeInvoke(file);
            } catch (InvalidDataException e) {
                monitor.error(e);
                // invalid file, return and send it the the error directory
                handleError(file, e);
                return;
            }
            WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
            Message message = MessageCache.getAndResetMessage();
            try {
                message.setWorkContext(workContext);
                Message response = dispatch(payload, message);
                afterInvoke(file, payload);
                if (response.isFault()) {
                    // the service threw an exception. this is interpreted as a bad file. Move the file to the error location
                    Exception error = (Exception) response.getBody();
                    handleError(file, error);
                    monitor.error("Error processing file: " + file.getName(), error);
                } else {
                    if (Strategy.ARCHIVE == strategy) {
                        archiveFile(file);
                    } else {
                        deleteFile(file);
                    }
                }
            } catch (RuntimeException e) {
                // an unexpected runtime error, try and close the resources and retry
                afterInvoke(file, payload);
                throw e;
            } finally {
                message.reset();
                workContext.reset();
            }
        } finally {
            releaseLock(lockFile, fileLock, lockChannel);
        }
    }

    private Message dispatch(Object[] payload, Message message) {
        message.setBody(payload);
        return interceptor.invoke(message);
    }

    private void afterInvoke(File file, Object[] payload) {
        try {
            adapter.afterInvoke(file, payload);
        } catch (IOException e) {
            monitor.error(e);
        }
    }

    private void archiveFile(File file) {
        try {
            adapter.archive(file, archiveDirectory);
        } catch (IOException e) {
            monitor.error(e);
        }
    }

    private void deleteFile(File file) {
        try {
            adapter.delete(file);
        } catch (IOException e) {
            monitor.error(e);
        }
    }

    private void handleError(File file, Exception e) {
        try {
            adapter.error(file, errorDirectory, e);
        } catch (IOException ex) {
            monitor.error(ex);
        }
    }

    private void releaseLock(File lockFile, FileLock lock, FileChannel lockChannel) {
        if (lock != null) {
            try {
                lock.release();
                IOHelper.closeQuietly(lockChannel);
                if (lockFile.exists()) {
                    lockFile.delete();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
