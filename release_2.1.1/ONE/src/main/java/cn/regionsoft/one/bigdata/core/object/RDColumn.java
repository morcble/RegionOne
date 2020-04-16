package cn.regionsoft.one.bigdata.core.object;

import cn.regionsoft.one.bigdata.enums.DataType;
import cn.regionsoft.one.bigdata.impl.RDObject;

public class RDColumn extends RDObject{
	public RDColumn(String rdColName,DataType dataType,int seq) {
		this.name = rdColName;
		this.dataType = dataType;
		this.seq = seq;
	}
	//序号
	private int seq;
	private DataType dataType;
	public DataType getDataType() {
		return dataType;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
}
