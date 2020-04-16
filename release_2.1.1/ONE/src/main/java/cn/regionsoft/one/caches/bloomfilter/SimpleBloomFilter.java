package cn.regionsoft.one.caches.bloomfilter;

import java.nio.charset.Charset;

import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.core.CommonUtil;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

public class SimpleBloomFilter {
	//1千万数据容量
	private static int size = 10000000;
 
    private BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.forName(Constants.UTF8)), size);
    
    public boolean mightExsit(BloomObject bloomObject) {
    	if(bloomObject==null)return false;
    	String bloomKey = bloomObject.getBloomKey();
    	if(CommonUtil.isEmpty(bloomKey))return true;
    	return bloomFilter.mightContain(bloomKey);
    }
    
    public void put(BloomObject bloomObject) {
    	String boomKey = bloomObject.getBloomKey();
    	if(bloomObject==null||CommonUtil.isEmpty(boomKey))return;
    	bloomFilter.put(boomKey);
    	
    	/*bloomFilter.readFrom(in, funnel)
    	bloomFilter.writeTo(out);*/
    }
}
