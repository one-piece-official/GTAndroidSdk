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

package com.sigmob.sdk.downloader.core.file;


import com.sigmob.sdk.downloader.DownloadTask;
import com.sigmob.sdk.downloader.FileDownload;
import com.sigmob.sdk.downloader.core.breakpoint.BreakpointInfo;
import com.sigmob.sdk.downloader.core.breakpoint.DownloadStore;

import java.io.File;
import java.io.IOException;

public class ProcessFileStrategy {
    private final FileLock fileLock = new FileLock();


    public MultiPointOutputStream createProcessStream(DownloadTask task,
                                                      BreakpointInfo info,
                                                      DownloadStore store) {
        return new MultiPointOutputStream(task, info, store);
    }

    public void completeProcessStream(MultiPointOutputStream processOutputStream,
                                      DownloadTask task) {
    }

    public void discardProcess(DownloadTask task) throws IOException {
        // Remove target file.
        final File file = task.getTempFile();
        // Do nothing, because the filename hasn't found yet.
        if (file == null) return;

        if (file.exists() && !file.delete()) {
            throw new IOException("Delete file failed!");
        }
    }


    public FileLock getFileLock() {
        return fileLock;
    }

    public boolean isPreAllocateLength(DownloadTask task) {
        // if support seek, enable pre-allocate length.
        boolean supportSeek = FileDownload.with().outputStreamFactory().supportSeek();
        if (!supportSeek) return false;

        if (task.getSetPreAllocateLength() != null) return task.getSetPreAllocateLength();
        return true;
    }
}
