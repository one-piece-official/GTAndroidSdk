package com.sigmob.sdk.videocache;

import static com.sigmob.sdk.videocache.Preconditions.checkNotNull;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

import android.text.TextUtils;

import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.videocache.headers.EmptyHeadersInjector;
import com.sigmob.sdk.videocache.headers.HeaderInjector;
import com.sigmob.sdk.videocache.sourcestorage.SourceInfoStorage;
import com.sigmob.sdk.videocache.sourcestorage.SourceInfoStorageFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * {@link Source} that uses http resource as source for {@link ProxyCache}.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class OkHttp3UrlSource implements Source {
    private static final int MAX_REDIRECTS = 5;
    //
    private static OkHttpClient client;

    private InputStream inputStream;
    private SourceInfo sourceInfo;

    protected volatile long length = Integer.MIN_VALUE;
    protected volatile String mime = "video/mp4";
    private final SourceInfoStorage sourceInfoStorage;
    private final HeaderInjector headerInjector;
    private Response response;

    public OkHttp3UrlSource(String url) {
        this(url, SourceInfoStorageFactory.newEmptySourceInfoStorage());
    }

    public OkHttp3UrlSource(String url, SourceInfoStorage sourceInfoStorage) {
        this(url, sourceInfoStorage, new EmptyHeadersInjector());
    }

    public OkHttp3UrlSource(String url, SourceInfoStorage sourceInfoStorage, HeaderInjector headerInjector) {
        this.sourceInfoStorage = checkNotNull(sourceInfoStorage);
        this.headerInjector = checkNotNull(headerInjector);
        SourceInfo sourceInfo = sourceInfoStorage.get(url);
        this.sourceInfo = sourceInfo != null ? sourceInfo :
                new SourceInfo(url, Integer.MIN_VALUE, ProxyCacheUtils.getSupposablyMime(url));
    }


    public OkHttp3UrlSource(Source source) {
        this.sourceInfo = source.getSourceInfo();
        this.sourceInfoStorage = source.getSourceInfoStorage();
        this.headerInjector = source.getHeaderInjector();
    }

    @Override
    public long length() throws ProxyCacheException {
        if (length == Integer.MIN_VALUE) {
            fetchContentInfo();
        }
        return length;
    }

    public synchronized String getMime() throws ProxyCacheException {
        if (TextUtils.isEmpty(sourceInfo.mime)) {
            fetchContentInfo();
        }
        return sourceInfo.mime;
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
    public void open(long offset) throws ProxyCacheException {

        try {
            response = openConnection(offset, -1);
            ResponseBody body = response.body();

            if (body != null && body.contentType() != null) {
                mime = body.contentType().toString();
            }

            length = readSourceAvailableBytes(response, offset);
            inputStream = new BufferedInputStream(body.byteStream(), ProxyCacheUtils.DEFAULT_BUFFER_SIZE);

            this.sourceInfo = new SourceInfo(sourceInfo.url, length, mime);
            this.sourceInfoStorage.put(sourceInfo.url, sourceInfo);

        } catch (IOException e) {
            throw new ProxyCacheException("Error opening connection for " + sourceInfo.url + " with offset " + offset, e);
        }
    }

    private long readSourceAvailableBytes(Response response, long offset) throws IOException {
        int responseCode = response.code();
        int contentLength = (int) response.body().contentLength();
        return responseCode == HTTP_OK ? contentLength
                : responseCode == HTTP_PARTIAL ? contentLength + offset : length;
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

    @Override
    public void close() throws ProxyCacheException {
        try {
            ProxyCacheUtils.close(inputStream);

        } catch (NullPointerException | IllegalArgumentException e) {
            String message = "Wait... but why? WTF!? " +
                    "If you read it on your device log, please";
            throw new RuntimeException(message, e);
        } catch (ArrayIndexOutOfBoundsException e) {
            SigmobLog.e("Error closing connection correctly. Should happen only on SigmobAndroid L. " +
                    "Until good solution is not know, just ignore this issue :(", e);
        }
    }

    private void fetchContentInfo() throws ProxyCacheException {
        Response response = null;
        try {
            SigmobLog.d("Read content info from " + sourceInfo.url);

            response = openConnection(0, 10000);
            if (response == null || !response.isSuccessful()) {
                throw new ProxyCacheException("Fail to fetchContentInfo: " + this.sourceInfo.url);
            }
            ResponseBody body = response.body();
            if (body != null) {
                length = (int) body.contentLength();
                if (body.contentType() != null) {
                    mime = body.contentType().toString();
                }
            }

            this.sourceInfo = new SourceInfo(sourceInfo.url, length, mime);
            this.sourceInfoStorage.put(sourceInfo.url, sourceInfo);
        } catch (IOException e) {
            SigmobLog.e("Error fetching info from " + sourceInfo.url, e);
        } finally {
//            if (response != null) {
//                response.close();
//            }
//            ProxyCacheUtils.close(inputStream);
        }
    }

    private void injectCustomHeaders(Request.Builder requestBuilder, String url) {
        Map<String, String> extraHeaders = headerInjector.addHeaders(url);
        for (Map.Entry<String, String> header : extraHeaders.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }
    }

    private Response openConnection(long offset, int timeout) throws IOException, ProxyCacheException {

        if (client == null) {
            client = new OkHttpClient().newBuilder().connectionPool(new ConnectionPool())
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000, TimeUnit.MILLISECONDS)
                    .writeTimeout(10000, TimeUnit.MILLISECONDS).build();
        }
        boolean isRedirect = false;
        String url = this.sourceInfo.url;
        int redirectCount = 0;

        if (response != null && offset > 0) {
            try {
                response.close();
            } catch (Throwable t) {

            }
            response = null;
            ProxyCacheUtils.close(inputStream);
        }

        if (response == null) {

            do {
                Request.Builder requestBuilder = new Request.Builder();
                requestBuilder.get();
                requestBuilder.url(url);
                injectCustomHeaders(requestBuilder, url);

                if (offset > 0) {
                    requestBuilder.addHeader("Range", "bytes=" + offset + "-");
                }

                response = client.newCall(requestBuilder.build()).execute();
                if (response.isRedirect()) {
                    url = response.header("Location");
                    isRedirect = response.isRedirect();
                    redirectCount++;
                }
                if (redirectCount > MAX_REDIRECTS) {
                    throw new ProxyCacheException("Too many redirects: " + redirectCount);
                }
            } while (isRedirect);

        }
        return response;
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
