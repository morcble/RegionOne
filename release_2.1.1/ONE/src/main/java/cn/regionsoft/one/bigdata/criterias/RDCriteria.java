package cn.regionsoft.one.bigdata.criterias;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;

import cn.regionsoft.one.bigdata.core.exceptions.NotFoundException;
import cn.regionsoft.one.bigdata.core.object.RDColumn;
import cn.regionsoft.one.bigdata.core.object.RDIndex;
import cn.regionsoft.one.bigdata.core.object.RDTable;
import cn.regionsoft.one.bigdata.criterias.indexfilter.RDChildIndexFilterInfo;
import cn.regionsoft.one.bigdata.criterias.indexfilter.RDIndexFilterInfo;
import cn.regionsoft.one.bigdata.criterias.indexfilter.RDRootIndexFilterInfo;
import cn.regionsoft.one.bigdata.enums.ConditionType;
import cn.regionsoft.one.bigdata.enums.DataType;
import cn.regionsoft.one.bigdata.impl.RDConstants;

public class RDCriteria {
	private RDCriteria() {}
	
	private RDCondition rdCondition;
	
	public RDCondition getRdCondition() {
		return rdCondition;
	}

	public void setRdCondition(RDCondition rdCondition) {
		this.rdCondition = rdCondition;
	}

	public static RDCriteria create(RDCondition rdCondition) {
		RDCriteria rdCriteria = new RDCriteria();
		rdCriteria.setRdCondition(rdCondition);
		return rdCriteria;
	}
	
	public FilterList toFilterList(RDTable rdTable) throws Exception {
		if(rdCondition==null) return null;
		else {
			this.optimize(rdTable);
			return subToFilterList(rdCondition,rdTable,null);
		}
	}
	
	//?static
	private static FilterList subToFilterList(RDCondition rdCondition,RDTable rdTable,FilterList parentlist) throws Exception {
		if(rdCondition.getContitionType()==ConditionType.AND) {
			if(rdCondition.getChildConditions()==null||rdCondition.getChildConditions().length==0) {
				return null;
			}
			
			
			FilterList result = null;
			FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ALL);
			if(parentlist==null) {
				result = list;
			}
			else {
				parentlist.addFilter(list);
				result = parentlist;
			}
			
			for(RDCondition childCondition:rdCondition.getChildConditions()) {
				subToFilterList(childCondition,rdTable,list);
			}
			return result;
		}
		else if(rdCondition.getContitionType()==ConditionType.OR) {
			if(rdCondition.getChildConditions()==null||rdCondition.getChildConditions().length==0) {
				return null;
			}
			
			
			FilterList result = null;
			FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ONE); 
			if(parentlist==null) {
				result = list;
			}
			else {
				parentlist.addFilter(list);
				result = parentlist;
			}
			
			for(RDCondition childCondition:rdCondition.getChildConditions()) {
				subToFilterList(childCondition,rdTable,list);
			}
			return result;
		}
		
		//单条件
		FilterList result = parentlist;
		if(result==null)throw new Exception("condition config error");
		RDCondition.Para para = rdCondition.getPara();
		RDColumn rdColumn = rdTable.getRdColumn(para.getKey());
		if(rdColumn==null) {
			throw new NotFoundException("column not exists:"+para.getKey()+" in table "+rdTable.getName());
		}
		
		if(rdCondition.getContitionType()==ConditionType.EQUAL
				||rdCondition.getContitionType()==ConditionType.GT
				||rdCondition.getContitionType()==ConditionType.LT) {
			
			CompareOp compareOp = null;
			if(rdCondition.getContitionType()==ConditionType.EQUAL)compareOp=CompareOp.EQUAL;
			if(rdCondition.getContitionType()==ConditionType.GT)compareOp=CompareOp.GREATER;
			if(rdCondition.getContitionType()==ConditionType.LT)compareOp=CompareOp.LESS;
			
			SingleColumnValueFilter tmpFilter = null;
			
			if(rdColumn.getDataType()==DataType.STRING) {
				tmpFilter = new SingleColumnValueFilter(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0),Bytes.toBytes(rdColumn.getSeq()),compareOp,Bytes.toBytes((String)para.getValue()));
			}
			else if(rdColumn.getDataType()==DataType.INT) {
				int val = 0;
				if(para.getValue() instanceof Integer) {
					val = ((Integer)para.getValue()).intValue();
				}
				else if(para.getValue() instanceof String) {
					val = Integer.valueOf((String)para.getValue());
				}
				tmpFilter = new SingleColumnValueFilter(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0),Bytes.toBytes(rdColumn.getSeq()),compareOp,Bytes.toBytes(val));
			}
			else if(rdColumn.getDataType()==DataType.LONG) {
				long val = 0;
				if(para.getValue() instanceof Long) {
					val = ((Long)para.getValue()).longValue();
				}
				else if(para.getValue() instanceof String) {
					val = Long.valueOf((String)para.getValue());
				}
				tmpFilter = new SingleColumnValueFilter(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0),Bytes.toBytes(rdColumn.getSeq()),compareOp,Bytes.toBytes(val));
			}
			else if(rdColumn.getDataType()==DataType.FLOAT) {
				float val = 0;
				if(para.getValue() instanceof Float) {
					val = ((Float)para.getValue()).floatValue();
				}
				else if(para.getValue() instanceof String) {
					val = Float.valueOf((String)para.getValue());
				}
				tmpFilter = new SingleColumnValueFilter(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0),Bytes.toBytes(rdColumn.getSeq()),compareOp,Bytes.toBytes(val));

			}
			else if(rdColumn.getDataType()==DataType.DOUBLE) {
				double val = 0;
				if(para.getValue() instanceof Double) {
					val = ((Double)para.getValue()).doubleValue();
				}
				else if(para.getValue() instanceof String) {
					val = Double.valueOf((String)para.getValue());
				}
				tmpFilter = new SingleColumnValueFilter(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0),Bytes.toBytes(rdColumn.getSeq()),compareOp,Bytes.toBytes(val));
			}
			else if(rdColumn.getDataType()==DataType.BOOLEAN) {
				boolean val = false;
				if(para.getValue() instanceof Boolean) {
					val = ((Boolean)para.getValue()).booleanValue();
				}
				else if(para.getValue() instanceof String) {
					val = Boolean.valueOf((String)para.getValue());
				}
				tmpFilter = new SingleColumnValueFilter(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0),Bytes.toBytes(rdColumn.getSeq()),compareOp,Bytes.toBytes(val));
			}
			else if(rdColumn.getDataType()==DataType.DATE) {
				long val = 0;
				if(para.getValue() instanceof Date) {
					val = ((Date)para.getValue()).getTime();
				}
				if(para.getValue() instanceof Long) {
					val = ((Long)para.getValue()).longValue();
				}
				else {
					val = (long) para.getValue();
				}
				tmpFilter = new SingleColumnValueFilter(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0),Bytes.toBytes(rdColumn.getSeq()),compareOp,Bytes.toBytes(val));
			}
			else if(rdColumn.getDataType()==DataType.BIGDECIMAL) {
				tmpFilter = new SingleColumnValueFilter(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0),Bytes.toBytes(rdColumn.getSeq()),compareOp,Bytes.toBytes((BigDecimal)para.getValue()));
			}

			result.addFilter(tmpFilter);
		}
		else if(rdCondition.getContitionType()==ConditionType.REGEX) {
			if(rdColumn.getDataType()==DataType.STRING) {
				RegexStringComparator comparator = new RegexStringComparator((String)para.getValue());
				SingleColumnValueFilter tmpFilter = new SingleColumnValueFilter(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0),Bytes.toBytes(rdColumn.getSeq()),CompareOp.EQUAL,comparator);
				result.addFilter(tmpFilter);
			}
		}
		else if(rdCondition.getContitionType()==ConditionType.CONTAIN) {
			if(rdColumn.getDataType()==DataType.STRING) {
				SubstringComparator comparator = new SubstringComparator((String)para.getValue());
				SingleColumnValueFilter tmpFilter = new SingleColumnValueFilter(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0),Bytes.toBytes(rdColumn.getSeq()),CompareOp.EQUAL,comparator);
				result.addFilter(tmpFilter);
			}
		}
		else if(rdCondition.getContitionType()==ConditionType.START_WITH) {
			if(rdColumn.getDataType()==DataType.STRING) {
				RegexStringComparator comparator = new RegexStringComparator((String)para.getValue());
				SingleColumnValueFilter tmpFilter = new SingleColumnValueFilter(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0),Bytes.toBytes(rdColumn.getSeq()),CompareOp.EQUAL,comparator);
				result.addFilter(tmpFilter);
			}
		}
		else if(rdCondition.getContitionType()==ConditionType.END_WITH) {
			if(rdColumn.getDataType()==DataType.STRING) {
				RegexStringComparator comparator = new RegexStringComparator((String)para.getValue());
				SingleColumnValueFilter tmpFilter = new SingleColumnValueFilter(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0),Bytes.toBytes(rdColumn.getSeq()),CompareOp.EQUAL,comparator);
				result.addFilter(tmpFilter);
			}
		}
		return result;
	}
	
	public RDRootIndexFilterInfo getIndexFilter(RDTable rdTable) throws Exception {
		Map<String,RDIndex> rdIndexes = rdTable.getRdIndexes();
		if(rdIndexes==null||rdIndexes.size()==0)return null;
		
		//列的真实名字 及SEQ map
		this.optimize(rdTable);
		LinkedHashMap<String,Integer> indexColumnRealNames = new LinkedHashMap<String,Integer>();
		RDIndex rdIndex = getRDIndex(this.rdCondition, rdTable,indexColumnRealNames);
		if(rdIndex==null) {
			System.err.println("No index found for data search operation");
			return null;
		}
		
		RDRootIndexFilterInfo rdIndexFilterInfo = this.getIndexFilterRegex(rdTable,rdIndex, indexColumnRealNames);
		return rdIndexFilterInfo;
	}

	
	private RDRootIndexFilterInfo getIndexFilterRegex(RDTable rdTable,RDIndex rdIndex,LinkedHashMap<String,Integer> indexColumnRealNames) throws Exception {
		String rowPrefix = rdTable.getRdSchema().getUserId()+RDConstants._STR
				+rdTable.getRdSchema().getSeq() + RDConstants._STR + rdTable.getSeq() 
				+ RDConstants._STR + RDConstants.RD_INDEX_STR + RDConstants._STR +RDConstants.RD_DATA_STR
				+ RDConstants._STR + rdIndex.getSeq()+ RDConstants._STR ;
		//SAMPLE_0_INDEX_DATA_0_feng0_password0_4276725086617600
		RDRootIndexFilterInfo rdIndexFilterInfo = (RDRootIndexFilterInfo) RDCriteria.getRDIndexFilterInfo(rdTable,this.rdCondition,rowPrefix,indexColumnRealNames,true);

		System.out.println("CompareStr"+rdIndexFilterInfo.getCompareStr());
		System.out.println("rdIndexFilterInfo.getRowPrefix()="+rdIndexFilterInfo.getRowPrefix());
		System.out.println("rdIndexFilterInfo.getRdFilterType()="+rdIndexFilterInfo.getRdFilterType().name());
		System.out.println("RDCriteria-259:rdIndexFilterInfo.getStartRowKey()="+rdIndexFilterInfo.getStartRowKey());
		System.out.println("rdIndexFilterInfo.getStopRowKey()="+rdIndexFilterInfo.getStopRowKey());
		return rdIndexFilterInfo;
	}
	
	private static RDIndexFilterInfo getRDIndexFilterInfo(RDTable rdTable,RDCondition rdCondition,
			String rowPrefix,LinkedHashMap<String,Integer> indexColumnRealNames,boolean isRootNode) throws Exception {
		if(rdCondition.getContitionType()==ConditionType.AND || rdCondition.getContitionType()==ConditionType.OR) {
			if(rdCondition.getChildConditions()!=null) {
					RDChildIndexFilterInfo[] childIndexFilterInfoArray = new RDChildIndexFilterInfo[indexColumnRealNames.size()];
					//子聚合条件
					List<RDChildIndexFilterInfo> complexFilterInfoLs = null;
					Map<String,RDChildIndexFilterInfo> childIndexFilterInfoListMap = new HashMap<String,RDChildIndexFilterInfo>();
					for(RDCondition childCondition : rdCondition.getChildConditions()) {
						RDChildIndexFilterInfo rdChildIndexFilterInfo = (RDChildIndexFilterInfo) 
								RDCriteria.getRDIndexFilterInfo(rdTable, childCondition,rowPrefix,indexColumnRealNames,false);
						if(rdChildIndexFilterInfo.getComplexFilterType()==null) {
							childIndexFilterInfoListMap.put(rdChildIndexFilterInfo.getColumnName(),rdChildIndexFilterInfo);
						}
						else {
							//聚合条件
							if(complexFilterInfoLs==null) {
								complexFilterInfoLs = new ArrayList<RDChildIndexFilterInfo>();
								complexFilterInfoLs.add(rdChildIndexFilterInfo);
							}
							else {
								complexFilterInfoLs.add(rdChildIndexFilterInfo);
							}
						}
					}
					
					Iterator<Entry<String,Integer>> iterator = indexColumnRealNames.entrySet().iterator();
					int index = 0;
					Entry<String,Integer> tmpEntry = null;
					RDChildIndexFilterInfo tmpChildRDIndexFilterInfo = null;
					boolean hasSimpleFilter = false;
					while(iterator.hasNext()) {
						tmpEntry = iterator.next();
						tmpChildRDIndexFilterInfo = childIndexFilterInfoListMap.get(tmpEntry.getKey());
						childIndexFilterInfoArray[index] = tmpChildRDIndexFilterInfo;
						if(tmpChildRDIndexFilterInfo!=null) {
							hasSimpleFilter = true;
						}
						index++;
					}
				
				
				
					RDFilterType resultRdFilterType = null;
					String resultStartRowKey = "";
					String resultStopRowKey = "";
					String resultCompareStr = "";
					String resultRowPrefix = "";
					RDFilterType currentRdFilterType = null;
					
					if(hasSimpleFilter&&childIndexFilterInfoArray[0] ==null) {
						resultRdFilterType = RDFilterType.REGEX;
						resultStartRowKey = rowPrefix;
					}
					for(int i = 0 ; hasSimpleFilter && i < childIndexFilterInfoArray.length; i++) {
						tmpChildRDIndexFilterInfo = childIndexFilterInfoArray[i];
						String tmpCompareStr = null;
						if(tmpChildRDIndexFilterInfo==null) {//一行的列数比索引列数少
							tmpCompareStr = ".*";
							resultRdFilterType = RDFilterType.REGEX;
							resultCompareStr = resultCompareStr + RDConstants._STR  + tmpCompareStr;
							continue;
						}
						else {
							currentRdFilterType = tmpChildRDIndexFilterInfo.getRdFilterType();
							tmpCompareStr = tmpChildRDIndexFilterInfo.getCompareStr();
						}
						
						if(currentRdFilterType == RDFilterType.EQUAL) {
							if(resultRdFilterType==null) {//第一个column
								resultRdFilterType = currentRdFilterType;
								resultStartRowKey = rowPrefix + tmpCompareStr + RDConstants._STR;
								resultStopRowKey = resultStartRowKey + RDConstants.RD_END_SUFFIX;
								resultCompareStr = rowPrefix + tmpCompareStr;
							}
							else if(resultRdFilterType==RDFilterType.EQUAL) {
								resultCompareStr = resultCompareStr + RDConstants._STR + tmpCompareStr;
								resultStopRowKey = resultCompareStr + RDConstants._STR + RDConstants.RD_END_SUFFIX;
								resultStartRowKey = resultCompareStr;
							}
							else if(resultRdFilterType==RDFilterType.START_WITH) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr + RDConstants._STR + tmpCompareStr;
								resultStopRowKey = resultStartRowKey + RDConstants.RD_END_SUFFIX;
							}
							else if(resultRdFilterType==RDFilterType.STOP_WITH) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr + RDConstants._STR + ".*" +tmpCompareStr;
								resultStopRowKey = resultCompareStr + RDConstants._STR + tmpCompareStr;
							}
							else if(resultRdFilterType==RDFilterType.REGEX) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr + RDConstants._STR + tmpCompareStr;
								//resultCompareStr = resultCompareStr.replace("__", "_");
							}
							else if(resultRdFilterType==RDFilterType.GT) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = rowPrefix + ".*"+RDConstants._STR+tmpCompareStr;
								resultStopRowKey = rowPrefix+ RDConstants.RD_END_SUFFIX;
							}
						}
						else if(currentRdFilterType == RDFilterType.START_WITH) {
							if(resultRdFilterType==null) {//第一个column
								resultRdFilterType = currentRdFilterType;
								resultRowPrefix = rowPrefix + tmpCompareStr;
								resultStartRowKey = rowPrefix + tmpCompareStr;
								resultStopRowKey = resultStartRowKey + RDConstants.RD_END_SUFFIX;
								resultCompareStr = resultStartRowKey+ ".*";
							}
							else if(resultRdFilterType==RDFilterType.EQUAL) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr + RDConstants._STR+ tmpCompareStr +".*" ;
								resultStartRowKey+=tmpCompareStr;
								resultStopRowKey = resultStartRowKey + RDConstants.RD_END_SUFFIX;
							}
							else if(resultRdFilterType==RDFilterType.START_WITH) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr + RDConstants._STR + tmpCompareStr+ ".*";
								resultStopRowKey = null;
							}
							else if(resultRdFilterType==RDFilterType.STOP_WITH) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr+ RDConstants._STR + tmpCompareStr+ ".*";
								resultStopRowKey = null;
							}
							else if(resultRdFilterType==RDFilterType.REGEX) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr + RDConstants._STR + tmpCompareStr+ ".*";
								//resultCompareStr = resultCompareStr.replace("__", "_");
							}
							else if(resultRdFilterType==RDFilterType.GT) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = rowPrefix + ".*"+RDConstants._STR+tmpCompareStr+".*";
								resultStopRowKey = rowPrefix+ RDConstants.RD_END_SUFFIX;
							}
						}
						else if(currentRdFilterType == RDFilterType.STOP_WITH) {
							if(resultRdFilterType==null) {//第一个column
								resultRdFilterType = RDFilterType.REGEX;
								resultStartRowKey = rowPrefix;
								resultStopRowKey = rowPrefix +tmpCompareStr;
								resultCompareStr = rowPrefix + ".*";
							}
							else if(resultRdFilterType==RDFilterType.EQUAL) {
								resultRdFilterType = RDFilterType.START_WITH;
								resultRowPrefix = biggerStr(resultRowPrefix, resultCompareStr+RDConstants._STR);
								resultStopRowKey = resultCompareStr + RDConstants._STR + tmpCompareStr;
							}
							else if(resultRdFilterType==RDFilterType.START_WITH) {
								resultRdFilterType = RDFilterType.REGEX;
								resultRowPrefix = biggerStr(resultRowPrefix, resultCompareStr+RDConstants._STR);
								//resultStopRowKey = resultCompareStr + RDConstants.RD_SEPARATOR + tmpCompareStr;
							}
							else if(resultRdFilterType==RDFilterType.GT) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr += ".*" + RDConstants._STR + ".*";
								//resultStopRowKey = resultCompareStr + RDConstants.RD_SEPARATOR + tmpCompareStr;
							}
							else if(resultRdFilterType==RDFilterType.REGEX) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr += RDConstants._STR + ".*";
								//resultStopRowKey = resultCompareStr + RDConstants.RD_SEPARATOR + tmpCompareStr;
							}
							else {
								resultStopRowKey = null;
							}
						}
						else if(currentRdFilterType == RDFilterType.REGEX) {
							resultStopRowKey = null;//正则没有stoprowkey
							if(resultRdFilterType==null) {//第一个column
								resultRdFilterType = currentRdFilterType;
								resultStartRowKey = rowPrefix;
								resultCompareStr = rowPrefix + tmpCompareStr;
							}
							else if(resultRdFilterType==RDFilterType.EQUAL) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr + RDConstants._STR + tmpCompareStr;
							}
							else if(resultRdFilterType==RDFilterType.START_WITH) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr + RDConstants._STR + tmpCompareStr;
							}
							else if(resultRdFilterType==RDFilterType.STOP_WITH) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr + RDConstants._STR + ".*" +tmpCompareStr;
							}
							else if(resultRdFilterType==RDFilterType.REGEX) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr + RDConstants._STR +tmpCompareStr;
							}
						}
						else if(currentRdFilterType==RDFilterType.GT) {
							if(resultRdFilterType==null) {//第一个column
								resultRdFilterType = currentRdFilterType;
								resultRowPrefix = rowPrefix;
								resultStopRowKey = rowPrefix + RDConstants.RD_END_SUFFIX;
								resultStartRowKey = rowPrefix + tmpCompareStr;
								resultCompareStr = rowPrefix;
							}
							else if(resultRdFilterType==RDFilterType.EQUAL) {
								resultRdFilterType = RDFilterType.GT;
								resultRowPrefix = biggerStr(resultRowPrefix,resultCompareStr);
								resultStopRowKey = resultCompareStr + RDConstants._STR + RDConstants.RD_END_SUFFIX;
								resultCompareStr = resultCompareStr + RDConstants._STR + tmpCompareStr;
								resultStartRowKey = resultCompareStr;	 
							}
							else if(resultRdFilterType==RDFilterType.START_WITH) {
								resultRdFilterType = RDFilterType.REGEX;
								resultRowPrefix = rowPrefix;
								resultStopRowKey = resultStartRowKey + RDConstants.RD_END_SUFFIX;
								resultCompareStr = resultCompareStr + RDConstants._STR + ".*";
							}
							else if(resultRdFilterType==RDFilterType.STOP_WITH) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr + RDConstants._STR + ".*" +tmpCompareStr;
							}
							else if(resultRdFilterType==RDFilterType.REGEX) {
								resultRdFilterType = RDFilterType.REGEX;
								resultCompareStr = resultCompareStr + RDConstants._STR +".*";
							}
							else if(resultRdFilterType==RDFilterType.GT) {
								resultRdFilterType = RDFilterType.REGEX;
								resultStopRowKey = rowPrefix+RDConstants.RD_END_SUFFIX;
								resultCompareStr = rowPrefix + ".*"+RDConstants._STR+".*";
								
							}
						}
					}
					
					if(complexFilterInfoLs!=null && complexFilterInfoLs.size()>0) {
						String complexCompareStr = resultCompareStr;				
						for(RDChildIndexFilterInfo complexFilterInfo:complexFilterInfoLs) {
							if(rdCondition.getContitionType()==ConditionType.AND) {
								resultStartRowKey = biggerStr(complexFilterInfo.getStartRowKey(),resultStartRowKey);
								resultStopRowKey = smallerStr(complexFilterInfo.getStopRowKey(),resultStopRowKey);
							}
							else {
								resultStartRowKey = smallerStr(complexFilterInfo.getStartRowKey(),resultStartRowKey);
								resultStopRowKey = biggerStr(complexFilterInfo.getStopRowKey(),resultStopRowKey);
							}
							
//							if(complexCompareStr==null) {
//								complexCompareStr = complexFilterInfo.getCompareStr()+"_.*";
//								continue;
//							}
//							
							if(complexFilterInfo.getRdFilterType()==RDFilterType.EQUAL||complexFilterInfo.getRdFilterType()==RDFilterType.REGEX
									||complexFilterInfo.getRdFilterType()==RDFilterType.START_WITH) {
								if(rdCondition.getContitionType()==ConditionType.AND) {
									complexCompareStr = "(?=("+ complexCompareStr + ")(" + complexFilterInfo.getCompareStr()+ "))";
								}
								else {
									if(complexCompareStr.equals("")) {
										complexCompareStr = complexFilterInfo.getCompareStr();
									}
									else {
										complexCompareStr = "("+ complexCompareStr + ")|(" + complexFilterInfo.getCompareStr()+ ")";
									}
									
								}
							}
							else if(complexFilterInfo.getRdFilterType()==RDFilterType.STOP_WITH) {
								//do nothing
							}
						}
						
//						if(!resultCompareStr.equals("") && complexCompareStr!=null) {
//							resultCompareStr += "_.*";
//							if(rdCondition.getContitionType()==ConditionType.AND) {
//								complexCompareStr = "(?=("+ complexCompareStr + ")(" + resultCompareStr+ "_.*))";
//							}
//							else {
//								complexCompareStr = "(("+ complexCompareStr + ")|(" + resultCompareStr+ "_.*))";
//							}
//						}
//						else {
//							resultCompareStr = complexCompareStr;
//						}
						resultCompareStr = complexCompareStr;
						resultRdFilterType = RDFilterType.REGEX;
					}
					
					
//					System.out.println("resultStartRowKey="+resultStartRowKey);
//					System.out.println("resultStopRowKey="+resultStopRowKey);
//					System.out.println("resultCompareStr="+resultCompareStr);
//					System.out.println("resultRdFilterType="+resultRdFilterType);
					if(isRootNode) {
						RDRootIndexFilterInfo result = new RDRootIndexFilterInfo();
						if(rdCondition.getContitionType()==ConditionType.AND) {
							result.setComplexFilterType(RDFilterType.AND);
						}
						else if(rdCondition.getContitionType()==ConditionType.OR) {
							result.setComplexFilterType(RDFilterType.OR);
						}
						result.setRowPrefix(resultRowPrefix);
						result.setStartRowKey(resultStartRowKey);
						result.setStopRowKey(resultStopRowKey);
						result.setCompareStr(resultCompareStr);
						result.setRdFilterType(resultRdFilterType);
						return result;
					}
					else {
						RDChildIndexFilterInfo result = new RDChildIndexFilterInfo();
						if(rdCondition.getContitionType()==ConditionType.AND) {
							result.setComplexFilterType(RDFilterType.AND);
						}
						else if(rdCondition.getContitionType()==ConditionType.OR) {
							result.setComplexFilterType(RDFilterType.OR);
						}
						result.setRowPrefix(resultRowPrefix);
						result.setStartRowKey(resultStartRowKey);
						result.setStopRowKey(resultStopRowKey);
						result.setCompareStr(resultCompareStr);
						result.setRdFilterType(resultRdFilterType);
						return result;
					}
			}
			else {
				//无效的的查询条件
				return null;
			}
		}
		else {
			if(isRootNode) {
				return getRootSingleColumnIndexFilter(rowPrefix,rdCondition);
			}
			else {
				return getChildColumnIndexFilter(rowPrefix,rdCondition);
			}
		}
	}
	
	//返回or 或者 and的子条件
	private static RDChildIndexFilterInfo getChildColumnIndexFilter(String rowPrefix , RDCondition rdCondition) {
		RDChildIndexFilterInfo result = new RDChildIndexFilterInfo();
		//多列索引的子索引
		if(rdCondition.getContitionType()==ConditionType.EQUAL){
			result.setRdFilterType(RDFilterType.EQUAL);
			result.setColumnName(rdCondition.getPara().getKey());
			result.setCompareStr(String.valueOf(rdCondition.getPara().getValue()));
		}
		else if(rdCondition.getContitionType()==ConditionType.GT) {
			result.setRdFilterType(RDFilterType.GT);
			result.setColumnName(rdCondition.getPara().getKey());
			result.setCompareStr(String.valueOf(rdCondition.getPara().getValue()));
		}
		else if(rdCondition.getContitionType()==ConditionType.START_WITH){
			result.setRdFilterType(RDFilterType.START_WITH);
			result.setColumnName(rdCondition.getPara().getKey());
			result.setCompareStr(String.valueOf(rdCondition.getPara().getValue()));
		}
		else if(rdCondition.getContitionType()==ConditionType.LT) {
			result.setRdFilterType(RDFilterType.STOP_WITH);
			result.setColumnName(rdCondition.getPara().getKey());
			result.setCompareStr(String.valueOf(rdCondition.getPara().getValue()));
		}
		else if(rdCondition.getContitionType()==ConditionType.CONTAIN) {
			result.setRdFilterType(RDFilterType.REGEX);
			result.setColumnName(rdCondition.getPara().getKey());
			result.setCompareStr(".*"+rdCondition.getPara().getValue()+".*");
		}
		else if(rdCondition.getContitionType()==ConditionType.END_WITH) {
			result.setRdFilterType(RDFilterType.REGEX);
			result.setColumnName(rdCondition.getPara().getKey());
			result.setCompareStr(".*"+rdCondition.getPara().getValue());
		}
		else if(rdCondition.getContitionType()==ConditionType.REGEX) {
			result.setRdFilterType(RDFilterType.REGEX);
			result.setColumnName(rdCondition.getPara().getKey());
			result.setCompareStr(String.valueOf(rdCondition.getPara().getValue()));
		}
		return result;
	}
	
	//只有单列的索引 且没有子条件
	private static RDRootIndexFilterInfo getRootSingleColumnIndexFilter(String rowPrefix , RDCondition rdCondition) {
		RDRootIndexFilterInfo result = new RDRootIndexFilterInfo();
		//单列索引  START_WITH   STOP_WITH   REGEX
		if(rdCondition.getContitionType()==ConditionType.EQUAL){
			result.setRdFilterType(RDFilterType.START_WITH);//+ RDConstants.RD_SEPARATOR 
			result.setStartRowKey(rowPrefix + String.valueOf(rdCondition.getPara().getValue()));
		}
		else if(rdCondition.getContitionType()==ConditionType.GT) {
			result.setRdFilterType(RDFilterType.START_WITH);
			result.setStartRowKey(rowPrefix + rdCondition.getPara().getValue()+RDConstants.RD_BEGIN_SUFFIX);
		}
		else if(rdCondition.getContitionType()==ConditionType.START_WITH){
			result.setRdFilterType(RDFilterType.START_WITH);
			result.setStartRowKey(rowPrefix + String.valueOf(rdCondition.getPara().getValue()));
		}
		else if(rdCondition.getContitionType()==ConditionType.LT) {
			result.setRdFilterType(RDFilterType.STOP_WITH);
			result.setStartRowKey(rowPrefix);
			result.setStopRowKey(rowPrefix + String.valueOf(rdCondition.getPara().getValue()));
		}
		else if(rdCondition.getContitionType()==ConditionType.CONTAIN) {
			result.setRdFilterType(RDFilterType.REGEX);
			result.setStartRowKey(rowPrefix);
			result.setCompareStr(rowPrefix + ".*"+rdCondition.getPara().getValue()+".*");
		}
		else if(rdCondition.getContitionType()==ConditionType.END_WITH) {
			result.setRdFilterType(RDFilterType.REGEX);
			result.setStartRowKey(rowPrefix);
			result.setCompareStr(rowPrefix + ".*"+rdCondition.getPara().getValue());
		}
		else if(rdCondition.getContitionType()==ConditionType.REGEX) {
			result.setRdFilterType(RDFilterType.REGEX);
			result.setStartRowKey(rowPrefix);
			result.setCompareStr(rowPrefix + String.valueOf(rdCondition.getPara().getValue()));
		}
		return result;
	}
	
	//根据table 获取当前查询条件对应的RDindex
	private static RDIndex getRDIndex(RDCondition rdCondition,RDTable rdTable,LinkedHashMap<String,Integer> indexColumnRealNames) throws Exception {
		HashSet<String> columnNames = new HashSet<String>();
		fillConditionColumnNames(rdCondition,columnNames);
		
		RDColumn rdColumn = null;
		List<RDColumn> indexColumns = new ArrayList<RDColumn>();
		for(String rdColNm:columnNames) {
			rdColumn = rdTable.getRdColumns().get(rdColNm);
			if(rdColumn==null) throw new NotFoundException("Column "+rdColNm+" is not found");
			indexColumns.add(rdColumn);
		}
		
		Collections.sort(indexColumns, new Comparator<RDColumn>() {
			@Override
			public int compare(RDColumn o1, RDColumn o2) {
				if(o1.getSeq()>o2.getSeq())return 1;
				else if(o1.getSeq()<o2.getSeq()) return -1;
				else return 0;
			}
		});
		
		StringBuilder indexName = new StringBuilder();
		int columnsCount = indexColumns.size();
		for(int i = 0 ; i<columnsCount; i ++) {
			indexName.append(indexColumns.get(i).getSeq());
			if(i!=(columnsCount-1)) {
				indexName.append(RDConstants._STR);
			}
		}
		RDIndex rdIndex = rdTable.getRdIndexes().get(indexName.toString());
		if(rdIndex!=null) {
			for(RDColumn tmpColumn:indexColumns) {
				indexColumnRealNames.put(tmpColumn.getName(),tmpColumn.getSeq());
			}
		}
		return rdIndex;
	}
	
	//把rdCondition里所有的条件列名添加到集合columnNames
	private static void fillConditionColumnNames(RDCondition rdCondition,HashSet<String> columnNames) {
		if(rdCondition.getContitionType()==ConditionType.AND || rdCondition.getContitionType()==ConditionType.OR) {
			for(RDCondition childCondition : rdCondition.getChildConditions()) {
				fillConditionColumnNames(childCondition,columnNames);
			}
		}
		else {
			columnNames.add(rdCondition.getPara().getKey());
		}
	}
	

	
	
	
	
	public static void main(String[] args) {
		// 要验证的字符串
	    String str = "Windows 3.1123123";
	    // 正则表达式规则
	    //String regEx = "((?=Windows .*)(Windows 3.1123123))";
	    
	    String regEx = "((?=(?!(Windows1 .*)))(.*3.1123123))";
	    
	    // String regEx = "((Windows .*)|(.*get))";
	    // 编译正则表达式
	    Pattern pattern = Pattern.compile(regEx);
	    Matcher matcher = pattern.matcher(str);
	    // 查找字符串中是否有匹配正则表达式的字符/字符串
	   // System.out.println(matcher.find());
	    boolean rs = matcher.matches();
	    System.out.println(rs);
	}
	
	public static String biggerStr(String str1,String str2) {
		if(str1==null||str1.equals(""))return str2;
		else {
			if(str2==null||str2.equals(""))
				return str1;
			else if(str1.compareTo(str2)>0) {
				return str1;
			}
			else return str2;
		}
	}
	
	public static String smallerStr(String str1,String str2) {
		if(str1==null||str1.equals(""))return str2;
		else {
			if(str2==null||str2.equals(""))
				return str1;
			else if(str1.compareTo(str2)>0) {
				return str2;
			}
			else return str1;
		}
	}
	
	
	//优化树状结构
	public void optimize(RDTable rdTable) {
		this.rdCondition = subOptimize(rdTable,this.rdCondition,true);
	}
	
	private static RDCondition subOptimize(RDTable rdTable , RDCondition parendtRdCondition,boolean isRootNode) {
		RDCondition[] childConditions = parendtRdCondition.getChildConditions();
		if(childConditions == null||childConditions.length==0) {
			return parendtRdCondition;
		}
		
		ConditionType parentRdConditionType = parendtRdCondition.getContitionType();
		
		List<RDCondition> newChildConditions = new ArrayList<RDCondition>();
		if(parentRdConditionType==ConditionType.OR){
			for(RDCondition childCondition:childConditions) {
				RDCondition rdCondition = RDCondition.or(childCondition);
				newChildConditions.add(rdCondition);
			}
			parendtRdCondition.setChildConditions(newChildConditions.toArray(new RDCondition[newChildConditions.size()]));
			return parendtRdCondition;
		}
		else {
			Map<String,RDCondition> columnsSet = new HashMap<String,RDCondition>();
			
			List<RDCondition> duplicateChildConditions = new ArrayList<RDCondition>();//保留重复的
			
			for(RDCondition childCondition:childConditions) {
				RDCondition tmpRDCondition = subOptimize(rdTable,childCondition,false);
				if(tmpRDCondition.getContitionType()==ConditionType.AND) {
					RDCondition[] childsOfChild = tmpRDCondition.getChildConditions();
					for(int i = 0 ; i < childsOfChild.length ; i++) {
						RDCriteria.handleChildCondition(newChildConditions, duplicateChildConditions, columnsSet, childsOfChild[i]);
					}
				}
				else{//
					RDCriteria.handleChildCondition(newChildConditions, duplicateChildConditions, columnsSet, tmpRDCondition);
				}
			}
			
			if(duplicateChildConditions.size()>0) {
				newChildConditions.add(resolveDuplicate(duplicateChildConditions,parentRdConditionType));
			}
			
			parendtRdCondition.setChildConditions(newChildConditions.toArray(new RDCondition[newChildConditions.size()]));
			return parendtRdCondition;
		}
		
		
		
		
	}

	private static RDCondition resolveDuplicate(List<RDCondition> duplicateChildConditions, ConditionType parentRdConditionType) {
		RDCondition rdCondition = null;
		if(parentRdConditionType ==ConditionType.AND) {
			rdCondition = RDCondition.and(duplicateChildConditions.toArray(new RDCondition[duplicateChildConditions.size()]));
		}
		else {
			rdCondition = RDCondition.or(duplicateChildConditions.toArray(new RDCondition[duplicateChildConditions.size()]));
		}
		
		RDCondition[] childConditions = rdCondition.getChildConditions();
		List<RDCondition> newChildConditions = new ArrayList<RDCondition>();
		Map<String,RDCondition> columnsSet = new HashMap<String,RDCondition>();
		duplicateChildConditions = new ArrayList<RDCondition>();//保留重复的
		for(RDCondition tmpRDCondition:childConditions) {
			RDCriteria.handleChildCondition(newChildConditions, duplicateChildConditions, columnsSet, tmpRDCondition);
		}
		if(duplicateChildConditions.size()>0) {
			newChildConditions.add(resolveDuplicate(duplicateChildConditions,parentRdConditionType));
		}
		
		return rdCondition;
	}
	
	private static void handleChildCondition(List<RDCondition> newChildConditions,List<RDCondition> duplicateChildConditions,Map<String,RDCondition> columnsSet,RDCondition tmpRDCondition) {
		if(tmpRDCondition.getPara()!=null && !columnsSet.containsKey(tmpRDCondition.getPara().getKey())) {
			newChildConditions.add(tmpRDCondition);
			columnsSet.put(tmpRDCondition.getPara().getKey(),tmpRDCondition);
		}
		else {
			RDCondition exsit = columnsSet.get(tmpRDCondition.getPara().getKey());
			if(exsit.getContitionType()!=tmpRDCondition.getContitionType()
					|| !exsit.getPara().getKey().equals(tmpRDCondition.getPara().getKey())
					|| !exsit.getPara().getValue().equals(tmpRDCondition.getPara().getValue())) {
				duplicateChildConditions.add(tmpRDCondition);
			}
		}
	}
}
