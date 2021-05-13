package com.http.helper;

public class ReqResponce {
	int statusCode = -1;
	String json = "";
	Throwable throwable;
	int exceptionType= -1;
	boolean interrupted = false;
	boolean timeouted = false;

	public ReqResponce() {
		super();
	}
	
	public ReqResponce(int statusCode, String json,
			Throwable throwable) {
		super();
		this.statusCode = statusCode;
		this.json = json;
		this.throwable = throwable;
	}

	@Override
	public String toString() {
		return "QueueResponce [statusCode=" + statusCode + ", json=" + json
				+ ", throwable=" + throwable + ", exceptionType="
				+ exceptionType + ", interrupted=" + interrupted
				+ ", timeouted=" + timeouted + "]";
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public boolean isInterrupted() {
		return interrupted;
	}

	public boolean isTimeouted() {
		return timeouted;
	}

	public void setTimeouted(boolean timeouted) {
		this.timeouted = timeouted;
	}

	public int getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(int exceptionType) {
		this.exceptionType = exceptionType;
	}

	public void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
		if(throwable instanceof Exception) {
			Exception ex = (Exception) throwable;
			this.exceptionType = ReqException.parseType(ex);
		}
	}
}
