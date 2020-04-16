package cn.regionsoft.one.core.dbconnection;

import java.util.Date;

public interface H2OConnection {
	public Date getLastUseDt();
	public void setLastUseDt(Date lastUseDt);
	public void commit() throws Exception;
	public void rollback() throws Exception;
	void setAutoCommit(boolean autoCommit) throws Exception;
}
