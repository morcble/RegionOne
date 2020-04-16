package cn.regionsoft.one.rpc.common.to;

import cn.regionsoft.one.core.CommonUtil;

public class LongIDDto {
	private Long id;
	
	private Integer version;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	} 
	
	public String toString(){
	 return CommonUtil.instanceToString(this,false);
	}
}
