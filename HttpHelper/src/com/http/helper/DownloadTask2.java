
package com.http.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 下载任务
 * <p>
 * 参数为url及保存路径
 */
public class DownloadTask2 extends
        AsyncTask<String, Integer, DownloadTask2.STATE> {
    public static final int REQUEST_TIMEOUT = 60 * 1000;
    public static enum STATE {
        SUCCESS, HTTP_IO_ERROR, FILE_IO_ERROR
    };

    public interface Callback {
        /**
         * @param progress 进度, 0-100
         */
        public void onProgressUpdate(int progress);

        /**
         * @param state 执行结果
         */
        public void onFinish(STATE state);
    }

    private static final String TAG = "DownloadTask";
    private static final int PARAMS_NUM = 2;
    private Callback mCallback = null;
    private Context mContext = null;

    public DownloadTask2(Callback cb, Context context) {
        mCallback = cb;
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "pre execute");
    }

    @Override
    protected STATE doInBackground(String... params) {
        if (params == null || PARAMS_NUM != params.length) {
            throw new IllegalArgumentException("only accepts " + PARAMS_NUM
                    + " params.");
        }
        String url = params[0];
        String filePath = params[1];
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url is null.");
        } else if (TextUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("download path is null.");
        }
        Log.d(TAG, "url:" + url + ", file path:" + filePath);

        STATE result = null;
        HttpClient httpClient = getHttpClient(mContext);
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(request);
            if (response == null
                    || HttpStatus.SC_OK != response.getStatusLine()
                            .getStatusCode()) {
                throw new IOException("url connection error.");
            }
            long fileLength = response.getEntity().getContentLength();
            if (fileLength <= 0) {
                throw new IOException("download file length is negative.");
            }
            Log.d(TAG, "file length:" + fileLength);

            InputStream is = null;
            OutputStream out = null;
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
                if (!file.createNewFile()) {
                    throw new IOException("can not create file.");
                }
                is = response.getEntity().getContent();
                out = new BufferedOutputStream(
                        new FileOutputStream(file));
                byte[] buffer = new byte[4 * 1024];
                int len = -1;
                int sum = 0;
                while ((len = is.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    sum += len;
                    publishProgress((int) (sum * 100 / fileLength));
                }
                out.flush();
                result = STATE.SUCCESS;
            } catch (IOException e) {
                e.printStackTrace();
                result = STATE.FILE_IO_ERROR;
            } finally {
                if (is != null) {
                    is.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = STATE.HTTP_IO_ERROR;
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Log.d(TAG, "progress:" + values[0]);
        super.onProgressUpdate(values);
        if (mCallback != null) {
            mCallback.onProgressUpdate(values[0]);
        }
    }

    protected void onPostExecute(STATE result) {
        Log.d(TAG, "post execute state:" + result);
        super.onPostExecute(result);
        if (mCallback != null) {
            mCallback.onFinish(result);
        }
    }

    public HttpClient getHttpClient(Context context) {
        return getHttpClient(context, REQUEST_TIMEOUT, REQUEST_TIMEOUT);
    }

    public HttpClient getHttpClient(Context context, int connectionTimeout, int socketTimeout) {
        BasicHttpParams params = new BasicHttpParams();
        // 设置连接及响应超时时间(s)
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setSoTimeout(params, socketTimeout);
        
        return new DefaultHttpClient(params);
    }
}
