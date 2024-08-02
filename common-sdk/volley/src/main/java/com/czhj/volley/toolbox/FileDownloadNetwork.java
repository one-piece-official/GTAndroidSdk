package com.czhj.volley.toolbox;

import android.os.SystemClock;

import com.czhj.volley.NoConnectionError;
import com.czhj.volley.ServerError;
import com.czhj.volley.TimeoutError;
import com.czhj.volley.AuthFailureError;
import com.czhj.volley.Cache;
import com.czhj.volley.Header;
import com.czhj.volley.Network;
import com.czhj.volley.NetworkError;
import com.czhj.volley.NetworkResponse;
import com.czhj.volley.Request;
import com.czhj.volley.RetryPolicy;
import com.czhj.volley.VolleyError;
import com.czhj.volley.VolleyLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class FileDownloadNetwork implements Network {

    protected static final boolean DEBUG = VolleyLog.DEBUG;

    private static final int SLOW_REQUEST_THRESHOLD_MS = 3000;

    private static final int DEFAULT_POOL_SIZE = 4096;
    protected final ByteArrayPool mPool;
    /**
     * @deprecated Should never have been exposed in the API. This field may be removed in a future
     * release of Volley.
     */

    private final BaseHttpStack mBaseHttpStack;


    /**
     * @param httpStack HTTP stack to be used
     */
    public FileDownloadNetwork(BaseHttpStack httpStack) {
        // If a pool isn't passed in, then build a small default pool that will give us a lot of
        // benefit and not use too much memory.
        this(httpStack, new ByteArrayPool(DEFAULT_POOL_SIZE));
    }

    /**
     * @param httpStack HTTP stack to be used
     * @param pool      a buffer pool that improves GC performance in copy operations
     */
    public FileDownloadNetwork(BaseHttpStack httpStack, ByteArrayPool pool) {
        mBaseHttpStack = httpStack;
        mPool = pool;
    }

    /**
     * Attempts to prepare the request for a retry. If there are no more attempts remaining in the
     * request's retry policy, a timeout exception is thrown.
     *
     * @param request The request to use.
     */
    public static void attemptRetryOnException(
            String logPrefix, Request<?> request, VolleyError exception) throws VolleyError {
        RetryPolicy retryPolicy = request.getRetryPolicy();
        int oldTimeout = request.getTimeoutMs();

        try {
            retryPolicy.retry(exception);
        } catch (VolleyError e) {
            request.addMarker(
                    String.format("%s-timeout-giveup [RetryCount %s] [timeout=%s]", logPrefix,retryPolicy.getCurrentRetryCount(), oldTimeout));
            throw e;
        }
        request.addMarker(String.format("%s-retry [RetryCount %s]  [timeout=%s]", logPrefix,retryPolicy.getCurrentRetryCount(), oldTimeout));
    }

    /**
     * Converts Headers[] to Map&lt;String, String&gt;.
     *
     * @deprecated Should never have been exposed in the API. This method may be removed in a future
     * release of Volley.
     */

    public static Map<String, String> convertHeaders(List<Header> headers) {
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < headers.size(); i++) {
            Header header = headers.get(i);
            result.put(header.getName(), header.getValue());
        }
        return result;
    }

    /**
     * Combine cache headers with network response headers for an HTTP 304 response.
     * <p>
     * <p>An HTTP 304 response does not have all header fields. We have to use the header fields
     * from the cache entry plus the new ones from the response. See also:
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5
     *
     * @param responseHeaders Headers from the network response.
     * @param entry           The cached response.
     * @return The combined list of headers.
     */
    private static List<Header> combineHeaders(List<Header> responseHeaders, Cache.Entry entry) {
        // First, create a case-insensitive set of header names from the network
        // response.
        Set<String> headerNamesFromNetworkResponse = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (!responseHeaders.isEmpty()) {
            for (Header header : responseHeaders) {
                headerNamesFromNetworkResponse.add(header.getName());
            }
        }

        // Second, add headers from the cache entry to the network response as long as
        // they didn't appear in the network response, which should take precedence.
        List<Header> combinedHeaders = new ArrayList<>(responseHeaders);
        if (entry.allResponseHeaders != null) {
            if (!entry.allResponseHeaders.isEmpty()) {
                for (Header header : entry.allResponseHeaders) {
                    if (!headerNamesFromNetworkResponse.contains(header.getName())) {
                        combinedHeaders.add(header);
                    }
                }
            }
        } else {
            // Legacy caches only have entry.responseHeaders.
            if (!entry.responseHeaders.isEmpty()) {
                for (Map.Entry<String, String> header : entry.responseHeaders.entrySet()) {
                    if (!headerNamesFromNetworkResponse.contains(header.getKey())) {
                        combinedHeaders.add(new Header(header.getKey(), header.getValue()));
                    }
                }
            }
        }
        return combinedHeaders;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError {
        long requestStart = SystemClock.elapsedRealtime();
        while (true) {

            VolleyLog.d("download start %s",request.getUrl());
            HttpResponse httpResponse = null;
            byte[] responseContents = null;
            List<Header> responseHeaders = Collections.emptyList();
            try {
                // Gather headers.
                Map<String, String> additionalRequestHeaders =
                        getCacheHeaders(request.getCacheEntry());
                httpResponse = mBaseHttpStack.
                        executeRequest(request, additionalRequestHeaders);
                int statusCode = httpResponse.getStatusCode();
                VolleyLog.d("download getStatus %s StatusCode: %s",request.getUrl(),statusCode);

                responseHeaders = httpResponse.getHeaders();
                // Handle cache validation.
                if (statusCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    Cache.Entry entry = request.getCacheEntry();
                    if (entry == null) {
                        return new NetworkResponse(
                                HttpURLConnection.HTTP_NOT_MODIFIED,
                                /* data= */ null,
                                /* notModified= */ true,
                                SystemClock.elapsedRealtime() - requestStart,
                                responseHeaders);
                    }
                    // Combine cached and response headers so the response will be complete.
                    List<Header> combinedHeaders = combineHeaders(responseHeaders, entry);
                    return new NetworkResponse(
                            HttpURLConnection.HTTP_NOT_MODIFIED,
                            entry.data,
                            /* notModified= */ true,
                            SystemClock.elapsedRealtime() - requestStart,
                            combinedHeaders);
                }

                // Some responses such as 204s do not have content.  We must check.
                InputStream inputStream = httpResponse.getContent();
                if (inputStream != null) {


                    if(request instanceof FileDownloadRequest){
                        FileDownloadRequest fileDownloadRequest = (FileDownloadRequest) request;
                        responseContents = fileDownloadRequest.handleRawResponse(httpResponse);
                    }
                } else {
                    // Add 0 byte response as a way of honestly representing a
                    // no-content request.
                    responseContents = new byte[0];
                }

                // if the request is slow, log it.
                long requestLifetime = SystemClock.elapsedRealtime() - requestStart;
                logSlowRequests(requestLifetime, request, responseContents, statusCode);

                if (statusCode < 200 || statusCode > 299) {
                    throw new IOException();
                }
               return new NetworkResponse(
                        statusCode,
                        responseContents,
                        /* notModified= */ false,
                        SystemClock.elapsedRealtime() - requestStart,
                        responseHeaders);
            } catch (SocketTimeoutException e) {
                attemptRetryOnException("socket", request, new TimeoutError());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad URL " + request.getUrl(), e);
            } catch (IOException e) {
                int statusCode;
                if (httpResponse != null) {
                    statusCode = httpResponse.getStatusCode();
                } else {
                    throw new NoConnectionError(e);
                }
                VolleyLog.e("%s Unexpected response code %d for %s",e.getMessage(), statusCode, request.getUrl());
                NetworkResponse networkResponse;
                if (responseContents != null) {
                    networkResponse =
                            new NetworkResponse(
                                    statusCode,
                                    responseContents,
                                    /* notModified= */ false,
                                    SystemClock.elapsedRealtime() - requestStart,
                                    responseHeaders);
                    if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED
                            || statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
                        attemptRetryOnException(
                                "auth", request, new AuthFailureError(networkResponse));
                    } else if(statusCode == HttpURLConnection.HTTP_MOVED_PERM
                            || statusCode == HttpURLConnection.HTTP_MOVED_TEMP){
                        attemptRetryOnException(
                                "auth", request, new AuthFailureError(networkResponse));
                    } else if (statusCode >= 400 && statusCode <= 499) {
                        // Don't retry other client errors.
                        throw new ServerError(networkResponse);
                    } else if (statusCode >= 500 && statusCode <= 599) {
                        if (request.shouldRetryServerErrors()) {
                            attemptRetryOnException(
                                    "server", request, new ServerError(networkResponse));
                        } else {
                            throw new ServerError(networkResponse);
                        }
                    } else {
                        // 3xx? No reason to retry.
                        throw new ServerError(networkResponse);
                    }
                } else {
                    attemptRetryOnException("network", request, new NetworkError(e));
                }
            }
        }
    }

    /**
     * Logs requests that took over SLOW_REQUEST_THRESHOLD_MS to complete.
     */
    private void logSlowRequests(
            long requestLifetime, Request<?> request, byte[] responseContents, int statusCode) {
        if (DEBUG || requestLifetime > SLOW_REQUEST_THRESHOLD_MS) {
            VolleyLog.d(
                    "HTTP response for request=<%s> [lifetime=%d], [size=%s], "
                            + "[rc=%d], [retryCount=%s]",
                    request,
                    requestLifetime,
                    responseContents != null ? responseContents.length : "null",
                    statusCode,
                    request.getRetryPolicy().getCurrentRetryCount());
        }
    }

    private Map<String, String> getCacheHeaders(Cache.Entry entry) {
        // If there's no cache entry, we're done.
        if (entry == null) {
            return Collections.emptyMap();
        }

        Map<String, String> headers = new HashMap<>();

        if (entry.etag != null) {
            headers.put("If-None-Match", entry.etag);
        }

        if (entry.lastModified > 0) {
            headers.put(
                    "If-Modified-Since", HttpHeaderParser.formatEpochAsRfc1123(entry.lastModified));
        }

        return headers;
    }

    protected void logError(String what, String url, long start) {
        long now = SystemClock.elapsedRealtime();
        VolleyLog.v("HTTP ERROR(%s) %d ms to fetch %s", what, (now - start), url);
    }

    /**
     * Reads the contents of an InputStream into a byte[].
     */
    private byte[] inputStreamToBytes(InputStream in, int contentLength)
            throws IOException, ServerError {

        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(mPool, contentLength);
        byte[] buffer = null;
        try {
            if (in == null) {
                throw new ServerError();
            }
            buffer = mPool.getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);

            }
            return bytes.toByteArray();
        } finally {
            try {
                // Close the InputStream and release the resources by "consuming the content".
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // This can happen if there was an exception above that left the stream in
                // an invalid state.
                VolleyLog.v("Error occurred when closing InputStream");
            }
            mPool.returnBuf(buffer);
            bytes.close();
        }
    }
}