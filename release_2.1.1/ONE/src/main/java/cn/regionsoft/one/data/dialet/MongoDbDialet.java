package cn.regionsoft.one.data.dialet;

import java.sql.Types;
import java.text.MessageFormat;

import cn.regionsoft.one.data.dialet.core.NoSQLDialet;
import cn.regionsoft.one.data.dialet.core.SQLDialet;



public class MongoDbDialet extends NoSQLDialet{ 
	
	public MongoDbDialet(){
		//init();
	}
	public void config(){
		/*typeMapping.put(Integer.class, Types.INTEGER);
	    typeMapping.put(Long.class, Types.BIGINT);
	    typeMapping.put(Float.class, Types.REAL);
	    typeMapping.put(Double.class, Types.DOUBLE);
	    typeMapping.put(Byte[].class, Types.BINARY);
	    typeMapping.put(Boolean.class, Types.BIT);
		typeMapping.put(String.class,Types.VARCHAR);
	    typeMapping.put(java.math.BigDecimal.class, Types.NUMERIC);
	    
	    
	    typeMapping.put(java.sql.Date.class, Types.DATE);
	    typeMapping.put(java.util.Date.class, Types.DATE);
	    typeMapping.put(java.sql.Time.class, Types.TIME);
	    typeMapping.put(java.sql.Timestamp.class, Types.TIMESTAMP);
	    typeMapping.put(java.sql.Clob.class, Types.CLOB);
	    typeMapping.put(java.sql.Blob.class, Types.BLOB);
	    
	    //--------------------------------
	    typeMapString.put(-7,"BIT");
		typeMapString.put(-6,"TINYINT");
		typeMapString.put(5,"SMALLINT");
		typeMapString.put(4,"INTEGER");
		typeMapString.put(-5,"BIGINT");
		typeMapString.put(6,"FLOAT");
		typeMapString.put(7,"REAL");
		typeMapString.put(8,"DOUBLE");
		typeMapString.put(2,"NUMERIC");
		typeMapString.put(3,"DECIMAL");
		typeMapString.put(1,"CHAR");
		typeMapString.put(12,"VARCHAR");
		typeMapString.put(-1,"LONGVARCHAR");
		typeMapString.put(91,"DATE");
		typeMapString.put(92,"TIME");
		typeMapString.put(93,"TIMESTAMP");
		typeMapString.put(-2,"BINARY");
		typeMapString.put(-3,"VARBINARY");
		typeMapString.put(-4,"LONGVARBINARY");
		typeMapString.put(2003,"ARRAY");
		typeMapString.put(2004,"BLOB");
		typeMapString.put(2005,"CLOB");
		typeMapString.put(2006,"REF");
		typeMapString.put(16,"BOOLEAN");
		typeMapString.put(-8,"ROWID");
		typeMapString.put(-15,"NCHAR");
		typeMapString.put(-9,"NVARCHAR");
		typeMapString.put(-16,"LONGNVARCHAR");
		typeMapString.put(2011,"NCLOB");
		typeMapString.put(2009,"SQLXML");*/
	}
	
	public String getTableExsitsQuery(String tableName,String schema) {
		return MessageFormat.format("select count(*) as count from sqlite_master where type=''table'' and name=''{0}''", tableName);
	}
	

	
	
	
}
