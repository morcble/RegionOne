package cn.regionsoft.one.standalone;

import io.netty.handler.codec.http.HttpRequest;

public class UploadMsg {
	public UploadMsg(byte[] boundary, String fileName, HttpRequest currRequest) {
		super();
		this.boundary = boundary;
		this.fileName = fileName;
		this.currRequest = currRequest;
	}
	private HttpRequest currRequest = null;
	private byte[] boundary = null;
    private String fileName = null;
	public HttpRequest getCurrRequest() {
		return currRequest;
	}
	public byte[] getBoundary() {
		return boundary;
	}
	public String getFileName() {
		return fileName;
	}
}
