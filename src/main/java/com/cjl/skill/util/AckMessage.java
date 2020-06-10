package com.cjl.skill.util;

public class AckMessage<T> {
	private int status;
	private String message;
	private T data;

	public AckMessage() {}
	
	public AckMessage(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public AckMessage(int status, String message, T data) {
		this.status = status;
		this.data = data;
		this.message = message;
	}

	public static <T> AckMessage<T> ok() {
		return new AckMessage<T>(200,"success", null);
	}

	public static <T> AckMessage<T> ok(T data) {
		return new AckMessage<T>(200,"success", data);
	}

	public static <T> AckMessage<T> illegalArgs() {
		return new AckMessage<T>(400,"参数不合法", null);
	}

	public static <T> AckMessage<T> unauthorized() {
		return new AckMessage<T>(401,"未授权", null);
	}
	
	public static <T> AckMessage<T> error() {
		return new AckMessage<T>(500,"error", null);
	}
	
	public static <T> AckMessage<T> error(String message) {
		return new AckMessage<T>(500,message, null);
	}

	public static <T> AckMessage<T> info(String message) {
		return new AckMessage<T>(100, message,null);
	}

	public static <T> AckMessage<T> vcodeError() {
		return new AckMessage<T>(700,"验证码错误", null);
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
