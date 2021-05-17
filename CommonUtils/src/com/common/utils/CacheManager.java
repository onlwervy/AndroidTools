
package com.common.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 缓存管理：清除缓存文件/计算缓存文件大小
 * @author hulk
 *
 */
public class CacheManager {

    public static final int CACHE_SIZE_CHECK_CODE = 400;
    public final static int CACHE_CLEAR_COMPLETE_CODE = 401;
    public final static int SIZE_UNIT_M = 1048576;
    public final static int SIZE_UNIT_K = 1024;
    private static final String TAG = "CacheManager";

    public static void checkDataCacheSize(Context cxt, Handler handler) {
        checkDirSize(getDataCachePathList(cxt), handler);
    }

    public static void clearDataCacheFiles(Context cxt, Handler handler) {
        clearDirFiles(getDataCachePathList(cxt), handler);
    }

    public static List<String> getDataCachePathList(Context cxt) {
        List<String> pathList = new ArrayList<String>();
        pathList.add(cxt.getCacheDir().getAbsolutePath());
        pathList.add(cxt.getFilesDir().getAbsolutePath());
        return pathList;
    }

    /**
     * Get the file count size (kb) of path list, and receive result in handler:
     * <p>Message msg = handler.obtainMessage(CacheManager.CACHE_SIZE_CHECK_CODE = 400);
       <p> msg.arg1 = errorCode;
       <p> msg.arg2 = dirSize;
       <p> msg.obj = obj;
       <p> msg.sendToTarget();
     * @param pathList eg: Context.getCacheDir(), Context.getFilesDir(), and so on.
     * @param handler
     */
    public static void checkDirSize(final List<String> pathList, final Handler handler) {
        new Thread() {
            @Override
            public void run() {
                long cacheSize = 0;
                try {
                    if(pathList != null) {
                        for (String path : pathList) {
                            cacheSize += getDirSize(path);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(handler != null) {
                    int sizeK = (int) (cacheSize / SIZE_UNIT_K);
                    notifyHandler(handler, CACHE_SIZE_CHECK_CODE, 0, sizeK, null);
                }
            };
        }.start();
    }

    /**
     * clear folder files,and receive result in handler:
     * <p>Message msg = handler.obtainMessage(CacheManager.CACHE_CLEAR_COMPLETE_CODE = 401);
       <p> msg.arg1 = errorCode;
       <p> msg.arg2 = delSize;
       <p> msg.obj = isSucess;
       <p> msg.sendToTarget();
     * @param pathList
     * @param handler
     */
    public static void clearDirFiles(final List<String> pathList, final Handler handler) {
        if (pathList != null && !pathList.isEmpty()) {
            new Thread() {
                @Override
                public void run() {
                    int delSizeM = 0;
                    for (String path : pathList) {
                        delSizeM += deleteFolder(path);
                    }
                    if(handler != null) {
                        notifyHandler(handler, CACHE_CLEAR_COMPLETE_CODE, 0, delSizeM, delSizeM > 0);
                    }
                };
            }.start();
        }
    }

    private static void notifyHandler(Handler handler, int what, int errorCode, int dirSize, Object obj) {
        Message msg = handler.obtainMessage(what);
        msg.arg1 = errorCode;
        msg.arg2 = dirSize;
        msg.obj = obj;
        msg.sendToTarget();
    }

    public static long getDirSize(String path) {
        if(path == null || path.length() == 0) return 0;
        long size = 0;
        try {
            File f = new File(path);
            if(f.exists()) {
                size = getDirSize(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * get size of isDirectory or file.
     * @param file
     * @return  the  count bytes of file or dir
     * @throws Exception
     */
    public static long getDirSize(java.io.File file) throws Exception {
        if (file == null)
            return 0;
        if (file.isFile()) {
            return file.length();
        }
        java.io.File[] fileList = file.listFiles();
        long size = 0;
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) {
                size = size + getDirSize(fileList[i]);
            } else {
                size = size + fileList[i].length();
            }
        }
        return size;
    }

    /**
     * delete all file of foder
     * 
     * @param filePath
     * @return deleted size (KB).
     */
    public static int deleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return 0;
        } else {
            if (file.isFile()) {
                boolean del = deleteFile(file);
                return (int) (del ? file.length() / SIZE_UNIT_K : 0);
            } else {
                long delSize = deleteDirectory(file.getAbsolutePath());
                return (int) (delSize / SIZE_UNIT_K);
            }
        }
    }

    /**
     * delete all file of file path
     * 
     * @param filePath
     * @return
     */
    public static long deleteDirectory(String filePath) {
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists()) {
            return 0;
        }
        if(!dirFile.isDirectory()) {
            return dirFile.delete() ? dirFile.length() : 0;
        }
        long delSize = 0;
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if(!f.exists()) continue;
            if (f.isFile()) {
                long len = f.length();
                boolean del = deleteFile(f);
                if (del) {
                    delSize += len;
                } else {
                    Log.w(TAG, "Delete file failed: " + f.getPath());
                }
            } else {
                delSize += deleteDirectory(files[i].getAbsolutePath());
            }
        }
        dirFile.delete();
        if (delSize <= 0)
            Log.w(TAG, "Delete directory failed: " + filePath);
        return delSize;
    }
    
    public static boolean deleteFile(File file) {
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }
}
