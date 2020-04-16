package cn.regionsoft.one.standalone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.ids.IDGenerator;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

public class HttpFileUploadHandler extends ChannelInboundHandlerAdapter {
    private HttpRequest currRequest;
    public HttpFileUploadHandler(){
    }
    
    private byte[] boundary = null;
    private String fileName = null;
    private boolean fileUploadFlag = false;
    private static final String FILE_CONTENT_TYPE = "multipart/form-data";
    
    //private List<ByteBuf> list = new ArrayList<ByteBuf>();
    /**
     * cached bytebuf from request, when matched boundary then flush data to item
     */
    /*private ByteBuf bufCache = ByteBufAllocator.DEFAULT.buffer(1024);
    
    private int searchStartIndex = 0;*/
    private File tmpFile = null;
    private FileOutputStream  fos = null;
    private FileChannel fileChannel = null;
    private static String tmpFileFolderPath= Constants.FILE_SERVER_ROOT+Constants.SYSTEM_SEPERATOR+"tmp"+Constants.SYSTEM_SEPERATOR;
    private void resolveIncomingByteBuf(ByteBuf incomingByteBuf){
    	if(tmpFile==null){
    		try{
    			File folder = new File(tmpFileFolderPath);
    			if(!folder.exists())folder.mkdirs();
    			fileName = String.valueOf(IDGenerator.generateSnowflakeID());
    			tmpFile = new File(tmpFileFolderPath+fileName);
    			fos  = new FileOutputStream(tmpFile);
    			fileChannel = fos.getChannel();
    		}
    		catch(Exception e){
    			e.printStackTrace();
    		}
    	}
    	if(fos!=null){
			try {
				fileChannel.write(incomingByteBuf.nioBuffer());
				incomingByteBuf.release();
				//fos.write(dst);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
    
  /*  public static void main(String[] args){
    	byte[] searched = new byte[]{1,2,3,4,5,6,3,1,5,7,8,9,10};
    	byte[] targetByte = new byte[]{1,5,7,8};
    	System.out.println(searchIndexOf(searched,targetByte));
    }
    
    private static int searchIndexOf(byte[] searched, byte[] targetByte){
    	int end = targetByte.length - 1; 
    	int searchEnd = searched.length- targetByte.length;
    	int index = 0;
    	for (; index <= searchEnd; index++){ 
    		if (targetByte[0] != searched[index] || targetByte[end] != searched[index + end]){
    			continue;
    		}
    		else{
    			if(isByteMatch(searched,targetByte,index)){
					return index;
				}
				else{
					continue;
				}
    		}
		}
    	if(index==(searchEnd+1))return-1;
    	return index;
    }
    
    private static boolean isByteMatch(byte[] src,byte[] target,int offset){
    	for(int i = offset+1,j = 1,length=target.length-1 ;j<length ; i ++ , j ++){
    		if(src[i]!=target[j])return false;
    	}
    	return true;
    }*/
    
  
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
    	
    	if (msg instanceof HttpRequest) {
    		currRequest = (HttpRequest) msg;
    		String contentType = currRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
    		if(contentType!=null&&contentType.startsWith(FILE_CONTENT_TYPE)){
    			/**
    			 * get msg boundary from content type
    			 */
    			String boundaryStr = contentType.split(";")[1].split("=")[1];
    			boundary = boundaryStr.getBytes(HttpServer.REQUEST_ENCODING.name());
    			fileUploadFlag = true;
    		}
    		else{
    			ctx.fireChannelRead(msg);
    		}
    	}
    	else{
    		if(fileUploadFlag){
    			if (msg instanceof HttpContent) {
    				if (msg instanceof LastHttpContent) {
    					resolveIncomingByteBuf(((DefaultLastHttpContent) msg).content());
    					
    					UploadMsg uploadMsg = new UploadMsg(boundary,fileName,currRequest);
    					
    					
    					CommonUtil.closeQuietly(fileChannel);
						CommonUtil.closeQuietly(fos);
						
    					reset();
    					ctx.fireChannelRead(uploadMsg);
    	    		}
    	    		else{
    	    			resolveIncomingByteBuf(((DefaultHttpContent) msg).content());

    	    		}
    			}
    		}
    		else{
    			ctx.fireChannelRead(msg);
    		}
    	}
    }
    
    private void reset(){
    	tmpFile = null;
		fos = null;
		fileChannel = null;
		fileUploadFlag = false;
		boundary = null;
	    fileName = null;
    }
  
}
