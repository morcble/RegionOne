package cn.regionsoft.one.bigdata.impl.hbase;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.coprocessor.AggregateImplementation;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import cn.regionsoft.one.bigdata.core.exceptions.DuplicateException;
import cn.regionsoft.one.bigdata.core.exceptions.ExistException;
import cn.regionsoft.one.bigdata.core.exceptions.NotFoundException;
import cn.regionsoft.one.bigdata.core.object.RDColumn;
import cn.regionsoft.one.bigdata.core.object.RDIndex;
import cn.regionsoft.one.bigdata.core.object.RDSchema;
import cn.regionsoft.one.bigdata.core.object.RDTable;
import cn.regionsoft.one.bigdata.core.persist.RDEntity;
import cn.regionsoft.one.bigdata.core.persist.RDEntityListResult;
import cn.regionsoft.one.bigdata.criterias.RDCriteria;
import cn.regionsoft.one.bigdata.criterias.RDFilterType;
import cn.regionsoft.one.bigdata.criterias.indexfilter.RDRootIndexFilterInfo;
import cn.regionsoft.one.bigdata.enums.DataType;
import cn.regionsoft.one.bigdata.ids.IDProducer;
import cn.regionsoft.one.bigdata.impl.RDConstants;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.entity.BindColumn;

public class HbaseUtil{
	private static IDProducer idProducer;

	public static void setIDProducer(IDProducer idProducer) {
		HbaseUtil.idProducer = idProducer;
	}
	
	
	private static Set<String> exsitSpace = new HashSet<String>();

	
	public static void deleteRDSchema(String userId ,String appName) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		
		RDSchema rdSchema = getRDSchema(userId,appName);
		if(rdSchema!=null) {
			deleteByRowKeyStartWith(table,userId+RDConstants._STR+rdSchema.getSeq());
		
			String schemaFlagRowKey = RDConstants.RD_SCHEMA_STR+RDConstants._STR+RDConstants.RD_ENABLE_STR;
			Put tableFlagOp = new Put(Bytes.toBytes(schemaFlagRowKey));
		    tableFlagOp.addColumn(RDConstants.FAMILY_BYTES, Bytes.toBytes(rdSchema.getSeq()), Bytes.toBytes(RDConstants.DISABLED));
		    table.put(tableFlagOp);
		}
	}
	
	
	public static RDSchema createRDSchema(String userId, String appName,String rdSchemaNm) throws Exception {
		String schemaRowKey = RDConstants.RD_SCHEMA_STR+RDConstants._STR+userId;
		String schemaAppIdRowKey = schemaRowKey+RDConstants._STR+RDConstants.RD_APPIDS_STR;
		String schemaFlagRowKey = schemaRowKey+RDConstants._STR+RDConstants.RD_ENABLE_STR;
		RDSchema rdSchema = null;
		try {
			int index = createMetaData(new String[] {schemaRowKey,schemaAppIdRowKey},schemaFlagRowKey,new String[] {rdSchemaNm,appName},0);
			rdSchema = new RDSchema(userId,appName,rdSchemaNm,index);
			return rdSchema;
		}
		catch(ExistException e) {
			throw new ExistException("Exsits schema:"+e.getMessage());
		}
	}
	
	//根据rowkey创建metaData ,notDuplicateIndex 不能重复的序号,-1 标识都能重复
	//返回新创建值的index
	public static int createMetaData(String[] rowKeys,String flagKey,String[] vals,int notDuplicateIndex) throws Exception{
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		List<Get> gets = new ArrayList<Get>();
		Get getFlagOfLogicTables = new Get(Bytes.toBytes(flagKey));
		gets.add(getFlagOfLogicTables);
		
		for(String rowKey:rowKeys) {
			gets.add(new Get(rowKey.getBytes()));
		}
		try {
			Result[] r = table.get(gets);
			Cell[] logicTableFlags = r[0].rawCells();
			
			int length = logicTableFlags.length;
			
			if(notDuplicateIndex!=-1) {//检查重复数据
				Cell[] compareCells = r[notDuplicateIndex+1].rawCells();
				String newVal = vals[notDuplicateIndex];
				for(int i = 0 ; i <length ; i ++) {
					String enableFlag = Bytes.toString(CellUtil.cloneValue(logicTableFlags[i]));
					if(RDConstants.ENABLED.equals(enableFlag)) {
						String checkVal = Bytes.toString(CellUtil.cloneValue(compareCells[i]));
						if(checkVal.equals(newVal))throw new ExistException(newVal);
					}
				}
			}
			
			List<Put> puts = new ArrayList<Put>();
			Put tableInfoOp = new Put(Bytes.toBytes(flagKey));
			tableInfoOp.addColumn(RDConstants.FAMILY_BYTES, Bytes.toBytes(length), Bytes.toBytes(RDConstants.ENABLED));
			puts.add(tableInfoOp);
	        
			int index = 0;
			for(String rowKey:rowKeys) {
				 Put tableFlagOp = new Put(Bytes.toBytes(rowKey));
			     tableFlagOp.addColumn(RDConstants.FAMILY_BYTES, Bytes.toBytes(length), Bytes.toBytes(vals[index]));
			     puts.add(tableFlagOp);
			     index++;
			}
	        table.put(puts);
	        
	        return length;
		}
		catch(Exception e) {
			throw e;
		}
	}
	
	//读出多rowkey的配置数据
	private static String[][] getMetaData(String flagKey,String[] rowKeys) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		List<Get> gets = new ArrayList<Get>();
		Get getFlagOfLogicTables = new Get(flagKey.getBytes());
		gets.add(getFlagOfLogicTables);
		
		for(String rowKey:rowKeys) {
			gets.add(new Get(rowKey.getBytes()));
		}
		try {
			Result[] r = table.get(gets);
			Cell[] logicTableFlags = r[0].rawCells();
			
			String[][] result = new String[gets.size()][logicTableFlags.length];
			
			for(int i = 0 ; i <result.length ; i ++) {
				String[] tmp = result[i];
				for(int j = 0 ; j<tmp.length ; j ++) {
					result[i][j] = Bytes.toString(CellUtil.cloneValue(r[i].rawCells()[j]));
				}
			}
			return result;
		}
		catch(Exception e) {
			throw e;
		}
	}

	//根据用户的userId和appName 查找数据库
	public static RDSchema getRDSchema(String userId,String appName) throws Exception {
		String schemaRowKey = RDConstants.RD_SCHEMA_STR+RDConstants._STR+userId;
		String schemaAppIdRowKey = schemaRowKey+RDConstants._STR+RDConstants.RD_APPIDS_STR;
		String schemaFlagRowKey = schemaRowKey+RDConstants._STR+RDConstants.RD_ENABLE_STR;
		
		String[][] schemaMetaData = getMetaData(schemaFlagRowKey,new String[] {schemaAppIdRowKey,schemaRowKey});
		
		RDSchema rdSchema = null;
		int index = 0;
		for(String enableFlag : schemaMetaData[0]) {
			if(RDConstants.ENABLED.equals(enableFlag) && appName.equals(schemaMetaData[1][index])) {
				rdSchema = new RDSchema(userId,appName,schemaMetaData[2][index],index);
				break;
			}
			index++;
		}
		return rdSchema;
	}
	
	/**
	 * 要检查table是否已经被删除
	 */
	public static RDTable getRdTable(RDSchema rdSchema, String rdTableNm) throws Exception {
		//获取table
		String logicTableKey = rdSchema.getUserId()  + RDConstants._STR + rdSchema.getSeq() + RDConstants._STR + RDConstants.RD_TBLS_STR;
		String logicTableFlagKey = logicTableKey + RDConstants._STR + RDConstants.RD_ENABLE_STR;
		
		String[][] schemaMetaData = getMetaData(logicTableFlagKey,new String[] {logicTableKey});
		
		RDTable rdTable = null;
		int index = 0;
		for(String enableFlag : schemaMetaData[0]) {
			if(RDConstants.ENABLED.equals(enableFlag) && rdTableNm.equals(schemaMetaData[1][index])) {
				rdTable = new RDTable(rdSchema,rdTableNm,index);
				break;
			}
			index++;
		}
		if(rdTable==null)return null;
		
		String logicColsKey = rdSchema.getUserId()  + RDConstants._STR + rdSchema.getSeq()
							+RDConstants._STR+rdTable.getSeq()+RDConstants._STR + RDConstants.RD_COLS_STR;
		String logicColsDataTypeKey = logicColsKey+RDConstants._STR +RDConstants.RD_DATATYPE_STR;
		String logicColsFlagKey = logicColsKey+RDConstants._STR +RDConstants.RD_ENABLE_STR;
		
		String[][] colsMetaData = getMetaData(logicColsFlagKey,new String[] {logicColsKey,logicColsDataTypeKey});
		
		index = 0;
		for(String enableFlag : colsMetaData[0]) {
			if(RDConstants.ENABLED.equals(enableFlag)) {
				rdTable.registerColumn(new RDColumn(colsMetaData[1][index],DataType.getDataType(colsMetaData[2][index]),index));
			}
			index++;
		}
		
		String logicIndexKey = rdSchema.getUserId()  + RDConstants._STR + rdSchema.getSeq()
							+RDConstants._STR+rdTable.getSeq()+RDConstants._STR + RDConstants.RD_INDEX_STR;
		String logicIndexFlagKey = logicIndexKey +RDConstants._STR +RDConstants.RD_ENABLE_STR;
		
		String[][] indexMetaData = getMetaData(logicIndexFlagKey,new String[] {logicIndexKey});
		
		index = 0;
		for(String enableFlag : indexMetaData[0]) {
			if(RDConstants.ENABLED.equals(enableFlag)) {
				rdTable.registerIndex(new RDIndex(indexMetaData[1][index],index));
			}
			index++;
		}

		return rdTable;
	}
	
	public static RDTable createRdTable(RDSchema rdSchema,String rdTableName) throws Exception {
		String logicTableKey = rdSchema.getUserId()  + RDConstants._STR+rdSchema.getSeq()+RDConstants._STR + RDConstants.RD_TBLS_STR;
		String logicTableFlagKey = logicTableKey+RDConstants._STR+RDConstants.RD_ENABLE_STR;
		try {
			int index = createMetaData(new String[] {logicTableKey},logicTableFlagKey,new String[] {rdTableName},0);
			return new RDTable(rdSchema,rdTableName,index);
		}
		catch(ExistException e) {
			throw new ExistException("Exsits Table:"+e.getMessage());
		}
	}
	
	public static RDColumn createRdColumn(RDTable rdTable, String rdColumnNm, String label,DataType dataType) throws Exception {
		RDSchema rdSchema = rdTable.getRdSchema();

		String logicColsKey = rdSchema.getUserId()  + RDConstants._STR+
				rdSchema.getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR + RDConstants.RD_COLS_STR;
		String logicColsDataTypeKey = logicColsKey +RDConstants._STR+RDConstants.RD_DATATYPE_STR;
		String logicColsLabeleKey = logicColsKey +RDConstants._STR+RDConstants.RD_LABEL_STR;
		String logicColsFlagKey = logicColsKey +RDConstants._STR+RDConstants.RD_ENABLE_STR;
		
		
		try {
			int columnIndex = createMetaData(new String[] {logicColsKey,logicColsDataTypeKey,logicColsLabeleKey},logicColsFlagKey,new String[] {rdColumnNm,dataType.name(),label},0);
			RDColumn rdColumn = new RDColumn(rdColumnNm, dataType, columnIndex);
	        return rdColumn;
		}
		catch(ExistException e) {
			throw new ExistException("Exsits column:"+e.getMessage());
		}
	}

	
	public static void clearTable(RDSchema rdSchema, String rdTableName) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		
		RDTable rdTable = getRdTable(rdSchema, rdTableName);
		String dataStartKey = rdSchema.getUserId()  + RDConstants._STR 
				+ rdSchema.getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR + RDConstants.RD_DATA_STR;
		deleteByRowKeyStartWith(table,dataStartKey);
		
		String indexDataStartKey = rdSchema.getUserId()  + RDConstants._STR 
				+ rdSchema.getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR + RDConstants.RD_INDEX_STR
				+ RDConstants._STR + RDConstants.RD_DATA_STR;
		deleteByRowKeyStartWith(table,indexDataStartKey);
	}

	
	public static void deleteTable(RDSchema rdSchema, String rdTableName) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();

		RDTable rdTable = getRdTable(rdSchema, rdTableName);
		if(rdTable==null)return;
		String dataStartKey = rdSchema.getUserId()  + RDConstants._STR 
				+ rdSchema.getSeq()+RDConstants._STR+rdTable.getSeq();
		deleteByRowKeyStartWith(table,dataStartKey);
		
		String logicTableFlagKey = rdSchema.getUserId()  + RDConstants._STR 
				+ rdSchema.getSeq()+RDConstants._STR + RDConstants.RD_TBLS_STR +RDConstants._STR+RDConstants.RD_ENABLE_STR;
		Put putOp = new Put(Bytes.toBytes(logicTableFlagKey));
		putOp.addColumn(RDConstants.FAMILY_BYTES, Bytes.toBytes(rdTable.getSeq()),Bytes.toBytes(RDConstants.DISABLED));
		
		table.put(putOp);
	}

	
	public static void printAllData() throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		ResultScanner resultScanner1 = null;
		try {
			Scan scan1 = new Scan();
			//scan1.withStartRow(Bytes.toBytes("SAMPLE_0_INDEX_DATA_1_feng2000.password2000"));
	        resultScanner1 = table.getScanner(scan1);
	        for (Result resRow : resultScanner1) {
	        	String tmp = Bytes.toString(resRow.getRow()) + " : ";
	        	
	        	CellScanner cellScanner = resRow.cellScanner();
	        	int i = 0;
	        	Cell tmpCell = null;
	        	String cellVal = null;
        		while(cellScanner.advance()) {
        			tmpCell = cellScanner.current();
        			byte[] bytes = CellUtil.cloneValue(tmpCell);
        			cellVal = Bytes.toString(CellUtil.cloneValue(tmpCell));
    				tmp+="["+i+":"+cellVal+"] ";
            		
            		i++;
        		}
				System.out.println(tmp); 
	        }
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(resultScanner1!=null)resultScanner1.close();
		}
	}
	

	public static Long insert( RDTable rdTable ,Map<String, Object> newRecord) throws Exception {//TODO String 到 Object
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		Map<String,RDColumn> tableCols = rdTable.getRdColumns();

		List<Put> puts = new ArrayList<Put>();
		
		Long recordId = idProducer.getNextLongId();
		RDSchema rdSchema = rdTable.getRdSchema();
		Put put = new Put(Bytes.toBytes(rdSchema.getUserId() +RDConstants._STR
				+rdTable.getRdSchema().getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR 
				+ RDConstants.RD_DATA_STR+RDConstants._STR+recordId));
		
		Iterator<Entry<String,Object>> iterator = newRecord.entrySet().iterator();
		RDColumn rdColumn = null;
		while(iterator.hasNext()) {
			Entry<String,Object> tmp = iterator.next();
			rdColumn = tableCols.get(tmp.getKey());
			if(rdColumn==null)continue;
			byte[] tmpBytes = getBytesFromColumnVal(rdColumn,tmp.getValue());
			if(tmpBytes!=null)
				put.addColumn(RDConstants.FAMILY_BYTES, Bytes.toBytes(rdColumn.getSeq()), tmpBytes);
		}
		puts.add(put);
		
		
		//添加索引
		if(rdTable.getRdIndexes().size()!=0) {
			String indexKeyPrefix = rdSchema.getUserId() +RDConstants._STR+rdTable.getRdSchema().getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR+RDConstants.RD_INDEX_STR
					+ RDConstants._STR + RDConstants.RD_DATA_STR + RDConstants._STR;
			StringBuilder indexKey = null;
			Map<Integer,String> columnIndexToNameMap = rdTable.getColumnIndexToNameMap();
			
			String[] columnSeqArray = null;
			String columnName = null;
			Object columnVal = null;
			for(RDIndex rdIndex :rdTable.getRdIndexes().values()) {
				indexKey = new StringBuilder();
				indexKey.append(indexKeyPrefix);
				indexKey.append(rdIndex.getSeq());
				indexKey.append(RDConstants._STR);
				
				columnSeqArray = rdIndex.getName().split(RDConstants._STR);
				for(String columnSeq:columnSeqArray) {
					columnName = columnIndexToNameMap.get(Integer.parseInt(columnSeq));
					columnVal = newRecord.get(columnName);
					indexKey.append(String.valueOf(columnVal));
					indexKey.append(RDConstants._STR);
				}
				indexKey.append(recordId);
				
				Put tableInfoOp = new Put(Bytes.toBytes(indexKey.toString()));
				tableInfoOp.addColumn(RDConstants.FAMILY_BYTES, Bytes.toBytes(0), Bytes.toBytes(0L));
				puts.add(tableInfoOp);
			}
		}
		
		table.put(puts);
		
		return recordId;
	}
	
	
	public static Long getAmount(RDTable rdTable,RDCriteria rdCriteria) throws Throwable {
		if(rdTable==null) throw new Exception("rdTable is null");
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		Scan scan = new Scan();
		wrapScanner(scan, rdTable, null, rdCriteria, null);
		if(Bytes.toString(scan.getStartRow()).compareTo(Bytes.toString(scan.getStopRow()))>0){
			return 0L;
		}
		return HbaseThreadHolder.getInstance().getThreadDatas().getAggregationClient().rowCount(table, new LongColumnInterpreter(), scan);
	}
	
	
	public static List<RDEntity> findAll(RDTable rdTable) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		List<RDEntity> listResult = new ArrayList<RDEntity>();
		
		RDSchema rdSchema = rdTable.getRdSchema();
		String rowPrefix = rdSchema.getUserId() +RDConstants._STR
				+rdTable.getRdSchema().getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR + RDConstants.RD_DATA_STR;
		Scan scan = new Scan();
		scan.withStartRow(Bytes.toBytes(rowPrefix));
		scan.setRowPrefixFilter(Bytes.toBytes(rowPrefix));	
		scan.withStopRow(Bytes.toBytes(rowPrefix+RDConstants.RD_END_SUFFIX));
		
		ResultScanner resultScanner = null;
		try {
			resultScanner = table.getScanner(scan);
			for (Result resRow : resultScanner) {
				listResult.add(wrapEntityFromRowData(rdTable, resRow));
	        }
		}
		finally {
			CommonUtil.closeQuietly(resultScanner);
		}
		return listResult;
	}
	
	//查找被回收的数据
	public static List<RDEntity> findRecycledData(RDTable rdTable) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getRecycleDataTable();
		List<RDEntity> listResult = new ArrayList<RDEntity>();
		
		RDSchema rdSchema = rdTable.getRdSchema();
		String rowPrefix = rdSchema.getUserId() +RDConstants._STR
				+rdTable.getRdSchema().getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR + RDConstants.RD_DATA_STR;
		Scan scan = new Scan();
		scan.withStartRow(Bytes.toBytes(rowPrefix));
		scan.setRowPrefixFilter(Bytes.toBytes(rowPrefix));	
		scan.withStopRow(Bytes.toBytes(rowPrefix+RDConstants.RD_END_SUFFIX));
		
		ResultScanner resultScanner = null;
		try {
			resultScanner = table.getScanner(scan);
			for (Result resRow : resultScanner) {
				listResult.add(wrapEntityFromRowData(rdTable, resRow));
	        }
		}
		finally {
			CommonUtil.closeQuietly(resultScanner);
		}
		return listResult;
	}
	
	/**
	 * currentPage >=1
	 */
	public static RDEntityListResult getList(RDTable rdTable, Integer pageSize, Integer pageNo, RDCriteria rdCriteria) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		if(pageNo==null||pageNo<1)pageNo=1;
		return subGetList(table,rdTable,pageNo,pageSize,rdCriteria,null);
	}
	
	
	public static RDEntityListResult getList(RDTable rdTable, Integer pageSize, String startRowKey, RDCriteria rdCriteria) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		if(startRowKey==null) throw new Exception("startRowKey is empty");
		return subGetList(table,rdTable,null,pageSize,rdCriteria,startRowKey);
	}
	
//	Filter filter1 = new PrefixFilter(Bytes.toBytes(rdIndexFilterInfo.getRowPrefix()));
//	Filter filter2 = new RowFilter(CompareOp.EQUAL,new RegexStringComparator(rdIndexFilterInfo.getCompareStr()));
//	Filter filter2 = new RowFilter(CompareOp.EQUAL,new BinaryComparator(rdIndexFilterInfo.getCompareStr()));
	
	//如果startRowKey不为空,则忽略pageNo
	//优先按索引获取数据
	private static boolean enableIndexSerach = true;//是否开启index查询
	
	private static RDEntityListResult subGetList(HTable table,RDTable rdTable,Integer pageNo,Integer pageSize,RDCriteria rdCriteria,String startRowKey) throws Exception {
		Scan scan = new Scan();
		RDRootIndexFilterInfo rdIndexFilterInfo = wrapScanner(scan, rdTable, pageSize, rdCriteria, startRowKey);
		
		RDEntityListResult rdListResult = null;
		if(rdIndexFilterInfo!=null) {//index search
			rdListResult = subOfSubGetList(table,scan, rdTable, pageNo, pageSize, startRowKey);
			List<RDEntity> idList = rdListResult.getList();
			
			rdListResult = new RDEntityListResult();
			if(idList!=null) {
				if(idList.size()==1) {
					List<RDEntity> listResult = new ArrayList<RDEntity>();
					listResult.add(findById(rdTable, idList.get(0).getId()));
					rdListResult.setList(listResult);
				}
				else {
					String[] ids = new String[idList.size()];
					int index = 0 ;
					for(RDEntity rdEntity:idList) {
						ids[index] = rdEntity.getId();
						index++;
					}
					
					List<RDEntity> listResult = findByIds(rdTable, ids);
					rdListResult.setList(listResult);
				}
			}
		}
		else {
			rdListResult = subOfSubGetList(table,scan, rdTable, pageNo, pageSize, startRowKey);
		}
		
		
		return rdListResult;
	}
	
	private static RDRootIndexFilterInfo wrapScanner(Scan scan,RDTable rdTable,Integer pageSize,RDCriteria rdCriteria,String startRowKey) throws Exception{
		if(rdTable==null) throw new Exception("rdTable is null");
		if(RDConstants.RD_SEARCH_EOF.equals(startRowKey))return null;
		
		RDSchema rdSchema = rdTable.getRdSchema();
		
		RDRootIndexFilterInfo rdIndexFilterInfo = null;
		Filter pageFilter = null;
		if(pageSize!=null) {
			pageFilter = new PageFilter(pageSize+1);
		}
		
		if(rdCriteria!=null) {
			if(enableIndexSerach) {
				rdIndexFilterInfo = rdCriteria.getIndexFilter(rdTable);
			}
			
			if(rdIndexFilterInfo!=null) {
				//index 查询
				startRowKey = RDCriteria.biggerStr(startRowKey, rdIndexFilterInfo.getStartRowKey());
				String stopRowKey = null;
				
				if(rdIndexFilterInfo.getRdFilterType() == RDFilterType.START_WITH) {
					stopRowKey = rdIndexFilterInfo.getStopRowKey();
					scan.setRowPrefixFilter(Bytes.toBytes(rdIndexFilterInfo.getCompareStr()));	
					if(pageFilter!=null)scan.setFilter(pageFilter);
				}
				else if(rdIndexFilterInfo.getRdFilterType() == RDFilterType.EQUAL) {
					stopRowKey = rdIndexFilterInfo.getStopRowKey();
					scan.setRowPrefixFilter(Bytes.toBytes(rdIndexFilterInfo.getCompareStr()));	
					if(pageFilter!=null)scan.setFilter(pageFilter);
				}
				else if(rdIndexFilterInfo.getRdFilterType() == RDFilterType.GT) {
					stopRowKey = rdIndexFilterInfo.getStopRowKey();
					scan.setRowPrefixFilter(Bytes.toBytes(rdIndexFilterInfo.getRowPrefix()));	
					if(pageFilter!=null)scan.setFilter(pageFilter);
				}
				else if(rdIndexFilterInfo.getRdFilterType() == RDFilterType.STOP_WITH
						||rdIndexFilterInfo.getRdFilterType() == RDFilterType.REGEX) {
					stopRowKey = rdIndexFilterInfo.getStopRowKey();
					
					FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
					filterList.addFilter(new RowFilter(CompareOp.EQUAL,new RegexStringComparator(rdIndexFilterInfo.getCompareStr()+"_")));
					if(pageFilter!=null)filterList.addFilter(pageFilter);
					scan.setFilter(filterList);
				}
				if(startRowKey!=null)scan.withStartRow(Bytes.toBytes(startRowKey));
				if(stopRowKey!=null)scan.withStopRow(Bytes.toBytes(stopRowKey));
			}
			else {
				//全表扫描
				FilterList childFilterList = rdCriteria.toFilterList(rdTable);
				if(childFilterList==null) {
					if(pageFilter!=null)scan.setFilter(pageFilter);
				}
				else {
					FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
					filterList.addFilter(childFilterList);
					if(pageFilter!=null)filterList.addFilter(pageFilter);
					scan.setFilter(filterList);
				}
				
				String rowPrefix = rdSchema.getUserId() +RDConstants._STR
						+rdTable.getRdSchema().getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR + RDConstants.RD_DATA_STR;
				scan.withStartRow(Bytes.toBytes(rowPrefix));
				scan.setRowPrefixFilter(Bytes.toBytes(rowPrefix));	
				scan.withStopRow(Bytes.toBytes(rowPrefix+RDConstants.RD_END_SUFFIX));
			}
		}
		else {
			String rowPrefix = rdSchema.getUserId() +RDConstants._STR
					+rdTable.getRdSchema().getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR + RDConstants.RD_DATA_STR;
			scan.setRowPrefixFilter(Bytes.toBytes(rowPrefix));
			if(startRowKey==null) {
				scan.withStartRow(Bytes.toBytes(rowPrefix));
			}
			else {
				startRowKey = RDCriteria.biggerStr(startRowKey, rowPrefix);//startRowKey 是来自页面的分页参数
			}
			scan.withStopRow(Bytes.toBytes(rowPrefix+RDConstants.RD_END_SUFFIX));
			scan.setFilter(pageFilter);
		}
		return rdIndexFilterInfo;
	}
	
	private static RDEntityListResult subOfSubGetList(HTable table,Scan scan,RDTable rdTable,Integer pageNo,Integer pageSize , String startRowKey) throws Exception {
		RDEntityListResult rdListResult = new RDEntityListResult();
		List<RDEntity> listResult = new ArrayList<RDEntity>();
		rdListResult.setList(listResult);
		//scan.setCaching(1000);
		
		//pageSize 为空时则查出所有记录
		if(pageSize==null) {
			ResultScanner resultScanner = table.getScanner(scan);
			try {
				for (Result resRow : resultScanner) {
					listResult.add(wrapEntityFromRowData(rdTable, resRow));
		        }
				return rdListResult;
			}
			finally {
				resultScanner.close();
			}
		}
		
		if(startRowKey==null) {//自动翻页
			for (int i = 1; i < pageNo+1; i++) {
				ResultScanner resultScanner = table.getScanner(scan);
				int count = 0;
				try {
					rdListResult.setNextStartRowKey(RDConstants.RD_SEARCH_EOF);//假定为最后一页
					for (Result resRow : resultScanner) {
						count++;
						if (count==pageSize + 1) {
			                startRowKey = Bytes.toString(resRow.getRow());
			                if(i==pageNo) {
			                	rdListResult.setNextStartRowKey(startRowKey);
			                	break;
			                }
			                scan.withStartRow(startRowKey.getBytes());
			                break;
			            }
						
						if(i==pageNo && count <=pageSize) {
							listResult.add(wrapEntityFromRowData(rdTable, resRow));
						}
			        }
					
					if(count < pageSize + 1) {
						break;
					}
					
				}
				finally {
					resultScanner.close();
				}
			}	
		}
		else {//直接到startRowKey指定位置
			scan.withStartRow(startRowKey.getBytes());
			
			if(Bytes.toString(scan.getStartRow()).compareTo(Bytes.toString(scan.getStopRow()))>0){
				return rdListResult;
			}
			
			ResultScanner resultScanner = table.getScanner(scan);
			int count = 0;
			try {
				rdListResult.setNextStartRowKey(RDConstants.RD_SEARCH_EOF);//假定为最后一页
				for (Result resRow : resultScanner) {
					count++;
					if (count==pageSize + 1) {
		                startRowKey = Bytes.toString(resRow.getRow());
		                rdListResult.setNextStartRowKey(startRowKey);
	                	break;
		            }
					
					if(count <=pageSize) {
						listResult.add(wrapEntityFromRowData(rdTable, resRow));
					}
		        }
			}
			finally {
				resultScanner.close();
			}
		}
		return rdListResult;
	}
	


	
	public static RDEntity findById(RDTable rdTable, String id) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		ResultScanner resultScanner = null;
		try {
			RDSchema rdSchema = rdTable.getRdSchema();
			String startRowKey = rdSchema.getUserId() +RDConstants._STR+rdTable.getRdSchema().getSeq()
					+RDConstants._STR+rdTable.getSeq()+RDConstants._STR+ RDConstants.RD_DATA_STR + RDConstants._STR+ id;
			Scan scan = new Scan();
			scan.withStartRow(startRowKey.getBytes());
			scan.setRowPrefixFilter(Bytes.toBytes(startRowKey));
			scan.setFilter(new RowFilter(CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes(startRowKey))));
			resultScanner = table.getScanner(scan);
			
			RDEntity result = null;
			for (Result resRow : resultScanner) {
				result = wrapEntityFromRowData(rdTable, resRow);
				break;
			}
			
			return result;
		}
		finally {
			if(resultScanner!=null)resultScanner.close();
		}
	}
	
	
	public static List<RDEntity> findByIds(RDTable rdTable, String[] ids)  throws Exception {
		ResultScanner resultScanner = null;
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		try {
			List<RDEntity> result = new ArrayList<RDEntity>();

			List<Get> getList=new ArrayList<Get>();
			RDSchema rdSchema = rdTable.getRdSchema();
			String startRowKey = rdSchema.getUserId() +RDConstants._STR+rdTable.getRdSchema().getSeq()
					+RDConstants._STR+rdTable.getSeq()+RDConstants._STR+ RDConstants.RD_DATA_STR + RDConstants._STR;
			
			for(String id:ids){
                Get get=new Get(Bytes.toBytes(startRowKey+id));
                get.addFamily(RDConstants.FAMILY_BYTES);
                getList.add(get);
            }
			
			Result[] resRows= table.get(getList);
            for (Result resRow:resRows) {
            	result.add(wrapEntityFromRowData(rdTable, resRow));
            }
            
            return result;
		}
		finally {
			if(resultScanner!=null)resultScanner.close();
		}
	}
	
	

	//删除rowkey以startWith开头的行
	public static void deleteByRowKeyStartWith(HTable table,String startWith) throws Exception {
		int pageSize = 2;
		int insertBatchSize = 1000;
		String stopWith = startWith+RDConstants.RD_END_SUFFIX;
		Scan scan = new Scan();
		Filter pageFilter = new PageFilter(pageSize+1);
		Filter filter = new KeyOnlyFilter();
		
		FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
		filterList.addFilter(pageFilter);
		filterList.addFilter(filter);

		scan.setFilter(filterList);
		scan.withStartRow(Bytes.toBytes(startWith));
		scan.withStopRow(Bytes.toBytes(stopWith));
		
		String startRowKey = null;
		String rowKey = null;
		List<Delete> deletes = new ArrayList<Delete>();
		while(true) {
			ResultScanner resultScanner = table.getScanner(scan);
			int count = 0;
			try {
		        for (Result resRow : resultScanner) {
		        	count++;					
					if (count==pageSize + 1) {
		                startRowKey = Bytes.toString(resRow.getRow());
		                scan.withStartRow(startRowKey.getBytes());
		                break;
		            }
		        	
					rowKey = Bytes.toString(resRow.getRow());
					
					Delete delete = new Delete(Bytes.toBytes(rowKey));
					deletes.add(delete);
					if(deletes.size()>insertBatchSize) {
						table.delete(deletes);
						deletes = new ArrayList<Delete>();
					}
		        }
		        if(count < pageSize + 1) {
					break;
				}
			}
			finally {
				resultScanner.close();
			}
		}
		
		if(deletes.size()>0)
			table.delete(deletes);
			
	}

	
	private static RDEntity wrapEntityFromRowData(RDTable rdTable,Result resRow) throws Exception {
		Iterator<Entry<String,RDColumn>> iterator = rdTable.getRdColumns().entrySet().iterator();
		
		RDEntity rdEntity = new RDEntity();
		Entry<String,RDColumn> tmpEntry = null;
		String propName = null;
		RDColumn rdColumn = null;
		Cell tmpCell = null;
		String rowKey = Bytes.toString(resRow.getRow());
		rdEntity.setId(rowKey.substring(rowKey.lastIndexOf(RDConstants._STR)+1));
		
		while(iterator.hasNext()) {
			tmpEntry = iterator.next();
			propName = tmpEntry.getKey();
			rdColumn = tmpEntry.getValue();
			
			tmpCell = resRow.getColumnLatestCell(RDConstants.FAMILY_BYTES, Bytes.toBytes(rdColumn.getSeq()));
			if(tmpCell==null)continue;
			rdEntity.put(propName, getColumnValFromCell(rdColumn,tmpCell));
		}
		return rdEntity;
	}
	
	
	//从cell里解析column的值
	public static Object getColumnValFromCell(RDColumn rdColumn,Cell cell) throws Exception {
		if(DataType.STRING==rdColumn.getDataType()) {
			return Bytes.toString(CellUtil.cloneValue(cell));
		}
		else if(DataType.INT==rdColumn.getDataType()) {
			return Bytes.toInt(CellUtil.cloneValue(cell));
		}
		else if(DataType.LONG==rdColumn.getDataType()) {
			return Bytes.toLong(CellUtil.cloneValue(cell));
		}
		else if(DataType.FLOAT==rdColumn.getDataType()) {
			return Bytes.toFloat(CellUtil.cloneValue(cell));
		}
		else if(DataType.DOUBLE==rdColumn.getDataType()) {
			return Bytes.toDouble(CellUtil.cloneValue(cell));
		}
		else if(DataType.BOOLEAN==rdColumn.getDataType()) {
			return Bytes.toBoolean(CellUtil.cloneValue(cell));
		}
		else if(DataType.DATE==rdColumn.getDataType()) {
			return new Date(Bytes.toLong(CellUtil.cloneValue(cell)));
		}
		else if(DataType.BIGDECIMAL==rdColumn.getDataType()) {
			return Bytes.toBigDecimal(CellUtil.cloneValue(cell));
		}
		throw new Exception("Data type is invalid:"+rdColumn.getDataType());
	}
	
	public static byte[] getBytesFromColumnVal(RDColumn rdColumn,Object columnVal) throws Exception {
		if(columnVal==null)return null;
		if(DataType.STRING==rdColumn.getDataType()) {
			return Bytes.toBytes((String)columnVal);
		}
		else if(DataType.INT==rdColumn.getDataType()) {
			if(columnVal instanceof String) {
				return Bytes.toBytes(Integer.valueOf((String) columnVal));
			}
			else {
				return Bytes.toBytes((int)columnVal);
			}
		}
		else if(DataType.LONG==rdColumn.getDataType()) {
			if(columnVal instanceof String) {
				return Bytes.toBytes(Long.valueOf((String) columnVal));
			}
			else {
				return Bytes.toBytes((long)columnVal);
			}
		}
		else if(DataType.FLOAT==rdColumn.getDataType()) {
			if(columnVal instanceof String) {
				return Bytes.toBytes(Float.valueOf((String) columnVal));
			}
			else {
				return Bytes.toBytes((float)columnVal);
			}
		}
		else if(DataType.DOUBLE==rdColumn.getDataType()) {
			if(columnVal instanceof String) {
				return Bytes.toBytes(Double.valueOf((String) columnVal));
			}
			else {
				return Bytes.toBytes((double)columnVal);
			}
		}
		else if(DataType.BOOLEAN==rdColumn.getDataType()) {
			return Bytes.toBytes((boolean)columnVal);
		}
		else if(DataType.DATE==rdColumn.getDataType()) {
			return Bytes.toBytes(((Date)columnVal).getTime());
		}
		else if(DataType.BIGDECIMAL==rdColumn.getDataType()) {
			return Bytes.toBytes((BigDecimal)columnVal);
		}
		throw new Exception("Data type is invalid:"+rdColumn.getDataType());
	}


	
	public static void createDataSpaceIfNotExist(Connection connection,String dataSpaceName) throws Exception {
		Admin admin = null;
		try {			
			if(!exsitSpace.contains(dataSpaceName)) {
				admin = connection.getAdmin();
	            TableName tbName = TableName.valueOf(dataSpaceName);
				if(!admin.tableExists(tbName)) {
					HTableDescriptor hTableDescriptor = new HTableDescriptor(tbName);
		            HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(RDConstants.RD_HBASE_FAMILY_0);
		            hTableDescriptor.addFamily(hColumnDescriptor);
		            hTableDescriptor.addCoprocessor(AggregateImplementation.class.getName());//增加聚合处理功能
		            admin.createTable(hTableDescriptor);  
				}
				exsitSpace.add(dataSpaceName);
            }
		} 
		catch (Exception e) {
			throw e;
		}
		finally {
			if (admin != null) {
				try {
					admin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void enableAggregate(Connection connection,String dataSpaceName) throws Exception {
		Admin admin = null;
		try {			
			admin = connection.getAdmin();
            TableName tbName = TableName.valueOf(dataSpaceName);
			HTableDescriptor htd = admin.getTableDescriptor(tbName);
			htd.addCoprocessor(AggregateImplementation.class.getName());
			admin.modifyTable(tbName, htd);
			admin.enableTable(tbName);
		} 
		catch (Exception e) {
			throw e;
		}
		finally {
			if (admin != null) {
				try {
					admin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public static void removeDataSpaceIfExist(Connection connection,String dataSpaceName)  throws Exception {
		Admin admin = null;
		try {
			admin = connection.getAdmin();
			TableName tbName = TableName.valueOf(dataSpaceName);
			if(admin.tableExists(tbName)) {
				admin.disableTable(tbName);
				//admin.deleteColumn(tbName, RDConstants.RD_HBASE_FAMILY_0);
				admin.deleteTable(tbName);
			}
			exsitSpace.remove(dataSpaceName);
		}
		finally {
			if(admin!=null)admin.close();
		}
	}

	/**
	 * 
	 */
	public static void updateById(RDTable rdTable, String recordId, Map<String, Object> updateRecord) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		Map<String,RDColumn> tableCols = rdTable.getRdColumns();

		RDSchema rdSchema = rdTable.getRdSchema();
		String recordRowKey = rdSchema.getUserId() +RDConstants._STR+rdTable.getRdSchema().getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR 
				+ RDConstants.RD_DATA_STR+RDConstants._STR+recordId;
		
		Put put = new Put(Bytes.toBytes(recordRowKey));
		Delete delete = new Delete(Bytes.toBytes(recordRowKey));
		Iterator<Entry<String,Object>> iterator = updateRecord.entrySet().iterator();
		RDColumn rdColumn = null;
		
		boolean hasSomeThingToDelete = false;
		while(iterator.hasNext()) {
			Entry<String,Object> tmp = iterator.next();
			rdColumn = tableCols.get(tmp.getKey());
			if(rdColumn==null) {
				continue;
			}
			if(tmp.getValue()!=null) {
				put.addColumn(RDConstants.FAMILY_BYTES, Bytes.toBytes(rdColumn.getSeq()), getBytesFromColumnVal(rdColumn,tmp.getValue()));
			}
			else {
				hasSomeThingToDelete = true;
				delete.addColumn(RDConstants.FAMILY_BYTES, Bytes.toBytes(rdColumn.getSeq()));
			}
		}
		table.put(put);
		if(hasSomeThingToDelete)table.delete(delete);
	}

	//把数据放入回收站
	public static void moveRecordIntoRecycle(RDTable rdTable, String recordId,String operator) throws Exception {
		HbaseDBConnection hbaseDBConnection = HbaseThreadHolder.getInstance().getThreadDatas();
		HTable dataTable = hbaseDBConnection.getDataTable();
		HTable recycleDataTable = hbaseDBConnection.getRecycleDataTable();
		
		RDSchema rdSchema = rdTable.getRdSchema();
		String recordRowKey = rdSchema.getUserId() +RDConstants._STR+rdTable.getRdSchema().getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR 
				+ RDConstants.RD_DATA_STR+RDConstants._STR+recordId;
		
		RDColumn updateDtColumn = null;
		RDColumn updateByColumn = null;
		for(RDColumn rdColumn :rdTable.getRdColumns().values()) {
			if(updateDtColumn==null&&rdColumn.getName().equals(RDEntity.UPDATE_BY)) {
				updateDtColumn = rdColumn;
			}
			else if(updateByColumn==null&&rdColumn.getName().equals(RDEntity.UPDATE_DT)) {
				updateByColumn = rdColumn;
			}
		}
		
		//copy data begin
		Put put = new Put(Bytes.toBytes(recordRowKey));
		Get getDeleteData = new Get(Bytes.toBytes(recordRowKey));
		try {
			Result r = dataTable.get(getDeleteData);
			Cell[] existData = r.rawCells();
			int cellLength = existData.length;
			
			if(cellLength>0) {
				for(int i = 0 ; i <existData.length ; i ++) {
					Cell tmp = existData[i];
					if(updateDtColumn!=null && updateDtColumn.getSeq() == i) {
						put.addColumn(RDConstants.FAMILY_BYTES, Bytes.toBytes(i), Bytes.toBytes(new Date().getTime()));
						updateDtColumn = null;
					}
					else if(updateByColumn!=null && updateByColumn.getSeq() == i) {
						put.addColumn(RDConstants.FAMILY_BYTES, Bytes.toBytes(i), Bytes.toBytes(operator));
						updateDtColumn = null;
					}
					else {
						put.addColumn(RDConstants.FAMILY_BYTES, Bytes.toBytes(i), CellUtil.cloneValue(tmp));
					}
				}
				recycleDataTable.put(put);
			}
		}
		catch(Exception e) {
			throw e;
		}
		//copy data end
		
		Delete delete = new Delete(Bytes.toBytes(recordRowKey));
		dataTable.delete(delete);
	}


	public static void deleteByRowKey(RDTable rdTable, String recordId) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		RDSchema rdSchema = rdTable.getRdSchema();
		String recordRowKey = rdSchema.getUserId() +RDConstants._STR+rdTable.getRdSchema().getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR 
				+ RDConstants.RD_DATA_STR+RDConstants._STR+recordId;
		Delete delete = new Delete(Bytes.toBytes(recordRowKey));
		table.delete(delete);
		
	}


	
}
