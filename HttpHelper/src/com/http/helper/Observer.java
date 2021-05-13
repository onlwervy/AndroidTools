package com.http.helper;

public interface Observer {
	/**
	 * post result after asyncTask done task
	 * @param id : post flag id
	 * @param result
	 */
	void post(int id, Object result);
}
