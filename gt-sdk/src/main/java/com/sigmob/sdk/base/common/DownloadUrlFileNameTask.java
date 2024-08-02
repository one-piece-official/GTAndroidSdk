package com.sigmob.sdk.base.common;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.czhj.sdk.logger.SigmobLog;
import com.czhj.sdk.common.ThreadPool.ThreadPoolFactory;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.utils.Md5Util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.czhj.sdk.common.network.ResponseHeader.USER_AGENT;


public class DownloadUrlFileNameTask extends AsyncTask<String, Void, HashMap<String,String>> {
    private static final int REDIRECT_LIMIT = 10;

    public interface DownloadUrlFileNameListener {
        void onSuccess(final String fileName, String url);
        void onFailure(final String message, final Throwable throwable);
    }

     private final DownloadUrlFileNameListener mListener;



    public static String getFileName(final String urlString){
        if (urlString.toLowerCase().contains(".apk")){
            String  result = parsefsnameIsAPK(urlString);

            if (!TextUtils.isEmpty(result)){
                return result;
            }else {
                result = parseAPK(urlString);
                if (!TextUtils.isEmpty(result)){
                    return result;
                }
            }
        }
        return null;
    }
    public static void getFileName( final String urlString,
                                       final DownloadUrlFileNameListener listener) {

        try {


            String result = getFileName(urlString);

            if (!TextUtils.isEmpty(result)){
                listener.onSuccess(result,urlString);
            } else {
                final DownloadUrlFileNameTask urlResolutionTask = new DownloadUrlFileNameTask(listener);
                urlResolutionTask.executeOnExecutor(ThreadPoolFactory.BackgroundThreadPool.getInstance().getExecutorService(),urlString);

            }

        } catch (Throwable e) {
            listener.onFailure("Failed to resolve url", e);
        }
    }

    private DownloadUrlFileNameTask(DownloadUrlFileNameListener listener) {
        mListener = listener;
    }




    @Override
    protected HashMap<String,String> doInBackground( String... urls) {
        if (urls == null || urls.length == 0) {
            return null;
        }
        String previousUrl = null;

        HashMap<String,String> result = new HashMap<>();

        try {
            String locationUrl = urls[0];
            try {
                locationUrl= getRedirectLocation(locationUrl);
            }catch (Throwable throwable){

                SigmobLog.e(throwable.getMessage());
            }
            result.put("url",locationUrl);

            String fileName = getDownloadUrlFilename(locationUrl);

            result.put("fileName",fileName);

        } catch (Throwable e) {
             SigmobLog.e(e.getMessage());
        }
        return result;
    }



    private String getRedirectLocation( final String urlString) throws IOException,
            URISyntaxException {

        String result = null;
        String jumpURL = urlString;

        try {

            while (jumpURL != null){

                result = jumpURL;



                jumpURL = resolveRedirectLocation(jumpURL);
            }

        } catch (Throwable throwable){
            SigmobLog.e(throwable.getMessage());
        }
        return result;
    }




    private static String resolveRedirectLocation( final String baseUrl) throws IOException, URISyntaxException {

        String result = null;

        HttpURLConnection httpUrlConnection = null;

        try {
            final URL url = new URL(baseUrl);

            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setInstanceFollowRedirects(false);
            httpUrlConnection.addRequestProperty(USER_AGENT.getKey(), Networking.getUserAgent());
            final int responseCode = httpUrlConnection.getResponseCode();
            final String redirectUrl = httpUrlConnection.getHeaderField("Location");

            if (responseCode >= 300 && responseCode < 400) {

                // If redirectUrl is a relative path, then resolve() will correctly complete the path;
                // otherwise, resolve() will return the redirectUrl
                result =  redirectUrl;
            }
        } catch (Throwable e) {
            // Ensure the request is cancelled instead of resolving an intermediary URL
            SigmobLog.e("resolveRedirectLocation fail",e);
        }finally {
            if (httpUrlConnection != null) {
                httpUrlConnection.disconnect();
            }
        }

        return result;
    }


    public static String parsefsnameIsAPK(String contentDisposition) {
        try {
            Matcher m = Pattern.compile("(fsname=)(.*?apk)",
                    Pattern.CASE_INSENSITIVE).matcher(contentDisposition);
            if (m.find()) {
                return m.group(2);
            }
        } catch (Throwable ex) {
            // This function is defined as returning null when it can't parse the header
        }
        return null;
    }

    public static String parseAPK(String contentDisposition) {
        try {
            Matcher m = Pattern.compile("[\\w\\.]+\\.apk",
                    Pattern.CASE_INSENSITIVE).matcher(contentDisposition);
            if (m.find()) {
                return m.group(0);
            }
        } catch (Throwable ex) {
            // This function is defined as returning null when it can't parse the header
        }
        return null;
    }



    private String getDownloadUrlFilename( final String urlString) throws IOException{


        String result = null;
        HttpURLConnection httpUrlConnection = null;

        try {

          if(!TextUtils.isEmpty(result)){
              result = getFileName(urlString);

          }else {
              final URL url = new URL(urlString);

              httpUrlConnection = (HttpURLConnection) url.openConnection();
              httpUrlConnection.setInstanceFollowRedirects(false);
              httpUrlConnection.addRequestProperty(USER_AGENT.getKey(), Networking.getUserAgent());

              String fileName = getFilenameFromHeaderField(urlString, httpUrlConnection);

              if(fileName !=  null) {
                  if(fileName.contains("fsname=")) {
                      result = parsefsnameIsAPK(urlString);
                  }else if(fileName.contains(".apk")){
                      result = parseAPK(fileName);
                  } else {
                      result = fileName;
                  }
              }
              else{
                  if(!TextUtils.isEmpty(urlString)){
                      Uri uri = Uri.parse(urlString);

                      String urlPath =  uri.getPath();

                      String a[] = urlPath.split("/");
                      if (a.length > 1) {
                          fileName = a[a.length - 1];
                      }

                      if(TextUtils.isEmpty(fileName) || !fileName.toLowerCase().endsWith(".apk")){
                          result = Md5Util.md5(urlString)+".apk";
                      }

                  }
              }

            }

        }catch (Throwable throwable){
            SigmobLog.e("getDownloadUrlFilename", throwable);
        } finally{
            if (httpUrlConnection != null) {
                httpUrlConnection.disconnect();
            }
        }
        return result;
    }



    private static String getFilenameFromHeaderField( final String baseUrl,
                                                   final HttpURLConnection httpUrlConnection) throws IOException, URISyntaxException {
        final int responseCode = httpUrlConnection.getResponseCode();
        String result = null;

        if (responseCode >= 200 && responseCode < 400) {
            try {
                // If redirectUrl is a relative path, then resolve() will correctly complete the path;
                // otherwise, resolve() will return the redirectUrl
                //do check on URL and handle 404 etc.
                String raw = httpUrlConnection.getHeaderField("Content-Disposition");
                // raw = "attachment; filename=abc.jpg"
               return URLUtil.guessFileName(baseUrl,raw,null);
            } catch (IllegalArgumentException e) {
                // Ensure the request is cancelled instead of resolving an intermediary URL
                SigmobLog.e("Invalid URL redirection. baseUrl=" + baseUrl );
                throw new URISyntaxException(baseUrl, "Unable to parse invalid URL");
            } catch (NullPointerException e) {
                SigmobLog.e("Invalid URL redirection. baseUrl=" + baseUrl );
                throw e;
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute( final HashMap<String,String> map) {

        String fileName = null;
        String url = null;

        if(map != null){
            fileName = map.get("fileName");
            url = map.get("url");
        }

        mListener.onSuccess(fileName,url);

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        mListener.onFailure("Task for resolving url was cancelled", null);
    }
}



