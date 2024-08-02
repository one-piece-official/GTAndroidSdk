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

package com.sigmob.sdk.downloader.core.interceptor.connect;


import com.sigmob.sdk.downloader.FileDownload;
import com.sigmob.sdk.downloader.core.connection.DownloadConnection;
import com.sigmob.sdk.downloader.core.download.DownloadChain;
import com.sigmob.sdk.downloader.core.interceptor.Interceptor;

import java.io.IOException;

public class CallServerInterceptor implements Interceptor.Connect {

    @Override
    public DownloadConnection.Connected interceptConnect(DownloadChain chain) throws IOException {
        FileDownload.with().downloadStrategy().inspectNetworkOnWifi(chain.getTask());
        FileDownload.with().downloadStrategy().inspectNetworkAvailable();

        return chain.getConnectionOrCreate().execute();
    }
}