package cn.regionsoft.one.bigdata.impl.hbase;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.util.Bytes;

import cn.regionsoft.one.core.CommonUtil;

/**
 * not thread safe
 * @author fenglj
 *
 */
public class HbaseDBConnection{
	private HTable dataTable;
	private boolean autoCommit = true;
	
	private Connection connection;
	private AggregationClient aggregationClient;
	
	private String dataSpaceName;
	private Configuration configuration;
	
	private HTable recycleDataTable;
	
	public HbaseDBConnection(Configuration configuration,String dataSpaceName) throws Exception {
		try {
			this.connection = ConnectionFactory.createConnection(configuration);
			this.aggregationClient = new AggregationClient(configuration);
			this.dataSpaceName = dataSpaceName;
			this.configuration = configuration;
            TableName tbName = TableName.valueOf(this.dataSpaceName);
            dataTable = (HTable) connection.getTable(tbName);
            dataTable.setWriteBufferSize(6 * 1024 * 1024); 
            
            
            String dataRecycleSpaceName = dataSpaceName+"_recycle";
            TableName recycleTbName = TableName.valueOf(dataRecycleSpaceName);
            recycleDataTable = (HTable) connection.getTable(recycleTbName);
            recycleDataTable.setWriteBufferSize(6 * 1024 * 1024); 
        } catch (Exception e) {
            throw e;
        }
	}
	
	public HbaseDBConnection(Connection connection ,Configuration configuration,String dataSpaceName) throws Exception {
		try {
			this.connection = connection;
			this.aggregationClient = new AggregationClient(configuration);
			this.dataSpaceName = dataSpaceName;
			this.configuration = configuration;
            TableName tbName = TableName.valueOf(dataSpaceName);
            dataTable = (HTable) connection.getTable(tbName);
            dataTable.setWriteBufferSize(6 * 1024 * 1024); 
            
            
            String dataRecycleSpaceName = dataSpaceName+"_recycle";
            TableName recycleTbName = TableName.valueOf(dataRecycleSpaceName);
            recycleDataTable = (HTable) connection.getTable(recycleTbName);
            recycleDataTable.setWriteBufferSize(6 * 1024 * 1024); 
        } catch (Exception e) {
            throw e;
        }
	}
	
	public void setAutoCommit(boolean autoPara) throws IOException {
		if(autoCommit == autoPara)return;
		
		if(autoPara) {
			dataTable.flushCommits();
			dataTable.setAutoFlushTo(true);
		}
		else {
			dataTable.setAutoFlushTo(false);
		}
		autoCommit = autoPara;
	}

	public HTable getDataTable() {
		return dataTable;
	}

	public Connection getConnection() {
		return connection;
	}


	public AggregationClient getAggregationClient() {
		return aggregationClient;
	}

	public void testConnectivity() throws Exception {
		Admin admin = null;
		try {
			admin = connection.getAdmin();
	        TableName tbName = TableName.valueOf(this.dataSpaceName);
	        admin.tableExists(tbName);
		}
		catch(Throwable e) {
			CommonUtil.closeQuietly(this.connection);
			this.connection = ConnectionFactory.createConnection(configuration);
			
			
			TableName tbName = TableName.valueOf(this.dataSpaceName);
            dataTable = (HTable) connection.getTable(tbName);
            dataTable.setWriteBufferSize(6 * 1024 * 1024); 
            
            
            String dataRecycleSpaceName = dataSpaceName+"_recycle";
            TableName recycleTbName = TableName.valueOf(dataRecycleSpaceName);
            recycleDataTable = (HTable) connection.getTable(recycleTbName);
            recycleDataTable.setWriteBufferSize(6 * 1024 * 1024); 
		}
		finally {
			CommonUtil.closeQuietly(admin);
		}
		
		try {
			Scan scan = new Scan();
			scan.withStartRow(Bytes.toBytes(0));
			scan.withStopRow(Bytes.toBytes(0));
			scan.setRowPrefixFilter(Bytes.toBytes(0));
			
			this.aggregationClient.rowCount(HbaseThreadHolder.getInstance().getThreadDatas().getDataTable(), new LongColumnInterpreter(), scan);
		}
		catch(Throwable e) {
			CommonUtil.closeQuietly(this.aggregationClient);
			this.aggregationClient = new AggregationClient(configuration);
		}
	}

	public HTable getRecycleDataTable() {
		return recycleDataTable;
	}
}
