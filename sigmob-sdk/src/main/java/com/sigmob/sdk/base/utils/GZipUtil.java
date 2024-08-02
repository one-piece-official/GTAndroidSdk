package com.sigmob.sdk.base.utils;

import android.os.AsyncTask;

import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.archives.ArchiveException;
import com.sigmob.sdk.archives.ArchiveStreamFactory;
import com.sigmob.sdk.archives.tar.TarArchiveEntry;
import com.sigmob.sdk.archives.tar.TarArchiveInputStream;
import com.sigmob.sdk.archives.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public final class GZipUtil {

    // 压缩
    private static byte[] compress(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(data);
        gzip.close();
        return out.toByteArray();//out.toString("ISO-8859-1");
    }


    public static byte[] compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return null;
        }
        return compress(str.getBytes("utf-8"));
    }


    // 解压缩
    private static byte[] uncompress(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return data;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        GZIPInputStream gunzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n;
        while ((n = gunzip.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        gunzip.close();
        in.close();
        return out.toByteArray();
    }


    /** Untar an input file into an output file.

     * The output file is created in the output folder, having the same name
     * as the input file, minus the '.tar' extension.
     *
     * @param inputFile     the input .tar file
     * @param outputDir     the output directory file.
     * @throws IOException
     * @throws FileNotFoundException
     *
     * @return  The {@link List} of {@link File}s with the untared content.
     * @throws ArchiveException
     */
    public static List<File> uncompressTarGzipSync(final File inputFile, final File outputDir) throws IOException, ArchiveException {

        SigmobLog.i(String.format("Untaring %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));

        final List<File> untaredFiles = new LinkedList<>();
        InputStream is = new FileInputStream(inputFile);
        GZIPInputStream gzipInputStream = null;
        try {

            gzipInputStream = new GZIPInputStream(is);
        }catch (Throwable e){
            if(is != null)
                is.close();
            is = new FileInputStream(inputFile);
            SigmobLog.e(e.getMessage());
        }

        TarArchiveInputStream debInputStream = null;
        if(gzipInputStream != null){
            debInputStream =  (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", gzipInputStream);
        }else {
            debInputStream =  (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
        }
        TarArchiveEntry entry = null;
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }
        while ((entry = (TarArchiveEntry)debInputStream.getNextEntry()) != null) {
            final File outputFile = new File(outputDir, entry.getName());
            if (entry.isDirectory()) {
                SigmobLog.i(String.format("Attempting to write output directory %s.", outputFile.getAbsolutePath()));
                if (!outputFile.exists()) {
                    SigmobLog.i(String.format("Attempting to create output directory %s.", outputFile.getAbsolutePath()));
                    if (!outputFile.mkdirs()) {
                        throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
                    }
                }
            } else {
                SigmobLog.i(String.format("Creating output file %s.", outputFile.getAbsolutePath()));
                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }
                final OutputStream outputFileStream = new FileOutputStream(outputFile);
                IOUtils.copy(debInputStream, outputFileStream);
                outputFileStream.close();
            }
            untaredFiles.add(outputFile);
        }
        debInputStream.close();

        if(gzipInputStream != null){
            gzipInputStream.close();
        }

        if(is != null){
            is.close();
        }
        return untaredFiles;
    }

    interface UnCompressAsyncTaskListener {
        void onFinish(boolean success);
    }



    public static void uncompressTarGzipAsync(final File inputFile, final File outputDir, UnCompressAsyncTaskListener unCompressAsyncTaskListener){

        UnCompressAsyncTask asyncTask = new UnCompressAsyncTask(inputFile, outputDir,unCompressAsyncTaskListener);

        SigmobLog.d("uncompressTarGzipAsync()  inputFile = [" + inputFile + "], outputDir = [" + outputDir + "], unCompressAsyncTaskListener = [" + unCompressAsyncTaskListener + "]");
    }


    private static class UnCompressAsyncTask extends AsyncTask<String, Void, Boolean> {

       private final File mInputFile;
       private final File mOutputDir;
       private final UnCompressAsyncTaskListener mListener;




        UnCompressAsyncTask(final File inputFile, final File outputDir, UnCompressAsyncTaskListener listener) {
            mInputFile = outputDir;
            mOutputDir = inputFile;
            mListener = listener;
        }


        protected Boolean doInBackground( String[] params) {

            try {
                uncompressTarGzipSync(mInputFile, mOutputDir);
                return true;
            } catch (Throwable e) {
                 SigmobLog.e(e.getMessage());
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {

            if(mListener != null){
                mListener.onFinish(result);
            }
        }

    }


    public static String uncompress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        byte[] data = uncompress(str.getBytes("utf-8")); // ISO-8859-1
        return new String(data);
    }

}
