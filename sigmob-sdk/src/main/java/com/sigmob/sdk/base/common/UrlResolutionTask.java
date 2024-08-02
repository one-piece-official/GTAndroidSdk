package com.sigmob.sdk.base.common;

import static com.czhj.sdk.common.network.ResponseHeader.USER_AGENT;

import android.net.Uri;
import android.os.AsyncTask;

import com.czhj.sdk.common.ThreadPool.ThreadPoolFactory;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.logger.SigmobLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


public class UrlResolutionTask extends AsyncTask<String, Void, String> {
    private static final int REDIRECT_LIMIT = 10;
    private final UrlResolutionListener mListener;

    private UrlResolutionTask(UrlResolutionListener listener) {
        mListener = listener;
    }

    public static void getResolvedUrl(final String urlString,
                                      final UrlResolutionListener listener) {
        final UrlResolutionTask urlResolutionTask = new UrlResolutionTask(listener);

        try {
            urlResolutionTask.executeOnExecutor(ThreadPoolFactory.BackgroundThreadPool.getInstance().getExecutorService(), urlString);
        } catch (Throwable e) {
            listener.onFailure("Failed to resolve url", e);
        }
    }

    private static String resolveRedirectLocation(final String baseUrl,
                                                  final HttpURLConnection httpUrlConnection) throws IOException, URISyntaxException {
        final URI baseUri = new URI(baseUrl);
        final int responseCode = httpUrlConnection.getResponseCode();
        final String redirectUrl = httpUrlConnection.getHeaderField("Location");
        String result = null;

        if (responseCode >= 300 && responseCode < 400) {
            try {
                // If redirectUrl is a relative path, then resolve() will correctly complete the path;
                // otherwise, resolve() will return the redirectUrl
                result = baseUri.resolve(redirectUrl).toString();
            } catch (IllegalArgumentException e) {
                // Ensure the request is cancelled instead of resolving an intermediary URL
                SigmobLog.e("Invalid URL redirection. baseUrl=" + baseUrl + "\n redirectUrl=" + redirectUrl);
                throw new URISyntaxException(redirectUrl, "Unable to parse invalid URL");
            } catch (Throwable e) {
                SigmobLog.e("Invalid URL redirection. baseUrl=" + baseUrl + "\n redirectUrl=" + redirectUrl);
                throw e;
            }
        }

        return result;
    }

    @Override
    protected String doInBackground(String... urls) {
        if (urls == null || urls.length == 0) {
            return null;
        }

        String previousUrl = null;
        try {
            String locationUrl = urls[0];

            while (locationUrl != null) {
                // if location url is not http(s), assume it's an SigmobAndroid deep link
                // this scheme will fail URL validation so we have to check early
                if (!UrlAction.OPEN_WITH_BROWSER.shouldTryHandlingUri(Uri.parse(locationUrl), 0)) {
                    return locationUrl;
                }
                previousUrl = locationUrl;
                locationUrl = getRedirectLocation(locationUrl);
            }

        } catch (Throwable e) {
            SigmobLog.w(e.getMessage());

        }

        return previousUrl;
    }


    private String getRedirectLocation(final String urlString) throws IOException,
            URISyntaxException {
        final URL url = new URL(urlString);

        HttpURLConnection httpUrlConnection = null;
        try {
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setInstanceFollowRedirects(false);
            httpUrlConnection.addRequestProperty(USER_AGENT.getKey(), Networking.getUserAgent());

            return resolveRedirectLocation(urlString, httpUrlConnection);
        } finally {
            if (httpUrlConnection != null) {
                final InputStream is = httpUrlConnection.getInputStream();
                if (is != null) {
                    try {
                        is.close();
                    } catch (Throwable e) {
                        SigmobLog.d("IOException when closing httpUrlConnection. Ignoring.");
                    }
                }
                httpUrlConnection.disconnect();
            }
        }
    }

    @Override
    public void onPostExecute(final String resolvedUrl) {
        super.onPostExecute(resolvedUrl);

        if (isCancelled() || resolvedUrl == null) {
            onCancelled();
        } else {
            mListener.onSuccess(resolvedUrl);
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        mListener.onFailure("Task for resolving url was cancelled", null);
    }

    public interface UrlResolutionListener {
        void onSuccess(final String resolvedUrl);

        void onFailure(final String message, final Throwable throwable);
    }
}



