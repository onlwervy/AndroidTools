package com.image.cacheloader.entity;

public class CacheBase {
	long updateTime = 0;
	Object key = null;
	
	@Override
	public String toString() {
		return "key= " + key + ", updateTime= " +updateTime;
	}
	
	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}
}
