package cn.regionsoft.one.bigdata.criterias.indexfilter;

import cn.regionsoft.one.bigdata.criterias.RDFilterType;
import cn.regionsoft.one.bigdata.impl.RDConstants;

public class RDRootIndexFilterInfo implements RDIndexFilterInfo{
	private RDFilterType rdFilterType;
	private String compareStr;
	private String rowPrefix;
	
	private String startRowKey;
	private String stopRowKey;

	
	private RDFilterType complexFilterType;//AND OR
	public RDFilterType getRdFilterType() {
		return rdFilterType;
	}
	public void setRdFilterType(RDFilterType rdFilterType) {
		this.rdFilterType = rdFilterType;
	}
	public String getCompareStr() {
		return compareStr;
	}
	public void setCompareStr(String compareStr) {
		this.compareStr = compareStr;
	}
	public String getRowPrefix() {
		if(rowPrefix==null||rowPrefix.equals("")) {
			if(rdFilterType==RDFilterType.EQUAL) {
				rowPrefix = compareStr;
			}
			else if(rdFilterType==RDFilterType.REGEX) {
				rowPrefix = startRowKey;
			}
		}
		return rowPrefix;
	}
	public void setRowPrefix(String rowPrefix) {
		this.rowPrefix = rowPrefix;
	}
	public String getStartRowKey() {
		return startRowKey;
	}
	public void setStartRowKey(String startRowKey) {
		this.startRowKey = startRowKey;
	}
	public String getStopRowKey() {
		if(stopRowKey==null) {
			if(rdFilterType==RDFilterType.REGEX) {
				stopRowKey = startRowKey + RDConstants.RD_END_SUFFIX;
			}
		}
		return stopRowKey;
	}
	public void setStopRowKey(String stopRowKey) {
		this.stopRowKey = stopRowKey;
	}
	public RDFilterType getComplexFilterType() {
		return complexFilterType;
	}
	public void setComplexFilterType(RDFilterType complexFilterType) {
		this.complexFilterType = complexFilterType;
	}
}
