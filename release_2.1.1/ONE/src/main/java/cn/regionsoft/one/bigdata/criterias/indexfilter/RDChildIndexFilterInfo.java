package cn.regionsoft.one.bigdata.criterias.indexfilter;

import java.util.List;

import cn.regionsoft.one.bigdata.criterias.RDFilterType;

public class RDChildIndexFilterInfo implements RDIndexFilterInfo{
	private RDFilterType rdFilterType;
	private String columnName;
	private String compareStr;
	
	private String startRowKey;
	private String stopRowKey;
	private String rowPrefix;
	
	private RDFilterType complexFilterType;//AND OR

	public String getRowPrefix() {
		return rowPrefix;
	}
	public void setRowPrefix(String rowPrefix) {
		this.rowPrefix = rowPrefix;
	}
	public RDFilterType getRdFilterType() {
		return rdFilterType;
	}
	public void setRdFilterType(RDFilterType rdFilterType) {
		this.rdFilterType = rdFilterType;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getCompareStr() {
		return compareStr;
	}
	public void setCompareStr(String compareStr) {
		this.compareStr = compareStr;
	}
	public RDFilterType getComplexFilterType() {
		return complexFilterType;
	}
	public void setComplexFilterType(RDFilterType complexFilterType) {
		this.complexFilterType = complexFilterType;
	}
	public String getStartRowKey() {
		return startRowKey;
	}
	public void setStartRowKey(String startRowKey) {
		this.startRowKey = startRowKey;
	}
	public String getStopRowKey() {
		return stopRowKey;
	}
	public void setStopRowKey(String stopRowKey) {
		this.stopRowKey = stopRowKey;
	}

}
