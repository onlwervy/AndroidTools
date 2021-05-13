package com.image.cacheloader.impl;

import java.util.LinkedList;
import java.util.List;

import com.image.cacheloader.CacheFactory;
import com.image.cacheloader.api.CacheAPI;
import com.image.cacheloader.entity.CacheBase;



import android.util.Log;

public class CacheImpl<T extends CacheBase> implements CacheAPI<T> {
	private static final String TAG = "CacheImpl";
	public boolean debugMode = CacheFactory.isDebug();

	private int capacity = 10;

	List<T> list = null;

	public CacheImpl() {
		list = new LinkedList<T>();
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getCount() {
		return list.size();
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public void add(T data) {
		synchronized (list) {
			try {
				list.remove(data);
				if (isBeyondCapacity()) {
					T t = list.remove(0);
					recycle(t);
				}
				list.add(data);
				if(debugMode) {
					Log.i(TAG, "add data = " + data + ", size= " + list.size());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void add(Object key, T data) {
		synchronized (list) {
			try {
				list.remove(data);
				if (isBeyondCapacity()) {
					T t = list.remove(0);
					recycle(t);
					t = null;
				}
				list.add(data);
				if(debugMode) {
					Log.i(TAG, "add key = " + key + ", size= " + list.size());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void removeFirst() {
		synchronized (list) {
			T t = list.remove(0);
			recycle(t);
			t = null;
		}
	}

	public boolean remove(T data) {
		synchronized (list) {
			try {
				return list.remove(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void removeByKey(Object key) {
		if(key == null) return;
		synchronized (list) {
			T temp = null;
			for (T t : list) {
				if (t != null && key.equals(t.getKey())) {
					temp = t;
					break;
				}
			}
			if (temp != null) {
				list.remove(temp);
				recycle(temp);
			}
		}
	}

	public T remove(int index) {
		if (index >= list.size()) {
			return null;
		}
		synchronized (list) {
			return list.remove(index);
		}
	}

	/**
	 * Traversal cache list: get it and add it to last location if exist, or return null
	 * @param key
	 * @return the value of key
	 */
	public T get(Object key) {
		T temp = null;
		int index = -1;
		if(key != null) {
			for (int i = list.size() - 1; i >= 0; i--) {
				T t = list.get(i);
				if (t != null && key.equals(t.getKey())) {
					index = i;
					break;
				}
			}
			if(index > -1) {
				temp = list.remove(index);
				list.add(temp);
			}
			
		}
		return temp;
	}

	
	public boolean isBeyondCapacity() {
		return list != null && list.size() > capacity;
	}
	
	public void cleanCache() {
		for (T t : list) {
			recycle(t);
		}
		list.clear();
	}
	
	public void clearCache() {
		cleanCache();
		System.gc();
        System.runFinalization();
	}

	public void recycle(T data) {
		data = null;
	}

	@Override
	public void recycle() {
		clearCache();
	}
}
