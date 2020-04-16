package cn.regionsoft.one.rpc.common.to;

import cn.regionsoft.one.core.CommonUtil;

public class StringIDDto {
	private String id;
	
	private Integer version;

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
