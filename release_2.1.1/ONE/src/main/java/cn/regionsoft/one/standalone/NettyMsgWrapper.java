package cn.regionsoft.one.standalone;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

public class NettyMsgWrapper {
	private ChannelHandlerContext ctx;
	private String contextPath;
	private HttpRequest request;
	private Object msg;
	public NettyMsgWrapper(ChannelHandlerContext ctx, String contextPath, HttpRequest request, Object msg) {
		super();
		this.ctx = ctx;
		this.contextPath = contextPath;
		this.request = request;
		this.msg = msg;
	}
	public ChannelHandlerContext getCtx() {
		return ctx;
	}
	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
	public String getContextPath() {
		return contextPath;
	}
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	public HttpRequest getRequest() {
		return request;
	}
	public void setRequest(HttpRequest request) {
		this.request = request;
	}
	public Object getMsg() {
		return msg;
	}
	public void setMsg(Object msg) {
		this.msg = msg;
	}
}
