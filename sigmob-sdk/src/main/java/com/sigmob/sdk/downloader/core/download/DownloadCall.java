/*
 * Copyright (c) 2017 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sigmob.sdk.downloader.core.download;

import android.os.SystemClock;

import com.sigmob.sdk.downloader.DownloadTask;
import com.sigmob.sdk.downloader.FileDownload;
import com.sigmob.sdk.downloader.core.NamedRunnable;
import com.sigmob.sdk.downloader.core.Util;
import com.sigmob.sdk.downloader.core.breakpoint.BlockInfo;
import com.sigmob.sdk.downloader.core.breakpoint.BreakpointInfo;
import com.sigmob.sdk.downloader.core.breakpoint.DownloadStore;
import com.sigmob.sdk.downloader.core.cause.EndCause;
import com.sigmob.sdk.downloader.core.cause.ResumeFailedCause;
import com.sigmob.sdk.downloader.core.file.MultiPointOutputStream;
import com.sigmob.sdk.downloader.core.file.ProcessFileStrategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class DownloadCall extends NamedRunnable implements Comparable<DownloadCall> {
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
            Util.threadFactory("FileDownload Block", false));

    private static final String TAG = "DownloadCall";

    static final int MAX_COUNT_RETRY_FOR_PRECONDITION_FAILED = 3;
    public final DownloadTask task;
    public final boolean asyncExecuted;

    final ArrayList<DownloadChain> blockChainList;

    volatile DownloadCache cache;
    volatile boolean canceled;
    volatile boolean finishing;

    volatile Thread currentThread;

    private final DownloadStore store;

    private DownloadCall(DownloadTask task, boolean asyncExecuted, DownloadStore store) {
        this(task, asyncExecuted, new ArrayList<DownloadChain>(), store);
    }

    DownloadCall(DownloadTask task, boolean asyncExecuted,
                 ArrayList<DownloadChain> runningBlockList,
                 DownloadStore store) {
        super("download call: " + task.getId());
        this.task = task;
        this.asyncExecuted = asyncExecuted;
        this.blockChainList = runningBlockList;
        this.store = store;
    }

    public static DownloadCall create(DownloadTask task, boolean asyncExecuted,
                                      DownloadStore store) {
        return new DownloadCall(task, asyncExecuted, store);
    }

    public boolean cancel() {
        synchronized (this) {
            if (canceled) return true;
            if (finishing) return false;
            this.canceled = true;
        }

        final long startCancelTime = SystemClock.uptimeMillis();

        FileDownload.with().downloadDispatcher().flyingCanceled(this);

        final DownloadCache cache = this.cache;
        if (cache != null) cache.setUserCanceled();

        // ArrayList#clone is not a thread safe operation,
        // so chains#size may > chains#elementData.length and this will cause
        // ConcurrentModificationException during iterate the ArrayList(ArrayList#next).
        // This is a reproduce example:
        // https://repl.it/talk/share/ConcurrentModificationException/18566.
        // So don't use clone anymore.
        final Object[] chains = blockChainList.toArray();
        if (chains == null || chains.length == 0) {
            if (currentThread != null) {
                Util.d(TAG,
                        "interrupt thread with cancel operation because of chains are not running "
                                + task.getId());
                currentThread.interrupt();
            }
        } else {
            for (Object chain : chains) {
                if (chain instanceof DownloadChain) {
                    ((DownloadChain) chain).cancel();
                }
            }
        }

        if (cache != null) cache.getOutputStream().cancelAsync();

        Util.d(TAG, "cancel task " + task.getId() + " consume: " + (SystemClock
                .uptimeMillis() - startCancelTime) + "ms");
        return true;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean isFinishing() {
        return finishing;
    }

    @Override
    public void execute() throws InterruptedException {
        currentThread = Thread.currentThread();

        boolean retry;
        int retryCount = 0;

        // ready param
        final FileDownload fileDownload = FileDownload.with();
        final ProcessFileStrategy fileStrategy = fileDownload.processFileStrategy();

        // inspect task start
        inspectTaskStart();
        do {
            // 0. check basic param before start
            if (null == task.getUrl() || task.getUrl().length() <= 0) {
                this.cache = new DownloadCache.PreError(
                        new IOException("unexpected url: " + task.getUrl()));
                break;
            }

            if (canceled) break;

            // 1. create basic info if not exist
            final BreakpointInfo info;
            try {
                BreakpointInfo infoOnStore = store.get(task.getId());
                if (infoOnStore == null) {
                    info = store.createAndInsert(task);
                } else {
                    info = infoOnStore;
                }
                setInfoToTask(info);
            } catch (IOException e) {
                this.cache = new DownloadCache.PreError(e);
                break;
            }
            if (canceled) break;

            // ready cache.
            final DownloadCache cache = createCache(info);
            this.cache = cache;

            // 2. remote check.
            final BreakpointRemoteCheck remoteCheck = createRemoteCheck(info);
            try {
                remoteCheck.check();
            } catch (IOException e) {
                cache.catchException(e);
                break;
            }
            cache.setRedirectLocation(task.getRedirectLocation());

            // 3. waiting for file lock release after file path is confirmed.
            fileStrategy.getFileLock().waitForRelease(task.getTempFile().getAbsolutePath());

            // 4. reuse another info if another info is idle and available for reuse.
            FileDownload.with().downloadStrategy()
                    .inspectAnotherSameInfo(task, info, remoteCheck.getInstanceLength());

            try {
                if (remoteCheck.isResumable()) {
                    // 5. local check
                    final BreakpointLocalCheck localCheck = createLocalCheck(info,
                            remoteCheck.getInstanceLength());
                    localCheck.check();
                    if (localCheck.isDirty()) {
                        Util.d(TAG, "breakpoint invalid: download from beginning because of "
                                + "local check is dirty " + task.getId() + " " + localCheck);
                        // 6. assemble block data
                        fileStrategy.discardProcess(task);
                        assembleBlockAndCallbackFromBeginning(info, remoteCheck,
                                localCheck.getCauseOrThrow());
                    } else {
                        fileDownload.callbackDispatcher().dispatch()
                                .downloadFromBreakpoint(task, info);
                    }
                } else {
                    Util.d(TAG, "breakpoint invalid: download from beginning because of "
                            + "remote check not resumable " + task.getId() + " " + remoteCheck);
                    // 6. assemble block data
                    fileStrategy.discardProcess(task);
                    assembleBlockAndCallbackFromBeginning(info, remoteCheck,
                            remoteCheck.getCauseOrThrow());
                }
            } catch (IOException e) {
                cache.setUnknownError(e);
                break;
            }

            // 7. start with cache and info.
            start(cache, info);

            if (canceled) break;

            // 8. retry if precondition failed.
            if (cache.isPreconditionFailed()
                    && retryCount++ < MAX_COUNT_RETRY_FOR_PRECONDITION_FAILED) {
                store.remove(task.getId());
                retry = true;
            } else {
                retry = false;
            }
        } while (retry);

        // finish
        finishing = true;
        blockChainList.clear();

        final DownloadCache cache = this.cache;
        if (canceled || cache == null) return;

        final EndCause cause;
        Exception realCause = null;
        if (cache.isServerCanceled() || cache.isUnknownError()
                || cache.isPreconditionFailed()) {
            // error
            cause = EndCause.ERROR;
            realCause = cache.getRealCause();
        } else if (cache.isFileBusyAfterRun()) {
            cause = EndCause.FILE_BUSY;
        } else if (cache.isPreAllocateFailed()) {
            cause = EndCause.PRE_ALLOCATE_FAILED;
            realCause = cache.getRealCause();
        } else {
            cause = EndCause.COMPLETED;
        }
        inspectTaskEnd(cache, cause, realCause);
    }

    private void inspectTaskStart() {
        store.onTaskStart(task.getId());
        FileDownload.with().callbackDispatcher().dispatch().taskStart(task);
    }

    private void inspectTaskEnd(DownloadCache cache, EndCause cause,
                                Exception realCause) {
        // non-cancel handled on here
        if (cause == EndCause.CANCELED) {
            throw new IllegalAccessError("can't recognize cancelled on here");
        }

        synchronized (this) {
            if (canceled) return;
            finishing = true;
        }

        store.onTaskEnd(task.getId(), cause, realCause);
        if (cause == EndCause.COMPLETED) {
            store.markFileClear(task.getId());
            FileDownload.with().processFileStrategy()
                    .completeProcessStream(cache.getOutputStream(), task);


        }

        FileDownload.with().callbackDispatcher().dispatch().taskEnd(task, cause, realCause);
    }

    // this method is convenient for unit-test.
    DownloadCache createCache(BreakpointInfo info) {
        final MultiPointOutputStream outputStream = FileDownload.with().processFileStrategy()
                .createProcessStream(task, info, store);
        return new DownloadCache(outputStream);
    }

    // this method is convenient for unit-test.
    int getPriority() {
        return task.getPriority();
    }

    void start(final DownloadCache cache, BreakpointInfo info) throws InterruptedException {
        final int blockCount = info.getBlockCount();
        final List<DownloadChain> blockChainList = new ArrayList<>(info.getBlockCount());
        final List<Integer> blockIndexList = new ArrayList<>();
        for (int i = 0; i < blockCount; i++) {
            final BlockInfo blockInfo = info.getBlock(i);
            if (Util.isCorrectFull(blockInfo.getCurrentOffset(), blockInfo.getContentLength())) {
                continue;
            }

            Util.resetBlockIfDirty(blockInfo);
            final DownloadChain chain = DownloadChain.createChain(i, task, info, cache, store);
            blockChainList.add(chain);
            blockIndexList.add(chain.getBlockIndex());
        }

        if (canceled) {
            return;
        }

        cache.getOutputStream().setRequireStreamBlocks(blockIndexList);

        startBlocks(blockChainList);
    }

    @Override
    protected void interrupted(InterruptedException e) {
    }

    @Override
    protected void finished() {
        FileDownload.with().downloadDispatcher().finish(this);
        Util.d(TAG, "call is finished " + task.getId());
    }

    void startBlocks(List<DownloadChain> tasks) throws InterruptedException {
        ArrayList<Future> futures = new ArrayList<>(tasks.size());
        try {
            for (DownloadChain chain : tasks) {
                futures.add(submitChain(chain));
            }

            blockChainList.addAll(tasks);

            for (Future future : futures) {
                if (!future.isDone()) {
                    try {
                        future.get();
                    } catch (CancellationException | ExecutionException ignore) {
                    }
                }
            }
        } catch (Throwable t) {
            for (Future future : futures) {
                future.cancel(true);
            }
            throw t;
        } finally {
            blockChainList.removeAll(tasks);
        }
    }

    // convenient for unit-test

    BreakpointLocalCheck createLocalCheck(BreakpointInfo info,
                                          long responseInstanceLength) {
        return new BreakpointLocalCheck(task, info, responseInstanceLength);
    }

    // convenient for unit-test

    BreakpointRemoteCheck createRemoteCheck(BreakpointInfo info) {
        return new BreakpointRemoteCheck(task, info);
    }

    // convenient for unit-test
    void setInfoToTask(BreakpointInfo info) {
        DownloadTask.TaskHideWrapper.setBreakpointInfo(task, info);
    }

    void assembleBlockAndCallbackFromBeginning(BreakpointInfo info,
                                               BreakpointRemoteCheck remoteCheck,
                                               ResumeFailedCause failedCause) {
        Util.assembleBlock(task, info, remoteCheck.getInstanceLength(),
                remoteCheck.isAcceptRange());
        FileDownload.with().callbackDispatcher().dispatch()
                .downloadFromBeginning(task, info, failedCause);
    }

    Future<?> submitChain(DownloadChain chain) {
        return EXECUTOR.submit(chain);
    }

    public boolean equalsTask(DownloadTask task) {
        return this.task.equals(task);
    }

    public File getFile() {
        return this.task.getTempFile();
    }

    //    //@SuppressFBWarnings(value = "Eq", justification = "This special case is just for task priority")
    @Override
    public int compareTo(DownloadCall o) {
        return o.getPriority() - getPriority();
    }
}
