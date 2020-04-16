package cn.regionsoft.one.bigdata.core.object;

import cn.regionsoft.one.bigdata.impl.RDObject;

public class RDIndex extends RDObject{
	public RDIndex(String indexName,int seq) {
		this.name = indexName;
		this.seq = seq;
	}
	private int seq;
	
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
}
