package cn.regionsoft.one.bigdata.core.object;

import cn.regionsoft.one.bigdata.impl.RDConstants;
import cn.regionsoft.one.bigdata.impl.RDObject;

//schema-table1
public class RDSchema extends RDObject{
	private int seq;
	private String appName;
	private String userId;
	//表名，序号
	
	public RDSchema(String userId,String appName,String rdSchemaName,int seq) {
		this.userId = userId;
		this.appName = appName;
		this.name = rdSchemaName;
		this.seq = seq;
	}

	public int getSeq() {
		return seq;
	}

	

	public String getAppName() {
		return appName;
	}

	public String getUserId() {
		return userId;
	}
}
