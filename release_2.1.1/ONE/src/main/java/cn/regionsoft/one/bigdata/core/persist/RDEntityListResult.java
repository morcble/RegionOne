package cn.regionsoft.one.bigdata.core.persist;

import java.util.List;

public class RDEntityListResult {
	private List<RDEntity> list;
	private String nextStartRowKey;
	public List<RDEntity> getList() {
		return list;
	}
	public void setList(List<RDEntity> list) {
		this.list = list;
	}
	public String getNextStartRowKey() {
		return nextStartRowKey;
	}
	public void setNextStartRowKey(String nextStartRowKey) {
		this.nextStartRowKey = nextStartRowKey;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(list !=null) {
			for(RDEntity rdEntity:list) {
				sb.append(rdEntity.toString());
				sb.append("\r\n");
			}
		}
		return sb.toString();
	}
}
