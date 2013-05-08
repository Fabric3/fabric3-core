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

import org.fabric3.binding.file.api.InvalidDataException;
import org.fabric3.binding.file.api.ServiceAdapter;
import org.fabric3.binding.file.common.Strategy;
import org.fabric3.host.util.IOHelper;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextCache;
import org.fabric3.spi.wire.Interceptor;

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

    private Map<String, FileEntry> cache = new ConcurrentHashMap<String, FileEntry>();
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
        List<File> files = new ArrayList<File>();
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
            WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
            Object[] payload;
            try {
                payload = adapter.beforeInvoke(file);
            } catch (InvalidDataException e) {
                monitor.error(e);
                // invalid file, return and send it the the error directory
                handleError(file, e);
                return;
            }
            try {
                Message response = dispatch(payload, workContext);
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
            }
        } finally {
            releaseLock(lockFile, fileLock, lockChannel);
        }
    }

    private Message dispatch(Object[] payload, WorkContext workContext) {
        Message message = new MessageImpl();
        message.setWorkContext(workContext);
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
