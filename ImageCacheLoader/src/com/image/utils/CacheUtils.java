package com.image.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import com.image.cacheloader.CacheFactory;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * 图片缓存工具类
 * <!-- 在sd卡中创建与删除文件权限 -->
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    <!-- 向SD卡写入数据权限 -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 */
public class CacheUtils extends BitmapUtil {

	public final static String CACHE_DIR = "/libImageCache/";
	public final static int CLEAR_CACHE_AUTO_TIME_INETERVAL = 5 * 24 * 60 * 60 * 1000;
	public final static int CLEAR_CACHE_SPACE_INETERVAL = 60 * 60 * 1000;
	public final static int CHACHE_MAX_MB = 10;
	private static final String TAG = "ImageCacheUtils";

	/**
	 * save cache image from SD or files and return path
	 * 
	 * @param context
	 * @param fileName
	 * @param bitmap
	 * @param quality
	 * @return
	 */
	public static String saveCacheImage(Context context, Bitmap bitmap, String fileName, int quality) {
		if(bitmap == null) return null;
		String path = CacheUtils.saveImageToSD(bitmap, fileName, quality);
		if (path == null) {
			path = CacheUtils.saveImageToFiles(context, bitmap, fileName,
					quality);
		}
		if (CacheFactory.isDebug())
			Log.d(TAG, "save Cache Image path: " + path);
		return path;
	}

	/**
	 * get cache image from SD or files
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static Bitmap getCacheImage(Context context, String fileName) {
		Bitmap bm = CacheUtils.getImageFromSDCard(fileName);
		if (bm == null) {
			bm = CacheUtils.getImageFromFiles(context, fileName);
		}
		return bm;
	}

	public static String savePathImageToFiles(Context context, Bitmap bitmap,
			String urlOrPath, int quality) {
		String fileName = NetUtils.getFileName(urlOrPath);
		return saveImageToFiles(context, bitmap, fileName, quality);
	}

	public static String saveImageToFiles(Context context, Bitmap bitmap,
			String fileName, int quality) {
		if (bitmap == null)
			return null;
		FileOutputStream fos = null;
		clearCacheByModifyTime(context.getFilesDir().getAbsolutePath());
		try {
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			CompressFormat format = ImageUtils.getImageFormat(fileName);
			bitmap.compress(format, quality, stream);
			byte[] bytes = stream.toByteArray();
			fos.write(bytes);
			return context.getFilesDir() + "/" + fileName;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.flush();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * get image from context
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static Bitmap getImageFromFiles(Context context, String fileName) {
		File file = context.getFileStreamPath(fileName);
		FileInputStream fis = null;
		if (file != null && file.exists()) {
			try {
				file.setLastModified(System.currentTimeMillis());
				fis = context.openFileInput(fileName);
				return BitmapFactory.decodeStream(fis);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Log.w(TAG, "getBitmapFromFiles failed: bitmap null !!");
		return null;
	}

	public static String savePathImageToSD(Bitmap bitmap, String urlOrPath,
			int quality) {
		String fileName = NetUtils.getFileName(urlOrPath);
		return saveImageToSD(bitmap, fileName, quality);
	}

	/**
	 * 保存图片的方法 保存到sdcard
	 * 
	 * @param bitmap
	 * @param imageName
	 * @param quality
	 * @return
	 */
	public static String saveImageToSD(Bitmap bitmap, String imageName,
			int quality) {
		if(bitmap == null) return null;
		String filePath = mkdirIfNeed();
		File file = new File(filePath, imageName);
		clearCacheIfNeed(file.length());
		BufferedOutputStream bos = null;
		Exception exception = null;
		try {
			file.createNewFile();
			bos = new BufferedOutputStream(new FileOutputStream(file));
			CompressFormat format = ImageUtils.getImageFormat(imageName);
			bitmap.compress(format, quality, bos);
			String path = file.getAbsolutePath();
			if (CacheFactory.isDebug())
				Log.d(TAG, "save SDcard path: " + path);
			return path;
		} catch (FileNotFoundException e) {
			exception = e;
			e.printStackTrace();
		} catch (IOException e) {
			exception = e;
			e.printStackTrace();
		} catch (Exception e) {
			exception = e;
			e.printStackTrace();
		} finally {
			if (exception != null) {
				Log.e(TAG, "save bitmap to sdcard exception: " + exception);
			}
			try {
				if (bos != null) {
					bos.flush();
					bos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void clearCacheIfNeed(long length) {
		if(!CacheFactory.isAutoClearOldCache()) {
			if(CacheFactory.isDebug()) Log.i(TAG, "auto clear cache in disabled !! ");
			return;
		}
		SdcardInfo info = getSdcardInfo();
		boolean av = info.availableSize < length;
		String chachDir = getCacheDir();
		if (av) {
			// no available, delete old data files
			clearOldCacheBySpace(chachDir);
		} else {
			clearCacheByModifyTime(chachDir);
		}
	}

	/**
	 * Save Bitmap to a file.保存图片到SD卡。
	 * @param bitmap
	 * @param pathFile
	 * @param quality
	 * @throws IOException
	 */
	public static void saveBitmapToSdcard(Bitmap bitmap, String pathFile, int quality) throws IOException {
		if(bitmap == null) return;
		BufferedOutputStream os = null;
		try {
			File file = new File(pathFile);
			// String _filePath_file.replace(File.separatorChar +
			// file.getName(), "");
			int end = pathFile.lastIndexOf(File.separator);
			String _fileDir = pathFile.substring(0, end);
			File filePath = new File(_fileDir);
			if (!filePath.exists()) {
				boolean result = filePath.mkdirs();
				Log.i(TAG, "result: " + result + ", filePath: " + filePath);
			}
			clearCacheIfNeed(file.length());
			file.createNewFile();
			os = new BufferedOutputStream(new FileOutputStream(file));
			bitmap.compress(Bitmap.CompressFormat.PNG, quality, os);
			if (CacheFactory.isDebug())
				Log.i(TAG, "saveBitmapToSdcard: " + file.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.flush();
					os.close();
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * 获取sd卡的缓存路径， 一般在卡中sdCard就是这个目录
	 * 
	 * @return SDPath
	 */
	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		} else {
			Log.e("ERROR", "No External Storage");
		}
		return sdDir.toString();
	}

	/**
	 * 获取SDCard文件
	 * 
	 * @return Bitmap
	 */
	public static Bitmap getImageFromSDCard(String imageName) {
		String filepath = getCacheDir() + "/" + imageName;
		try {
			File file = new File(filepath);
			if (file.exists()) {
				Bitmap bm = BitmapFactory.decodeFile(filepath);
				file.setLastModified(System.currentTimeMillis());
				return bm;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getPathImageFromSDCard(String filepath) {
		File file = new File(filepath);
		if (file.exists()) {
			try {
				file.setLastModified(System.currentTimeMillis());
				Bitmap bm = BitmapFactory.decodeFile(filepath);
				return bm;
			} catch (Exception e) {
				Log.e(TAG, "get SDCard image failed, filepath: " + filepath);
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 获取缓存文件夹目录 如果不存在创建 否则则创建文件夹
	 * 
	 * @return filePath
	 */
	private static String mkdirIfNeed() {
		String filePath = getCacheDir();
		File file = new File(filePath);
		if (!file.exists()) {
			boolean result = file.mkdirs();
			if (!result) {
				Log.e(TAG, "Failed make dir: " + filePath);
			}
		}
		return filePath;
	}

	public static long getDirSize(String dir) {
		if(dir == null || "".equals(dir)) return 0;
		File file = new File(dir);
		return getDirSize(file);
	}
	
	public static long getDirSize(File file) {
		if(file == null) return 0;
		java.io.File[] fileList = file.listFiles();
		if(fileList == null) return 0;
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
	
	public static long getCacheSize() {
		String filePath = getCacheDir();
		return getDirSize(filePath);
	}
	
	public static int getCacheMbSize(String filePath) {
		long cacheSize = getDirSize(filePath);
		return (int) (cacheSize / 1048576);
	}
	
	public static String getCacheDir() {
		return getSDPath() + CACHE_DIR;
	}

	/**
	 * delete old data image
	 * @return
	 */
	public static void clearOldCacheBySpace(final String dir) {
		new Thread(new Runnable() {
			public void run() {
				int mb = getCacheMbSize(dir);
				int count = 0;
				if(mb > CHACHE_MAX_MB) {
					File file = new File(dir);
					java.io.File[] fileList = file.listFiles();
					if(fileList == null) return;
					for (int i = 0; i < fileList.length; i++) {
						File fi = fileList[i];
						long now = System.currentTimeMillis();
						long time = now - CLEAR_CACHE_SPACE_INETERVAL;
						if(fi.lastModified() < time) {
							count += deleteFile(fi) ? 1 : 0;
						}
					}
				}
				Log.i(TAG, "clearOldCacheBySpace count= " + count);
			}
		});
	}
	
	public static void clearCacheByModifyTime(final String dir) {
		if(dir == null) return;
		final File file = new File(dir);
		new Thread(new Runnable() {
			public void run() {
				int count = 0;
				if(file != null) {
					java.io.File[] fileList = file.listFiles();
					if(fileList == null) return;
					for (int i = 0; i < fileList.length; i++) {
						File fi = fileList[i];
						long now = System.currentTimeMillis();
						long time = now - CLEAR_CACHE_AUTO_TIME_INETERVAL;
						if(fi.lastModified() < time) {
							count += deleteFile(fi) ? 1 : 0;
						}
					}
				}
				Log.i(TAG, "clearCacheByModifyTime count= " + count);
			}
		});
	}

	/**
	 * delete all file of foder
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean deleteFolder(String folderPath) {
		File file = new File(folderPath);
		if (!file.exists()) {
			return false;
		} else {
			if (file.isFile()) {
				return deleteFile(folderPath);
			} else {
				return deleteDirectory(folderPath);
			}
		}
	}

	/**
	 * delete all file of file path
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean deleteDirectory(String filePath) {
		boolean flag = false;
		if (!filePath.endsWith(File.separator)) {
			filePath = filePath + File.separator;
		}
		File dirFile = new File(filePath);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		return dirFile.delete();
	}
	
	/**
	 * @param filePath
	 * @return boolean true or false
	 */
	public static boolean deleteFile(String filePath) {
		File file = new File(filePath);
		return deleteFile(file);
	}

	public static boolean deleteFile(File file) {
		if (file.isFile() && file.exists()) {
			return file.delete();
		}
		return false;
	}

	/**
	 * 
	 * 获取sdcard使用情况
	 */
	public static SdcardInfo getSdcardInfo() {
		SdcardInfo info = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File filePath = Environment.getExternalStorageDirectory(); // 获得sd卡的路径
			info = new SdcardInfo();
			StatFs stat = new StatFs(filePath.getPath()); // 创建StatFs对象
			info.blockSize = stat.getBlockSize(); // 获取block的size
			info.totalBlocks = stat.getBlockCount(); // 获取block的总数
			info.totalSize = (long)info.totalBlocks * info.blockSize;
			info.availableBlocks = stat.getAvailableBlocks(); // 获取可用块大小
			info.availableSize = (long)info.availableBlocks * info.blockSize;
			info.totalGbSize = info.totalSize / 1024 / 1024 / 1024;

			DecimalFormat df = new DecimalFormat("#0.0");

			info.totalGbSizeStr = df.format(info.totalGbSize); // 总共大小

			info.usedTotalGbSize = (info.totalSize - info.availableSize) / 1024 / 1024 / 1024;
			// 已用大小
			info.usedTotalGbSizeStr = df.format(info.usedTotalGbSize);
		}
		return info;
	}

	public static class SdcardInfo {
		public int blockSize;
		public int totalBlocks;
		public int availableBlocks;
		public long availableSize;
		public long totalSize;
		public float totalGbSize;
		public String totalGbSizeStr;
		public float usedTotalGbSize;
		public String usedTotalGbSizeStr;

		@Override
		public String toString() {
			return "SdcardInfo [blockSize=" + blockSize + ", totalBlocks="
					+ totalBlocks + ", availableBlocks=" + availableBlocks
					+ ", availableSize=" + availableSize + ", totalSize="
					+ totalSize + ", totalGbSize=" + totalGbSize
					+ ", totalGbSizeStr=" + totalGbSizeStr
					+ ", usedTotalGbSize=" + usedTotalGbSize
					+ ", usedTotalGbSizeStr=" + usedTotalGbSizeStr + "]";
		}
	}
}
