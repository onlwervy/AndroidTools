package com.image.cacheloader.api;

public interface CacheAPI<T> {
	void add(T data);

	void removeByKey(Object key);

	T remove(int index);

	boolean remove(T data);

	/**
	 * Traversal cache list: get it and add it to last location if exist, or
	 * return null
	 * 
	 * @param key
	 * @return the value of key
	 */
	T get(Object key);

	int getCount();

	int getCapacity();

	void setCapacity(int capacity);

	void cleanCache();

	void clearCache();
	
	void setDebugMode(boolean debugMode);

	void removeFirst();
	
	void recycle();
}
