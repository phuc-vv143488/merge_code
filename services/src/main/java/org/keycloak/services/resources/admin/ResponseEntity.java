package org.keycloak.services.resources.admin;

public class ResponseEntity {
	private int result;
	private String errCode;
	private String errMessage;
	public ResponseEntity(int result, String errCode, String errMessage) {
		super();
		this.result = result;
		this.errCode = errCode;
		this.errMessage = errMessage;
	}
	public ResponseEntity(int result) {
		super();
		this.result = result;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public String getErrCode() {
		return errCode;
	}
	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}
	public String getErrMessage() {
		return errMessage;
	}
	public void setErrMessage(String errMessage) {
		this.errMessage = errMessage;
	}
	public static ResponseEntity error(String errCode, String errMessage) {
		return new ResponseEntity(0, errCode, errMessage);
	}
	public static ResponseEntity success() {
		return new ResponseEntity(1);
	}
}
