package com.gt.sdk.base.videocache;

import com.gt.sdk.base.videocache.headers.HeaderInjector;
import com.gt.sdk.base.videocache.sourcestorage.SourceInfoStorage;

import java.io.ByteArrayInputStream;

/**
 * Simple memory based {@link Source} implementation.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class ByteArraySource implements Source {

    private final byte[] data;
    private ByteArrayInputStream arrayInputStream;

    public ByteArraySource(byte[] data) {
        this.data = data;
    }

    @Override
    public int read(byte[] buffer) throws ProxyCacheException {
        return arrayInputStream.read(buffer, 0, buffer.length);
    }

    @Override
    public long length() throws ProxyCacheException {
        return data.length;
    }

    @Override
    public void open(long offset) throws ProxyCacheException {
        arrayInputStream = new ByteArrayInputStream(data);
        arrayInputStream.skip(offset);
    }

    @Override
    public void close() throws ProxyCacheException {
    }

    @Override
    public String getMime() throws ProxyCacheException {
        return null;
    }

    @Override
    public SourceInfo getSourceInfo() {
        return null;
    }

    @Override
    public SourceInfoStorage getSourceInfoStorage() {
        return null;
    }

    @Override
    public HeaderInjector getHeaderInjector() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }
}

