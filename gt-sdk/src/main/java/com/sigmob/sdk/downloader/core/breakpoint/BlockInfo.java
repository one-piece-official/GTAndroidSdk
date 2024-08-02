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

package com.sigmob.sdk.downloader.core.breakpoint;

import static com.sigmob.sdk.downloader.core.Util.CHUNKED_CONTENT_LENGTH;

import java.util.concurrent.atomic.AtomicLong;

public class BlockInfo {

    private final long startOffset;

    private final long contentLength;
    private final AtomicLong currentOffset;

    public BlockInfo(long startOffset, long contentLength) {
        this(startOffset, contentLength, 0);
    }

    public BlockInfo(long startOffset, long contentLength, long currentOffset) {
        if (startOffset < 0 || (contentLength < 0 && contentLength != CHUNKED_CONTENT_LENGTH)
                || currentOffset < 0) {
            throw new IllegalArgumentException();
        }

        this.startOffset = startOffset;
        this.contentLength = contentLength;
        this.currentOffset = new AtomicLong(currentOffset);
    }

    public long getCurrentOffset() {
        return this.currentOffset.get();
    }

    public long getStartOffset() {
        return startOffset;
    }

    public long getRangeLeft() {
        return startOffset + currentOffset.get();
    }

    public long getContentLength() {
        return contentLength;
    }

    public long getRangeRight() {
        if (contentLength == CHUNKED_CONTENT_LENGTH) {
            return CHUNKED_CONTENT_LENGTH;
        }
        return startOffset + contentLength - 1;
    }

    public void increaseCurrentOffset(long increaseLength) {
        this.currentOffset.addAndGet(increaseLength);
    }

    public void resetBlock() {
        this.currentOffset.set(0);
    }

    public BlockInfo copy() {
        return new BlockInfo(startOffset, contentLength, currentOffset.get());
    }

    @Override
    public String toString() {
        return "[" + startOffset + ", " + getRangeRight() + ")" + "-current:" + currentOffset;
    }
}
