package cn.regionsoft.one.caches.bloomfilter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface BloomObject {
	/**
	 * 更新和查询的业务主键,和缓存相关
	 * @return
	 */
	@JsonIgnore
	public String getBloomKey();
}
