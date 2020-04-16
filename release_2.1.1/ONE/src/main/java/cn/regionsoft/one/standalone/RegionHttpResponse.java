package cn.regionsoft.one.standalone;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import cn.regionsoft.one.core.CommonUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

public class RegionHttpResponse implements HttpServletResponse{
	private List<Cookie> cookieLs = null;
	private FullHttpResponse fullHttpResponse;
	private Channel channel;
	private HttpRequest request;

	@Override
	public void addCookie(Cookie cookie) {
		if(cookieLs==null)cookieLs =new ArrayList<Cookie>();
		cookieLs.add(cookie);
	}
	
	public void setFullHttpResponse(FullHttpResponse fullHttpResponse) {
		this.fullHttpResponse = fullHttpResponse;
	}
	

	public void setChannelHandlerContext(Channel channel) {
		this.channel = channel;
	}
	

	public void setRequest(HttpRequest request) {
		this.request = request;
	}
	
	
	@Override
	public void setHeader(String name, String value) {
		fullHttpResponse.headers().set(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		fullHttpResponse.headers().add(name, value);
	}
	
	private boolean completed = false;
	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	/**
	 * handle file download and view
	 * @param realFilePath
	 * @param viewMode
	 * @param fileName
	 */
	public void handleFile(String realFilePath,boolean viewMode,String fileName) {
		if(realFilePath==null){
			sendError(channel,HttpResponseStatus.NOT_FOUND);
			return;
		}
		RandomAccessFile raf;
		try {
			File file = new File(realFilePath);
			
			if(!file.exists()||file.isDirectory()){
				sendError(channel,HttpResponseStatus.NOT_FOUND);
				return;
			}
//			String lastSince = request.headers().get("If-Modified-Since");
//            if(!CommonUtil.isEmpty(lastSince)) {
//            	try {
//					long lastSinceTime = sdf.parse(lastSince).getTime();
//					if(file.lastModified() == lastSinceTime) {
//						sendError(channel,HttpResponseStatus.NOT_MODIFIED);
//						return;
//					}
//				} catch (ParseException e) {
//					
//					e.printStackTrace();
//				}
//            }
			
			
			if(fileName==null)fileName = file.getName();
			raf = new RandomAccessFile(file, "r");
			long fileLength = raf.length();
			DefaultHttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
			HttpUtil.setContentLength(response, fileLength);
			setContentTypeHeader(response, file);
			response.headers().add("Date", new Date());
			response.headers().add("Transfer-Encoding", "chunked");
			response.headers().add("Server", "RegionSoft/V0.5.1.2");
			
			response.headers().add("last-modified", new Date(file.lastModified()));
			
			
			if (viewMode) {
				response.headers().add("Content-Disposition", "inline; filename=" + file.getName());
			} else {
				try {
					response.headers().add("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName,"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					try {
						response.headers().add("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName,"UTF-16"));
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
				}
			}
			
			if (HttpUtil.isKeepAlive(request)) {
				response.headers().set("CONNECTION", HttpHeaderValues.KEEP_ALIVE);
			}
			// Write the initial line and the header.
			channel.write(response);
			
			ChannelFuture sendFileFuture;
			if (channel.pipeline().get(SslHandler.class) == null) {
				sendFileFuture = channel.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), channel.newProgressivePromise());
			} else {
				sendFileFuture = channel.write(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)), channel.newProgressivePromise());
			}

			
			sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
				@Override
				public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
					if (total < 0) { // total unknown
						//System.err.println(future.channel() + " Transfer progress: " + progress);
					} else {
						//System.err.println(future.channel() + " Transfer progress: " + progress + " / " + total);
					}
				}

				@Override
				public void operationComplete(ChannelProgressiveFuture future) {
					//System.err.println(future.channel() + " Transfer complete.");
				}
			});
			
			// Write the end marker
			ChannelFuture lastContentFuture = channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

			// Decide whether to close the connection or not.
			if (!HttpUtil.isKeepAlive(request)) {
				// Close the connection when the whole content is written out.
				lastContentFuture.addListener(ChannelFutureListener.CLOSE);
				//ctx.close();
			}

		} catch (FileNotFoundException ignore) {
			sendError(channel, NOT_FOUND);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}

		completed = true;
		
	}
	


	private static void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		String a = mimeTypesMap.getContentType(file.getPath());
		response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
	}

	@Override
	public void setContentType(String type) {
		fullHttpResponse.headers().set(CONTENT_TYPE, type);
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		
		return null;
	}

	public List<Cookie> getCookieLs() {
		return cookieLs;
	}

	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public PrintWriter getWriter() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentLength(int len) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentLengthLong(long len) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setBufferSize(int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetBuffer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocale(Locale loc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public boolean containsHeader(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String encodeURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeRedirectUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendError(int sc) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		this.setStatus(302);
		this.setHeader("Location",location);
	}

	@Override
	public void setDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void setIntHeader(String name, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addIntHeader(String name, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStatus(int sc) {
		// TODO Auto-generated method stub
		fullHttpResponse.setStatus(new HttpResponseStatus(sc, ""));
	}

	@Override
	public void setStatus(int sc, String sm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}


	private static void sendError(Channel channel, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		// Close the connection as soon as the error message is sent.
		channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}


	

}
