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

package com.sigmob.sdk.downloader;

import android.annotation.SuppressLint;
import android.content.Context;

import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.downloader.core.Util;
import com.sigmob.sdk.downloader.core.breakpoint.BreakpointStore;
import com.sigmob.sdk.downloader.core.breakpoint.DownloadStore;
import com.sigmob.sdk.downloader.core.connection.DownloadConnection;
import com.sigmob.sdk.downloader.core.dispatcher.CallbackDispatcher;
import com.sigmob.sdk.downloader.core.dispatcher.DownloadDispatcher;
import com.sigmob.sdk.downloader.core.download.DownloadStrategy;
import com.sigmob.sdk.downloader.core.file.DownloadOutputStream;
import com.sigmob.sdk.downloader.core.file.DownloadUriOutputStream;
import com.sigmob.sdk.downloader.core.file.ProcessFileStrategy;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class FileDownload {

    @SuppressLint("StaticFieldLeak")
    static volatile FileDownload singleton;

    private final DownloadDispatcher downloadDispatcher;
    private final CallbackDispatcher callbackDispatcher;
    private final BreakpointStore breakpointStore;
    private final DownloadConnection.Factory connectionFactory;
    private final DownloadOutputStream.Factory outputStreamFactory;
    private final ProcessFileStrategy processFileStrategy;
    private final DownloadStrategy downloadStrategy;

    private final Context context;

    DownloadMonitor monitor;

    FileDownload(Context context, DownloadDispatcher downloadDispatcher,
                 CallbackDispatcher callbackDispatcher, DownloadStore store,
                 DownloadConnection.Factory connectionFactory,
                 DownloadOutputStream.Factory outputStreamFactory,
                 ProcessFileStrategy processFileStrategy, DownloadStrategy downloadStrategy) {
        this.context = context;
        this.downloadDispatcher = downloadDispatcher;
        this.callbackDispatcher = callbackDispatcher;
        this.breakpointStore = store;
        this.connectionFactory = connectionFactory;
        this.outputStreamFactory = outputStreamFactory;
        this.processFileStrategy = processFileStrategy;
        this.downloadStrategy = downloadStrategy;

        this.downloadDispatcher.setDownloadStore(Util.createRemitDatabase(store));
    }

    public DownloadDispatcher downloadDispatcher() {
        return downloadDispatcher;
    }

    public CallbackDispatcher callbackDispatcher() {
        return callbackDispatcher;
    }

    public BreakpointStore breakpointStore() {
        return breakpointStore;
    }

    public DownloadConnection.Factory connectionFactory() {
        return connectionFactory;
    }

    public DownloadOutputStream.Factory outputStreamFactory() {
        return outputStreamFactory;
    }

    public ProcessFileStrategy processFileStrategy() {
        return processFileStrategy;
    }

    public DownloadStrategy downloadStrategy() {
        return downloadStrategy;
    }

    public Context context() {
        return this.context;
    }

    public void setMonitor(DownloadMonitor monitor) {
        this.monitor = monitor;
    }

    public DownloadMonitor getMonitor() {
        return monitor;
    }

    public static FileDownload with() {
        if (singleton == null) {
            synchronized (FileDownload.class) {
                if (singleton == null) {
                    if (SDKContext.getApplicationContext() == null) {
                        throw new IllegalStateException("context == null");
                    }
                    singleton = new Builder(SDKContext.getApplicationContext()).build();
                }
            }
        }
        return singleton;
    }

    public static void setSingletonInstance(FileDownload fileDownload) {
        if (singleton != null) {
            throw new IllegalArgumentException(("FileDownload must be null."));
        }

        synchronized (FileDownload.class) {
            if (singleton != null) {
                throw new IllegalArgumentException(("FileDownload must be null."));
            }
            singleton = fileDownload;
        }
    }

    public static class Builder {
        private DownloadDispatcher downloadDispatcher;
        private CallbackDispatcher callbackDispatcher;
        private DownloadStore downloadStore;
        private DownloadConnection.Factory connectionFactory;
        private ProcessFileStrategy processFileStrategy;
        private DownloadStrategy downloadStrategy;
        private DownloadOutputStream.Factory outputStreamFactory;
        private DownloadMonitor monitor;
        private final Context context;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder downloadDispatcher(DownloadDispatcher downloadDispatcher) {
            this.downloadDispatcher = downloadDispatcher;
            return this;
        }

        public Builder callbackDispatcher(CallbackDispatcher callbackDispatcher) {
            this.callbackDispatcher = callbackDispatcher;
            return this;
        }

        public Builder downloadStore(DownloadStore downloadStore) {
            this.downloadStore = downloadStore;
            return this;
        }

        public Builder connectionFactory(DownloadConnection.Factory connectionFactory) {
            this.connectionFactory = connectionFactory;
            return this;
        }

        public Builder outputStreamFactory(DownloadOutputStream.Factory outputStreamFactory) {
            this.outputStreamFactory = outputStreamFactory;
            return this;
        }

        public Builder processFileStrategy(ProcessFileStrategy processFileStrategy) {
            this.processFileStrategy = processFileStrategy;
            return this;
        }

        public Builder downloadStrategy(DownloadStrategy downloadStrategy) {
            this.downloadStrategy = downloadStrategy;
            return this;
        }

        public Builder monitor(DownloadMonitor monitor) {
            this.monitor = monitor;
            return this;
        }

        public FileDownload build() {
            if (downloadDispatcher == null) {
                downloadDispatcher = new DownloadDispatcher();
            }

            if (callbackDispatcher == null) {
                callbackDispatcher = new CallbackDispatcher();
            }

            if (downloadStore == null) {
                downloadStore = Util.createDefaultDatabase(context);
            }

            if (connectionFactory == null) {
                connectionFactory = Util.createDefaultConnectionFactory();
            }

            if (outputStreamFactory == null) {
                outputStreamFactory = new DownloadUriOutputStream.Factory();
            }

            if (processFileStrategy == null) {
                processFileStrategy = new ProcessFileStrategy();
            }

            if (downloadStrategy == null) {
                downloadStrategy = new DownloadStrategy();
            }

            FileDownload fileDownload = new FileDownload(context, downloadDispatcher, callbackDispatcher,
                    downloadStore, connectionFactory, outputStreamFactory, processFileStrategy,
                    downloadStrategy);

            fileDownload.setMonitor(monitor);

            Util.d("FileDownload", "downloadStore[" + downloadStore + "] connectionFactory["
                    + connectionFactory);
            return fileDownload;
        }
    }
}
