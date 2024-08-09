package com.gt.sdk.base.videocache;

import static com.gt.sdk.base.videocache.Preconditions.checkNotNull;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

import android.text.TextUtils;

import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.base.videocache.headers.EmptyHeadersInjector;
import com.gt.sdk.base.videocache.headers.HeaderInjector;
import com.gt.sdk.base.videocache.sourcestorage.SourceInfoStorage;
import com.gt.sdk.base.videocache.sourcestorage.SourceInfoStorageFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * {@link Source} that uses http resource as source for {@link ProxyCache}.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class HttpUrlSource implements Source {

    private static final int MAX_REDIRECTS = 5;
    private final SourceInfoStorage sourceInfoStorage;
    private final HeaderInjector headerInjector;
    private SourceInfo sourceInfo;
    private HttpURLConnection connection;
    private InputStream inputStream;

    public HttpUrlSource(String url) {
        this(url, SourceInfoStorageFactory.newEmptySourceInfoStorage());
    }

    public HttpUrlSource(String url, SourceInfoStorage sourceInfoStorage) {
        this(url, sourceInfoStorage, new EmptyHeadersInjector());
    }

    public HttpUrlSource(String url, SourceInfoStorage sourceInfoStorage, HeaderInjector headerInjector) {
        this.sourceInfoStorage = checkNotNull(sourceInfoStorage);
        this.headerInjector = checkNotNull(headerInjector);
        SourceInfo sourceInfo = sourceInfoStorage.get(url);
        this.sourceInfo = sourceInfo != null ? sourceInfo : new SourceInfo(url, Integer.MIN_VALUE, ProxyCacheUtils.getSupposablyMime(url));
    }

    public HttpUrlSource(Source source) {
        this.sourceInfo = source.getSourceInfo();
        this.sourceInfoStorage = source.getSourceInfoStorage();
        this.headerInjector = source.getHeaderInjector();
    }

    @Override
    public SourceInfo getSourceInfo() {
        return sourceInfo;
    }

    @Override
    public SourceInfoStorage getSourceInfoStorage() {
        return sourceInfoStorage;
    }

    @Override
    public HeaderInjector getHeaderInjector() {
        return headerInjector;
    }

    @Override
    public synchronized long length() throws ProxyCacheException {
        if (sourceInfo.length == Integer.MIN_VALUE) {
            fetchContentInfo();
        }
        return sourceInfo.length;
    }

    @Override
    public void open(long offset) throws ProxyCacheException {
        try {
            connection = openConnection(offset, 3 * 1000);
            String mime = connection.getContentType();
            inputStream = new BufferedInputStream(connection.getInputStream(), ProxyCacheUtils.DEFAULT_BUFFER_SIZE);
            long length = readSourceAvailableBytes(connection, offset, connection.getResponseCode());
            this.sourceInfo = new SourceInfo(sourceInfo.url, length, mime);
            this.sourceInfoStorage.put(sourceInfo.url, sourceInfo);
        } catch (IOException e) {
            throw new ProxyCacheException("Error opening connection for " + sourceInfo.url + " with offset " + offset, e);
        }
    }

    private long readSourceAvailableBytes(HttpURLConnection connection, long offset, int responseCode) throws IOException {
        long contentLength = getContentLength(connection);
        return responseCode == HTTP_OK ? contentLength : responseCode == HTTP_PARTIAL ? contentLength + offset : sourceInfo.length;
    }

    private long getContentLength(HttpURLConnection connection) {
        String contentLengthValue = connection.getHeaderField("Content-Length");
        return contentLengthValue == null ? -1 : Long.parseLong(contentLengthValue);
    }

    @Override
    public void close() throws ProxyCacheException {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (NullPointerException | IllegalArgumentException e) {
                String message = "Wait... but why? WTF!? ";
                throw new RuntimeException(message, e);
            } catch (ArrayIndexOutOfBoundsException e) {
                SigmobLog.e("Error closing connection correctly. Should happen only on SigmobAndroid L. " + "Until good solution is not know, just ignore this issue :(", e);
            }
        }
    }

    @Override
    public int read(byte[] buffer) throws ProxyCacheException {
        if (inputStream == null) {
            throw new ProxyCacheException("Error reading data from " + sourceInfo.url + ": connection is absent!");
        }
        try {
            return inputStream.read(buffer, 0, buffer.length);
        } catch (InterruptedIOException e) {
            throw new InterruptedProxyCacheException("Reading source " + sourceInfo.url + " is interrupted", e);
        } catch (IOException e) {
            throw new ProxyCacheException("Error reading data from " + sourceInfo.url, e);
        }
    }

    private void fetchContentInfo() throws ProxyCacheException {
        SigmobLog.d("Read content info from " + sourceInfo.url);
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = openConnection(0, 3000);
            long length = getContentLength(urlConnection);
            String mime = urlConnection.getContentType();
            this.sourceInfo = new SourceInfo(sourceInfo.url, length, mime);
            this.sourceInfoStorage.put(sourceInfo.url, sourceInfo);
            SigmobLog.d("Source info fetched: " + sourceInfo);
        } catch (IOException e) {
            SigmobLog.e("Error fetching info from " + sourceInfo.url, e);
        } finally {
//            ProxyCacheUtils.close(inputStream);
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
        }
    }

    private HttpURLConnection openConnection(long offset, int timeout) throws IOException, ProxyCacheException {
        boolean redirected;
        int redirectCount = 0;
        String url = this.sourceInfo.url;


        if (connection != null && offset > 0) {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Throwable t) {
                }
                connection = null;
                ProxyCacheUtils.close(inputStream);
            }

        }


        if (connection == null) {
            do {
                SigmobLog.d("Open connection " + (offset > 0 ? " with offset " + offset : "") + " to " + url);
                connection = (HttpURLConnection) new URL(url).openConnection();
                injectCustomHeaders(connection, url);
                if (offset > 0) {
                    connection.setRequestProperty("Range", "bytes=" + offset + "-");
                }
                if (timeout > 0) {
                    connection.setConnectTimeout(timeout);
                    connection.setReadTimeout(timeout);
                }
                int code = connection.getResponseCode();
                redirected = code == HTTP_MOVED_PERM || code == HTTP_MOVED_TEMP || code == HTTP_SEE_OTHER;
                if (redirected) {
                    String redirectURL = connection.getHeaderField("Location");
                    // 解析location，根据location是绝对还是相对地址处理
                    URI locationUri = URI.create(redirectURL);
                    if (locationUri != null && !locationUri.isAbsolute()) {
                        // 如果location是相对URL，将其解析为绝对URL
                        try {
                            URI orginalUri = URI.create(url);
                            if (orginalUri != null) {
                                locationUri = orginalUri.resolve(locationUri);
                                redirectURL = locationUri.toString();
                            }
                        } catch (Throwable e) {

                        }
                    }
                    url = redirectURL;
                    redirectCount++;
                    connection.disconnect();
                }
                if (redirectCount > MAX_REDIRECTS) {
                    throw new ProxyCacheException("Too many redirects: " + redirectCount);
                }
            } while (redirected);
        }

        return connection;
    }

    private void injectCustomHeaders(HttpURLConnection connection, String url) {
        Map<String, String> extraHeaders = headerInjector.addHeaders(url);
        for (Map.Entry<String, String> header : extraHeaders.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }
    }

    public synchronized String getMime() throws ProxyCacheException {
        if (TextUtils.isEmpty(sourceInfo.mime)) {
            fetchContentInfo();
        }
        return sourceInfo.mime;
    }

    @Override
    public String getUrl() {
        return sourceInfo.url;
    }

    @Override
    public String toString() {
        return "HttpUrlSource{sourceInfo='" + sourceInfo + "}";
    }
}
