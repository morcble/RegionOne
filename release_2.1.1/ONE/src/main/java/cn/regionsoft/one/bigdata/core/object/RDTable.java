package cn.regionsoft.one.bigdata.core.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;

import cn.regionsoft.one.bigdata.core.exceptions.DuplicateException;
import cn.regionsoft.one.bigdata.core.exceptions.ExistException;
import cn.regionsoft.one.bigdata.core.exceptions.NotFoundException;
import cn.regionsoft.one.bigdata.core.persist.RDEntity;
import cn.regionsoft.one.bigdata.enums.DataType;
import cn.regionsoft.one.bigdata.impl.RDConstants;
import cn.regionsoft.one.bigdata.impl.RDObject;
import cn.regionsoft.one.bigdata.impl.hbase.HbaseUtil;
import cn.regionsoft.one.bigdata.impl.hbase.HbaseThreadHolder;

public class RDTable extends RDObject{
	public RDTable(RDSchema rdSchema,String rdTableName,int seq) {
		this.name = rdTableName;
		this.rdSchema = rdSchema;
		this.seq = seq;
	}
	
	private RDSchema rdSchema;
	
	//列名－列
	private Map<String,RDColumn> rdColumns = new LinkedHashMap<String,RDColumn>();
	
	//列序号到列名的映射
	private Map<Integer,String> columnIndexToNameMap = new LinkedHashMap<Integer,String>();
	
	//序号
	private int seq;
	//索引
	private Map<String,RDIndex> rdIndexes = new LinkedHashMap<String,RDIndex>();
	
	//添加列
	public void createRdColumn(String rdColumnNm,String label ,DataType dataType) throws Exception {
		RDColumn rdColumn = HbaseUtil.createRdColumn(this, rdColumnNm,label, dataType);
		rdColumns.put(rdColumnNm, rdColumn);
		columnIndexToNameMap.put(rdColumn.getSeq(),rdColumn.getName());
	}
	
	//删除列
	public void deleteRDColumn(String rdColumnNm) throws Exception {
		deleteRDColumn(this,rdColumnNm);
		RDColumn rdColumn = rdColumns.remove(rdColumnNm);
		columnIndexToNameMap.remove(rdColumn.getSeq());
	}
	
	//添加索引
	public boolean createRdIndex(String... rdColumnNms) throws Exception {
		if(rdColumnNms==null||rdColumnNms.length==0)throw new Exception("rdColumnNms is empty");
		
		RDColumn rdColumn = null;
		List<RDColumn> indexColumns = new ArrayList<RDColumn>();
		for(String rdColNm:rdColumnNms) {
			rdColumn = rdColumns.get(rdColNm);
			if(rdColumn==null) {
				System.err.println("Column "+rdColNm+" is not found");
				return false;
			}
			indexColumns.add(rdColumn);
		}
		
		String indexName = genIndexName(indexColumns);
		try {
			
			HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
			int tableSeq = this.getSeq();

			String logicIndexKey = this.getRdSchema().getUserId()+RDConstants._STR
					+this.getRdSchema().getSeq() + RDConstants._STR + tableSeq + RDConstants._STR  + RDConstants.RD_INDEX_STR;
			String logicIndexFlagKey = logicIndexKey + RDConstants._STR +RDConstants.RD_ENABLE_STR;
			
			int indexSeq = HbaseUtil.createMetaData(new String[] {logicIndexKey},logicIndexFlagKey,new String[] {indexName},0);
			genIndexData(table,this,String.valueOf(indexSeq),indexColumns);
			RDIndex rdIndex = new RDIndex(indexName, indexSeq);
			rdIndexes.put(indexName, rdIndex);
			return true;
		}
		catch(ExistException existException) {
			StringBuilder sb = new StringBuilder("Index exsits for ");
			System.err.println(sb.toString());
			return false;
		}
	}
	
	public boolean deleteRdIndex(String... rdColumnNms) throws Exception {
		if(rdColumnNms==null||rdColumnNms.length==0)throw new Exception("rdColumnNms is empty");
		RDColumn rdColumn = null;
		List<RDColumn> indexColumns = new ArrayList<RDColumn>();
		for(String rdColNm:rdColumnNms) {
			rdColumn = rdColumns.get(rdColNm);
			if(rdColumn==null) return false;
			indexColumns.add(rdColumn);
		}
		
		String indexName = genIndexName(indexColumns);
		RDIndex rdIndex = rdIndexes.get(indexName);
		if(rdIndex==null) {
			System.err.println("index not found");
			return false;
		}
		
		deleteRdIndex(this,rdIndex);
		rdIndexes.remove(indexName);
		return true;
	}


	public RDSchema getRdSchema() {
		return rdSchema;
	}

	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	
	/**
	 * readonly
	 * @return
	 */
	public Map<String,RDColumn> getRdColumns() {
		return rdColumns;
	}
	
	public RDColumn getRdColumn(String rdColumnName) {
		return rdColumns.get(rdColumnName);
	}
	public Map<String, RDIndex> getRdIndexes() {
		return rdIndexes;
	}

	
	private String genIndexName(List<RDColumn> indexColumns) throws NotFoundException {
		Collections.sort(indexColumns, new Comparator<RDColumn>() {
			@Override
			public int compare(RDColumn o1, RDColumn o2) {
				if(o1.getSeq()>o2.getSeq())return 1;
				else if(o1.getSeq()<o2.getSeq()) return -1;
				else return 0;
			}
		});
		
		StringBuilder indexNameBuilder = new StringBuilder();
		int columnsCount = indexColumns.size();
		for(int i = 0 ; i<columnsCount; i ++) {
			indexNameBuilder.append(indexColumns.get(i).getSeq());
			if(i!=(columnsCount-1)) {
				indexNameBuilder.append(RDConstants._STR);
			}
		}
		
		return indexNameBuilder.toString();
	}
	
	public void registerColumn(RDColumn rdColumn) {
		rdColumns.put(rdColumn.getName(), rdColumn);
		columnIndexToNameMap.put(rdColumn.getSeq(),rdColumn.getName());
	}
	
	public void registerIndex(RDIndex rdIndex) {
		rdIndexes.put(rdIndex.getName(), rdIndex);
	}
	
	/**
	 * 从DB获取最新的表结构
	 * @throws Exception 
	 */
	public void refresh() throws Exception {
		RDTable rdTable = HbaseUtil.getRdTable(this.rdSchema,this.name);
		this.rdColumns = rdTable.getRdColumns();
		this.rdIndexes = rdTable.getRdIndexes();
		this.columnIndexToNameMap = rdTable.getColumnIndexToNameMap();
	}

	public void insert(Map<String, Object> newRecord) throws Exception {
		HbaseUtil.insert(this, newRecord);
	}

	public Map<Integer, String> getColumnIndexToNameMap() {
		return columnIndexToNameMap;
	}

	public void setColumnIndexToNameMap(Map<Integer, String> columnIndexToNameMap) {
		this.columnIndexToNameMap = columnIndexToNameMap;
	}


	private static void genIndexData(HTable table,RDTable rdTable,String indexSeq, List<RDColumn> indexColumns) throws Exception {
		int pageSize = 200;
		int insertBatchSize = 1000;
		Scan scan = new Scan();
		Filter pageFilter = new PageFilter(pageSize+1);
		scan.setFilter(pageFilter);
		
		//scan.setCaching(1000);
		scan.setRowPrefixFilter(Bytes.toBytes(rdTable.getRdSchema().getUserId()
				+RDConstants._STR+rdTable.getRdSchema().getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR + RDConstants.RD_DATA_STR));	
		
		String startRowKey = null;
		String rowKey = null;
		String recordId = null;
		String indexKeyPrefix = rdTable.getRdSchema().getUserId()+RDConstants._STR
				+rdTable.getRdSchema().getSeq()+RDConstants._STR+rdTable.getSeq()+RDConstants._STR+RDConstants.RD_INDEX_STR
				+ RDConstants._STR + RDConstants.RD_DATA_STR + RDConstants._STR + indexSeq + RDConstants._STR;
		List<Put> puts = new ArrayList<Put>();
		while(true) {
			ResultScanner resultScanner = table.getScanner(scan);
			int count = 0;
			try {
				for (Result resRow : resultScanner) {
					count++;					
					if (count==pageSize + 1) {
		                startRowKey = Bytes.toString(resRow.getRow());
		                scan.setStartRow(startRowKey.getBytes());
		                break;
		            }
					
					rowKey = Bytes.toString(resRow.getRow());
					recordId = rowKey.substring(rowKey.lastIndexOf(RDConstants._STR)+1);
					
					StringBuilder indexKey = new StringBuilder();
					indexKey.append(indexKeyPrefix);
					
					for(RDColumn rdColumn:indexColumns) {
						Cell tmpCell = resRow.getColumnLatestCell(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0), Bytes.toBytes(rdColumn.getSeq()));
						indexKey.append(String.valueOf(HbaseUtil.getColumnValFromCell(rdColumn,tmpCell))); 
						indexKey.append(RDConstants._STR);
					}
					indexKey.append(recordId);
					
					Put tableInfoOp = new Put(Bytes.toBytes(indexKey.toString()));
					tableInfoOp.addColumn(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0), Bytes.toBytes(0), Bytes.toBytes(0L));
					puts.add(tableInfoOp);
					if(puts.size()>insertBatchSize) {
						table.put(puts);
						puts = new ArrayList<Put>();
					}
					//Sample_0_INDEX_0_DATA_feng0_acc0_4259726279131136 : 4259726279131136
		        }
				
				if(count < pageSize + 1) {
					break;
				}
				
			}
			finally {
				resultScanner.close();
			}
		}
		
		if(puts.size()>0)
			table.put(puts);
	}

	private static boolean deleteRDColumn(RDTable rdTable, String rdColumnNm) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		RDColumn rdColumn = rdTable.getRdColumn(rdColumnNm);
		if(rdColumn==null) {
			rdTable.refresh();
			rdColumn = rdTable.getRdColumn(rdColumnNm);
		}
		
		if(rdColumn==null) {
			System.err.println("Column not found : "+rdColumnNm);
			return false;
		}
		
		Iterator<String> iterator = rdTable.getRdIndexes().keySet().iterator();
		String indexName = null;
		String[] tmpArray = null;
		while(iterator.hasNext()) {
			indexName = iterator.next();
			tmpArray = indexName.split(RDConstants._STR);
			for(String tmpStr:tmpArray) {
				if(Integer.valueOf(tmpStr).intValue()==rdColumn.getSeq()) {
					//删除index 及index data
					deleteRdIndex(rdTable, rdTable.getRdIndexes().get(indexName));//TODO
					break;
				}
			}
		}
		int tableSeq = rdTable.getSeq();

		String logicColsFlagKey = rdTable.getRdSchema().getUserId()+RDConstants._STR
				+rdTable.getRdSchema().getSeq()+RDConstants._STR+tableSeq+RDConstants._STR + RDConstants.RD_COLS_STR 
				+RDConstants._STR+RDConstants.RD_ENABLE_STR;

		Get get = new Get(logicColsFlagKey.getBytes());
		Result r = table.get(get);
		Cell cell = r.getColumnLatestCell(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0), Bytes.toBytes(rdColumn.getSeq()));
		String flag =  Bytes.toString(CellUtil.cloneValue(cell));
		if(RDConstants.ENABLED.equals(flag)) {
			Put putOp = new Put(Bytes.toBytes(logicColsFlagKey.toString()));
			putOp.addColumn(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0), Bytes.toBytes(rdColumn.getSeq()), Bytes.toBytes(RDConstants.DISABLED));
			table.put(putOp);
			return true;
		}
		else {
			System.err.println("No Column found for deletion");
			return false;
		}
	}
	
	
	//删除索引及索引数据
	private static boolean deleteRdIndex(RDTable rdTable, RDIndex rdIndex) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		int tableSeq = rdTable.getSeq();

		String logicIndexFlagKey = rdTable.getRdSchema().getUserId()+RDConstants._STR
				+rdTable.getRdSchema().getSeq() + RDConstants._STR + tableSeq + RDConstants._STR + RDConstants.RD_INDEX_STR 
				+ RDConstants._STR +RDConstants.RD_ENABLE_STR;
		
		Get get = new Get(logicIndexFlagKey.getBytes());
		Result r = table.get(get);
		Cell cell = r.getColumnLatestCell(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0), Bytes.toBytes(rdIndex.getSeq()));
		String flag =  Bytes.toString(CellUtil.cloneValue(cell));
		if(RDConstants.ENABLED.equals(flag)) {
			deleteIndexData(rdTable,rdIndex.getSeq());
			
			Put putOp = new Put(Bytes.toBytes(logicIndexFlagKey.toString()));
			putOp.addColumn(Bytes.toBytes(RDConstants.RD_HBASE_FAMILY_0), Bytes.toBytes(rdIndex.getSeq()), Bytes.toBytes(RDConstants.DISABLED));
			table.put(putOp);
			return true;
		}
		else {
			System.err.println("No index found for deletion");
			return false;
		}
	}
	
	
	private static void deleteIndexData(RDTable rdTable, int indexSeq) throws Exception {
		HTable table = HbaseThreadHolder.getInstance().getThreadDatas().getDataTable();
		String indexDataStartKey = rdTable.getRdSchema().getUserId()+RDConstants._STR
				+rdTable.getRdSchema().getSeq()+ RDConstants._STR + rdTable.getSeq()+RDConstants._STR 
				+RDConstants.RD_INDEX_STR +RDConstants._STR +RDConstants.RD_DATA_STR+RDConstants._STR +indexSeq;
	
		HbaseUtil.deleteByRowKeyStartWith(table,indexDataStartKey);
	}

}
