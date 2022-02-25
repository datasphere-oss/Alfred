/*******************************************************************************
 *
 *
 *  Copyright (c) 2022 Alfred
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package org.apache.alfred.storage.tfs;

import org.apache.alfred.gflog.api.Log;
import org.apache.alfred.gflog.api.LogFactory;
import org.apache.alfred.storage.tfs.pub.AbstractFileSystem;
import org.apache.alfred.storage.tfs.pub.AbstractPath;
import org.apache.alfred.storage.tfs.DataReaderImpl;
import org.apache.alfred.storage.tfs.DataWriterImpl;
import org.apache.alfred.storage.tfs.LiveDataReaderImpl;
import org.apache.alfred.storage.tfs.pub.DataReader;
import org.apache.alfred.storage.tfs.pub.DataWriter;
import org.apache.alfred.storage.tfs.pub.EmergencyShutdownControl;
import org.apache.alfred.storage.tfs.pub.PersistentDataStore;
import org.apache.alfred.storage.tfs.pub.TSRoot;
import org.apache.alfred.util.collections.generated.ObjectArrayList;
import org.apache.alfred.util.concurrent.QuickExecutor;
import org.apache.alfred.util.concurrent.UncheckedInterruptedException;
import org.apache.alfred.util.runtime.Shutdown;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class PDSImpl implements PersistentDataStore {
    static final Log LOGGER = LogFactory.getLog("deltix.dtb");

    // After thant number of failures across any number of files we trigger
    // Emergency shutdown.
    private static final int FAILURES_TO_TRIGGER_SHUTDOWN = Integer.getInteger("TimeBase.storage.failuresToShutdown",
            5);

    // Additional attempts to save a file. Value 2 means that there may be up to 3
    // failed attempts in total per files.
    // If all attempts failed then Emergency shutdown will be triggered.
    private static final int ATTEMPTS_PER_FILE = Integer.getInteger("TimeBase.storage.attemptsPerFile", 2);

    // Number of Writer Threads
    private static final int NUMBER_OF_WRITERS = Integer.getInteger("TimeBase.storage.writers", 1);

    final Runtime RUNTIME = Runtime.getRuntime();

    private long maxFileSize;

    // //For debugging
    // static {
    // LogFactory.getLog("deltix.dtb").setLevel(LogLevel.DEBUG);
    // }

    private boolean isStarted = false;
    private boolean shutdownInProgress = false;
    private boolean isReadOnly = false;
    private final ObjectArrayList<TSFile> dirtyFiles = new ObjectArrayList<>();
    private int numWriters = NUMBER_OF_WRITERS;
    private final List<TSFWriterThread> writers = new ObjectArrayList<>(numWriters);

    private final Object cleanLock = new Object();
    private int numDirtyFiles = 0;
    private final QuickExecutor executor;
    private final QuickExecutor localExecutor;

    private final ObjectArrayList<TSFile> badDirtyFiles = new ObjectArrayList<>(); // Files that were not saved properly

    // Key: TSFile.getPathString(), Value: number of failures
    // We do not keep TSFile itself because it will mean preserving a permanent
    // reference to a file object.
    // Instead of this we just keep path to file.
    private final Map<String, AtomicInteger> failuresByFile = new HashMap<>();

    private EmergencyShutdownControl shutdownControl = null;
    private final AtomicInteger failedWriteAttempts = new AtomicInteger(0);

    // private ByteArrayHeap allocator;

    PDSImpl(QuickExecutor exe) {
        this.executor = exe;
        this.localExecutor = null;
    }

    PDSImpl() {
        this.executor = null;
        this.localExecutor = QuickExecutor.createNewInstance("PDSImpl Executor", null);
        this.localExecutor.reuseInstance();
    }

    //
    // PersistentDataStore IMPLEMENTATION
    //

    @Override
    public synchronized boolean isStarted() {
        return (isStarted);
    }

    @Override
    public synchronized void setReadOnly(boolean readOnly) {
        if (isStarted)
            throw new IllegalStateException("Already started");

        isReadOnly = readOnly;
    }

    @Override
    public synchronized boolean isReadOnly() {
        return (isReadOnly);
    }

    @Override
    public synchronized void setNumWriterThreads(int n) {
        numWriters = n;
    }

    @Override
    public synchronized void start() {
        if (isStarted)
            throw new IllegalStateException("Already started");

        for (int ii = 0; ii < numWriters; ii++) {
            TSFWriterThread wt = new TSFWriterThread(this, ii);

            wt.start();

            writers.add(wt);
        }

        isStarted = true;
    }

    public synchronized void startShutdown() {
        shutdownInProgress = true;
    }

    @Override
    public synchronized void shutdown() {
        isStarted = false;

        for (TSFWriterThread wt : writers)
            wt.interrupt();

        shutdownInProgress = false;

        if (localExecutor != null)
            localExecutor.shutdownInstance();

        // allocator = null;
    }

    @Override
    public boolean waitUntilDataStored(int timeout) {

        assert isStarted();

        long limit = timeout <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;

        synchronized (cleanLock) {
            for (;;) {
                assert numDirtyFiles >= 0 : "should be >=0 " + numDirtyFiles;

                if (numDirtyFiles <= 0)
                    return (true);

                long ttw = limit - System.currentTimeMillis();

                if (ttw <= 0)
                    return (false);

                try {
                    cleanLock.wait(ttw);
                } catch (InterruptedException x) {
                    throw new UncheckedInterruptedException(x);
                }
            }
        }
    }

    void checkWriteQueueLimit(long maxFileSize) {
        boolean logged = false;

        synchronized (cleanLock) {
            for (;;) {

                long freeMemory = RUNTIME.maxMemory() - (RUNTIME.totalMemory() - RUNTIME.freeMemory());
                if (freeMemory > maxFileSize * 5)
                    return;

                if (numDirtyFiles == 0)
                    return;

                try {
                    if (!logged) {
                        LOGGER.warn("Write Queue is overloaded [dirty files = %s]. Waiting ... ").with(numDirtyFiles);
                        logged = true;
                    }

                    cleanLock.wait(1000);
                } catch (InterruptedException x) {
                    throw new UncheckedInterruptedException(x);
                }
            }
        }
    }

    @Override
    public synchronized boolean waitForShutdown(int timeout) {
        long limit = timeout <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;

        for (TSFWriterThread wt : writers) {
            while (wt.isAlive()) {
                long ttw = limit - System.currentTimeMillis();

                if (ttw <= 0)
                    return (false);

                try {
                    wt.join(ttw);
                } catch (InterruptedException x) {
                    throw new UncheckedInterruptedException(x);
                }
            }
        }
        writers.clear();

        int dirtyFiles;
        synchronized (cleanLock) {
            dirtyFiles = numDirtyFiles;
        }

        LOGGER.info("Writers successfully stopped, files in queue = " + dirtyFiles);

        return (true);
    }

    synchronized void writerFailed(TSFWriterThread writer) {
        writers.remove(writer);
        if (writers.size() == 0) {
            LOGGER.error("CRITICAL ERROR IN Writer Threads. Timebase will shutdown.");
            triggerEmergencyShutdown();
        }
    }

    private void triggerEmergencyShutdown() {
        EmergencyShutdownControl st = this.shutdownControl;
        if (st != null) {
            st.triggerEmergencyShutdown();
        } else {
            Shutdown.asyncTerminate();
        }
    }

    @Override
    public TSRootFolder createRoot(@Nullable String space, AbstractFileSystem fs, String path) {
        checkIsStarted();
        checkShutdown();

        return new TSRootFolder(this, fs, path, space);
    }

    @Override
    public TSRoot createRoot(@Nullable String space, AbstractPath path) {
        checkIsStarted();
        checkShutdown();

        return new TSRootFolder(this, path.getFileSystem(), path.getPathString(), space);
    }

    @Override
    public DataWriter createWriter() {
        checkIsStarted();
        checkShutdown();

        return (new DataWriterImpl());
    }

    @Override
    public DataReader createReader(boolean live) {
        checkIsStarted();
        checkShutdown();

        QuickExecutor exe = this.executor != null ? executor : localExecutor;

        return live ? new LiveDataReaderImpl(exe) : new DataReaderImpl(exe);
    }

    @Override
    public void setEmergencyShutdownControl(EmergencyShutdownControl shutdownControl) {
        this.shutdownControl = shutdownControl;
    }

    // public ByteArrayHeap getHeap() {
    // return allocator;
    // }

    //
    // PACKAGE INTERFACE
    //
    boolean removeFromWriteQueue(TSFile tsf) {

        boolean removed;

        synchronized (dirtyFiles) {
            removed = dirtyFiles.remove(tsf);
            if (removed) {
                tsf.queued = false;
                dirtyFiles.notify();
            }
        }

        // file can be processed now by Writer Thread, so it's not contains in dirty
        // files
        if (removed) {
            synchronized (cleanLock) {
                numDirtyFiles--;
                cleanLock.notifyAll();
            }
        }

        return removed;
    }

    void fileWasCheckedInClean(TSFile tsf) {
    }

    void fileHasFailed(TSFile tsf, Throwable x) {
        if (tsf.isActive()) {
            LOGGER.error().append("Error storing ").append(tsf).append(". Aborting.").append(x).commit();
            boolean discardFile = false;

            synchronized (dirtyFiles) {
                int failuresPerFile = failuresByFile
                        .computeIfAbsent(tsf.getPathString(), tsFile -> new AtomicInteger(0)).incrementAndGet();
                if (failuresPerFile < ATTEMPTS_PER_FILE) {
                    // Try to process file again
                    if (shutdownInProgress) {
                        // Add it to "bad" file list: files in "bad" list have lower priority
                        badDirtyFiles.add(tsf);
                    } else {
                        // Add it into regular list to avoid a situation when high writer load
                        // effectively prevents re-processing the file at all
                        dirtyFiles.add(tsf);
                    }
                    dirtyFiles.notify();
                } else {
                    discardFile = true;
                }
            }

            if (discardFile) {
                // Note: we do not call fileProcessed(tsf) on that execution branch:
                // we let TB to consider it queued to avoid adding it to queue again.
                synchronized (cleanLock) {
                    numDirtyFiles--;
                    cleanLock.notifyAll();
                }

                LOGGER.error("Failed to store %s after %s attempts: %s").with(tsf.getPathString())
                        .with(ATTEMPTS_PER_FILE).with(x);
                ;
            }

            // TODO: We can make few additional attempts first
            int attemptsFailed = failedWriteAttempts.incrementAndGet();

            // We use "==" because the counter is atomic and will return
            // FAILURES_TO_TRIGGER_SHUTDOWN exactly once.
            if (attemptsFailed == FAILURES_TO_TRIGGER_SHUTDOWN || discardFile) {
                triggerEmergencyShutdown();
            }

        } else {
            LOGGER.error().append("Error storing ").append(tsf.toShortString()).append(".").append(x != null ? x : "")
                    .commit();
            fileProcessed(tsf);
        }
    }

    void fileWasStored(TSFile tsf) {
        //
        // Later optionally cache it... for now, drop.
        //
        // It is CRITICAL to first unuse the file, as the below notification
        // is usually the last step on the way to closing the root.
        //
        TreeOps.unuse(tsf);
        fileProcessed(tsf);
    }

    void fileWasDropped(TSFile tsf) {
        // do not call "unuse" here
        fileProcessed(tsf);
    }

    private void fileProcessed(TSFile tsf) {
        synchronized (dirtyFiles) {
            tsf.queued = false;
        }

        // change numDirtyFiles at the end of processing, to be able to wait until all
        // data stored
        synchronized (cleanLock) {
            numDirtyFiles--;
            cleanLock.notifyAll();
        }
    }

    void addToWriteQueue(TSFile tsf) {
        synchronized (this) {
            checkIsStarted();

            if (writers.size() == 0)
                throw new IllegalStateException("No active writers available");

            for (TSFWriterThread writer : writers) {
                if (writer.isInterrupted())
                    throw new IllegalStateException("Writer thread is interrupted");
            }

            if (isReadOnly)
                throw new IllegalStateException("This PDS is running in read-only mode");
        }

        maxFileSize = Math.max(maxFileSize, tsf.getUncompressedSize());

        synchronized (cleanLock) {
            numDirtyFiles++;
            cleanLock.notifyAll();
        }

        synchronized (dirtyFiles) {
            assert dirtyFiles.indexOf(tsf) < 0 : tsf + " is already queued";

            tsf.queued = true;

            dirtyFiles.add(tsf);
            dirtyFiles.notify();
        }

        if (shutdownInProgress && LOGGER.isInfoEnabled()) {
            LOGGER.info("Added file to be saved during shutdown: %s").with(tsf.getPathString());
        }
    }

    TSFile getTSFToWrite() throws InterruptedException {
        synchronized (dirtyFiles) {
            while (dirtyFiles.isEmpty()) {

                // We don't have "normal" file to process, so let's try a "bad" file
                if (!badDirtyFiles.isEmpty()) {
                    return badDirtyFiles.remove(0);
                }

                dirtyFiles.wait();
            }

            return dirtyFiles.remove(0);
        }
    }

    private void checkIsStarted() throws IllegalStateException {
        if (!isStarted)
            throw new IllegalStateException("Not started");
    }

    private void checkShutdown() throws IllegalStateException {
        if (shutdownInProgress)
            throw new IllegalStateException("Shutdown in progress");
    }
}
