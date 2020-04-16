package cn.regionsoft.one.bigdata.test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.coprocessor.AggregateImplementation;
import org.apache.hadoop.hbase.util.Bytes;

import cn.regionsoft.one.bigdata.core.exceptions.ExistException;
import cn.regionsoft.one.bigdata.core.object.RDColumn;
import cn.regionsoft.one.bigdata.core.object.RDSchema;
import cn.regionsoft.one.bigdata.core.object.RDTable;
import cn.regionsoft.one.bigdata.core.persist.RDEntity;
import cn.regionsoft.one.bigdata.core.persist.RDEntityListResult;
import cn.regionsoft.one.bigdata.criterias.RDCondition;
import cn.regionsoft.one.bigdata.criterias.RDCriteria;
import cn.regionsoft.one.bigdata.enums.DataType;
import cn.regionsoft.one.bigdata.ids.IDProducer;
import cn.regionsoft.one.bigdata.impl.RDConstants;
import cn.regionsoft.one.bigdata.impl.hbase.HbaseUtil;
import cn.regionsoft.one.bigdata.impl.hbase.HbaseDBConnection;
import cn.regionsoft.one.bigdata.impl.hbase.HbaseThreadHolder;
import cn.regionsoft.one.core.ContextConfig;
import cn.regionsoft.one.core.ids.IDGenerator;
import cn.regionsoft.one.utils.ThreadData;
import cn.regionsoft.one.utils.ThreadHolder;
import com.google.gson.JsonObject;

/*
 * htable 批量查rowkey https://www.cnblogs.com/10158wsj/p/8214583.html
 * 
 * 
 * https://blog.csdn.net/lvwenyuan_1/article/details/78422567
 
#meta 一个app对应一个schema?
SCHEMA_USERID : [0:SAMPLE] 
SCHEMA_USERID_APPID:[0:APPID]
SCHEMA_USERID_PROGRAMID:[0:PROGRAMID]
SCHEMA_USERID_ENABLE:[0:1]

USERID_APPID_0_TBLS : [0:USER] 
USERID_APPID_0_TBLS_ENABLE : [0:1]
//USERID_APPID  schemaSeq  tableSeq
USERID_APPID_0_0_COLS : [0:NAME] [1:PWD] [2:ACC] 
USERID_APPID_0_0_COLS_LABEL : [0:名字] [1:密码] [2:帐号]
USERID_APPID_0_0_COLS_DATATYPE : [0:STRING] [1:STRING] [2:STRING] 
USERID_APPID_0_0_COLS_ENABLE : [0:1] [1:1] [2:1] 

USERID_APPID_0_0_DATA_4276725086617600 : [0:feng0] [1:password0] [2:acc0] 
USERID_APPID_0_0_DATA_4276725086617601 : [0:feng1] [1:password1] [2:acc1] 
USERID_APPID_0_0_DATA_4276725086617602 : [0:feng2] [1:password2] [2:acc2] 
USERID_APPID_0_0_DATA_4276725086617603 : [0:feng3] [1:password3] [2:acc3] 
USERID_APPID_0_0_DATA_4276725086617604 : [0:feng4] [1:password4] [2:acc4]


USERID_APPID_0_0_INDEX_DATA_0_feng0_password0_4276725086617600 : [0:4276725086617600] 
USERID_APPID_0_0_INDEX_DATA_0_feng1_password1_4276725086617601 : [0:4276725086617601] 
USERID_APPID_0_0_INDEX_DATA_0_feng2_password2_4276725086617602 : [0:4276725086617602] 
USERID_APPID_0_0_INDEX_DATA_0_feng3_password3_4276725086617603 : [0:4276725086617603] 
USERID_APPID_0_0_INDEX_DATA_0_feng4_password4_4276725086617604 : [0:4276725086617604]
USERID_APPID_0_0_INDEX : [0:0_1] 
USERID_APPID_0_0_INDEX_ENABLE : [0:1] 


新增dataspace	completed
删除dataspace	completed

新增schema	completed
删除schema	completed

新增column	completed
删除column(如果在索引里 需要先删除索引)	completed 

新增table	completed
删除table结构及数据	completed 
清空table数据		completed

增加index	completed 
删除index	completed 

新增数据		completed
批量新增数据	completed
主键查询数据	completed 
主键修改数据	TODO   index
主键删除数据	TODO   index
条件批量删除数据	TODO   index
条件批量修改数据	TODO   index
条件查询数据  completed  按index查询数据 TODO
性能测试	TODO
*/

public class Test {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Throwable {
		String dataSpaceName = "RDDATA26";
		String appOwner="liangjun";
		String appName="testAppId";
		String schema = "MorcblecloudDB";
		
		Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("hbase.zookeeper.quorum", "hbase-docker");
        //configuration.set("hbase.master", "hbase-docker:60000");
        Connection connection = ConnectionFactory.createConnection(configuration);
        //HbaseUtil.removeDataSpaceIfExist(connection,dataSpaceName);
		HbaseThreadHolder.getInstance().setThreadDatas(new HbaseDBConnection(connection,configuration,dataSpaceName));
		//HbaseUtil.enableAggregate(connection, dataSpaceName);
		Scan scan = new Scan();
		scan.withStartRow(Bytes.toBytes(0));
		scan.withStopRow(Bytes.toBytes(0));
		scan.setRowPrefixFilter(Bytes.toBytes(0));

//		HbaseUtil.printAllData();
//		HbaseUtil.createRDSchema(appOwner,"app2","app2DB");
//		RDSchema rdSchema11 = HbaseUtil.getRDSchema(appOwner,"app2");
//		HbaseUtil.createRdTable(rdSchema11,"CloudSample");
//		RDTable rdTableTmp11 = HbaseUtil.getRdTable(rdSchema11,"CloudSample");
//		rdTableTmp11.createRdColumn("name","Name",DataType.STRING);
//		rdTableTmp11.createRdColumn("desc","Desc",DataType.STRING);

		HbaseUtil.printAllData();
		HbaseUtil.setIDProducer(new IDProducer() {

			@Override
			public Long getNextLongId() {
				// TODO Auto-generated method stub
				try {
					return IDGenerator.getLongID();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
			
		});//TODO

		//HbaseUtil.removeDataSpaceIfExist(connection,dataSpaceName);
		
		HbaseUtil.createDataSpaceIfNotExist(connection,dataSpaceName);

		if(false) {
			//HbaseUtil.deleteRDSchema(cloudId,"Sample");
			HbaseUtil.printAllData();
			//HbaseUtil.createRDSchema(appOwner,appName,schema);
			HbaseUtil.printAllData();
			
			RDSchema rdSchema = HbaseUtil.getRDSchema(appOwner,appName);
			HbaseUtil.createRdTable(rdSchema,"USER");

			RDTable rdTableTmp = HbaseUtil.getRdTable(rdSchema,"USER");
			HbaseUtil.printAllData();
			rdTableTmp.createRdColumn("name","名字",DataType.STRING);
			rdTableTmp.createRdColumn("pwd","密码",DataType.STRING);
			rdTableTmp.createRdColumn("acc","帐号",DataType.STRING);
			HbaseUtil.printAllData();
		}
		//HbaseUtil.deleteByRowKeyStartWith(HbaseThreadHolder.getInstance().getThreadDatas().getHtable(),"XXXXX_4_0_INDEX");
		//HbaseUtil.deleteByRowKeyStartWith(HbaseThreadHolder.getInstance().getThreadDatas().getHtable(),"XXXXX_4_0_DATA");

		//HbaseUtil.deleteByRowKeyStartWith(HbaseThreadHolder.getInstance().getThreadDatas().getHtable(), "XXXXX_3_0_INDEX");
		RDSchema rdSchema = HbaseUtil.getRDSchema(appOwner,appName);
		RDTable rdTable = HbaseUtil.getRdTable(rdSchema,"USER");
		//rdTable.deleteRdIndex("acc","lastIp");
		rdTable.createRdIndex("acc","pwd");
		//rdTable.deleteRdIndex("ACC","PWD");
		//rdTable.createRdIndex("ACC","PWD");
		
		//Long count2 = HbaseUtil.getAmount(rdTable, null);
		//System.out.println(count2);
		//rdTable.deleteRdIndex("acc","lastIp");
		//HbaseUtil.printAllData();
		
		//rdTable.createRdIndex("acc","lastIp");
		HbaseUtil.printAllData();
		System.out.println(1);
		//rdDataSourceWrapper.printAllData();
		//rdTable.deleteRDColumn("NAME");
		//HbaseUtil.printAllData();
		System.out.println(1);
		//rdTable.deleteRDColumn("NAME");
		//rdDataSourceWrapper.printAllData();
	//	HbaseUtil.clearTable(rdSchema, "USER");
//		rdDataSourceWrapper.printAllData();
//		rdSchema.deleteTable("USER");
//		rdDataSourceWrapper.printAllData();

		
		Map<String,Object> newRecord = new LinkedHashMap<String,Object>();

		/**
		 * 增		
		 */
		if(true) {
			//使用缓存
			//RDTable rdTable = rdDataSourceWrapper.getRdTable("Sample","USER");

			//使用批量
			HbaseThreadHolder.getInstance().getThreadDatas().setAutoCommit(false);
			long time2  = System.currentTimeMillis();
			
			int k = 0 ;
			for(int i = 0 ; i <10 ; i ++) {
				newRecord.put("name", "feng" + i);
				newRecord.put("pwd", "password" + i);
				newRecord.put("acc", "acc" + i);
				rdTable.insert(newRecord);
			}
			
			HbaseThreadHolder.getInstance().getThreadDatas().setAutoCommit(true);
			HbaseUtil.printAllData();
			System.out.println(System.currentTimeMillis()-time2);
			System.out.println("insert completed");
		}
	
		
		if(false) {
			//使用缓存
			//RDTable rdTable = rdDataSourceWrapper.getRdTable("Sample","USER");

			//使用批量
			//rdDataSourceWrapper.beginBatch();
			long time2  = System.currentTimeMillis();
			
			int k = 0 ;
			for(int i = 10 ; i <100 ; i ++) {
				for(int j = 0 ; j <1000000 ; j ++) {
					 k = i*1000000 + j;
					 
					newRecord.put("NAME", "feng" + k);
					newRecord.put("PWD", "password" + k);
					newRecord.put("ACC", "acc" + k);
					rdTable.insert(newRecord);
				}
				
				System.out.println(k);
			}
			
			//rdDataSourceWrapper.endBatch();
			
			System.out.println(System.currentTimeMillis()-time2);
			System.out.println("insert completed");
		}
		
		
		RDEntity rdEntity = HbaseUtil.findById(rdTable,"4650923910856966145");
		System.out.println(1);
		//rdDataSourceWrapper.updateById(rdTable, rdEntity);
		/**
		 * 删除		
		 */	
		if(false) {
			
			int pageSize = 10;
			int currentPage = 2;
			String lastRowKey = null;
			String rowkeyReg = null;

			
		}
		
		/**
		 * 改		
		 */
		
		
		/**
		 * 查		
		 */
		
		if(true) {
			
			int pageSize = 10;
			int currentPage = 1;
			String rowkeyReg = null;
			
			RDCriteria criteria = RDCriteria.create(
						RDCondition.and(RDCondition.equal("name","feng4"),RDCondition.startWith("pwd","password4")));
//			RDCriteria criteria = RDCriteria.create(
//						RDCondition.and(
//								RDCondition.and(
//										RDCondition.equal("name","feng1"),
//										RDCondition.lt("acc","acc1")
//										),
//								RDCondition.and(
//										RDCondition.startWith("name","feng0"),
//										RDCondition.lt("acc","acc2")
//										)
//								)
//					);
//			RDCriteria criteria = RDCriteria.create(
//					RDCondition.or(
//							RDCondition.and(RDCondition.startWith("name","feng0"),RDCondition.startWith("acc","acc0")),
//							RDCondition.and(RDCondition.startWith("name","feng1"),RDCondition.startWith("acc","acc1")))
//					);
			//RDCriteria criteria1 = RDCriteria.create(RDCondition.and(RDCondition.equal("name","feng"),RDCondition.equal("acc","acc1")));
			//RDCriteria criteria = RDCriteria.create(RDCondition.and(RDCondition.startWith("name","feng"),RDCondition.startWith("acc","acc1")));
			//模糊查询
			System.out.println("search result with criteria");
			long time2  = System.currentTimeMillis();
			RDEntityListResult result1 = HbaseUtil.getList(rdTable,pageSize,currentPage, criteria);
			System.out.println(System.currentTimeMillis()-time2);
			
			time2  = System.currentTimeMillis();
			
			RDEntityListResult result2 = HbaseUtil.getList(rdTable,pageSize,2, criteria);
			System.out.println(System.currentTimeMillis()-time2);
			
			time2 = System.currentTimeMillis();
			RDEntityListResult result3 = HbaseUtil.getList(rdTable, pageSize, result1.getNextStartRowKey(), criteria);
			System.out.println(System.currentTimeMillis()-time2);
			
			//System.out.println(result);
			System.out.println("search result without criteria");
			time2  = System.currentTimeMillis();
		//	RDListResult result4 = rdDataSourceWrapper.getList(rdTable,pageSize,currentPage, null);
			System.out.println(System.currentTimeMillis()-time2);
			//System.out.println(result3);
			
		}
		
		/**
		 * 索引		
		 */
	}

}
