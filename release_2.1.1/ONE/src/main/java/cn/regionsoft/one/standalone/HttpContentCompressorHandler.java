package cn.regionsoft.one.standalone;

import java.util.HashSet;

import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
/**
 * http compress
 * @author fenglj
 *
 */
public class HttpContentCompressorHandler extends HttpContentCompressor{
	private HashSet<String> compressedMineType = new HashSet<String>();
	public HttpContentCompressorHandler(){
		compressedMineType.add("application/json");
		compressedMineType.add("application/text");
	}
	
	
	@Override
    protected Result beginEncode(HttpResponse reponse, String acceptEncoding) throws Exception {
		//System.out.println(reponse.headers().get(HttpHeaderNames.CONTENT_TYPE));
		if(compressedMineType.contains(reponse.headers().get(HttpHeaderNames.CONTENT_TYPE).toLowerCase())){
			return super.beginEncode(reponse, acceptEncoding);
		}
		else{//can not compress picture
			return null;
		}
        
	}

}
