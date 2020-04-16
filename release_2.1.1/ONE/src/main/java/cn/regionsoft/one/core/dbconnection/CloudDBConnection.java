package cn.regionsoft.one.core.dbconnection;

import java.util.Date;
import cn.regionsoft.one.bigdata.impl.hbase.HbaseDBConnection;

/**
 * not thread safe
 * @author fenglj
 *
 */
public class CloudDBConnection implements H2OConnection{
	private Date createDt = new Date();
	private Date lastUseDt = createDt;
	private HbaseDBConnection hbaseDBConnection;
	
	public HbaseDBConnection getHbaseDBConnection() {
		return hbaseDBConnection;
	}
	
	public CloudDBConnection(HbaseDBConnection hbaseDBConnection) {
		this.hbaseDBConnection = hbaseDBConnection;
	}
	
	@Override
	public void setAutoCommit(boolean autoPara) throws Exception {
		hbaseDBConnection.setAutoCommit(autoPara);
	}

	@Override
	public Date getLastUseDt() {
		return lastUseDt;
	}


	@Override
	public void setLastUseDt(Date lastUseDt) {
		this.lastUseDt = lastUseDt;
	}

	
	/**
	 * not implemented
	 */
	@Override
	public void commit() throws Exception {
		// TODO Auto-generated method stub
		
	}

	/**
	 * not implemented
	 */
	@Override
	public void rollback() throws Exception {
		// TODO Auto-generated method stub
		
	}


	

}
