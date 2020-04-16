package cn.regionsoft.one.data.dialet;

import cn.regionsoft.one.bigdata.enums.DataType;
import cn.regionsoft.one.data.dialet.core.NoSQLDialet;

public class CloudDbDialet extends NoSQLDialet{ 
	public CloudDbDialet(){
		init();
	}
	
	public DataType getDataTypeByClass(Class javaClass){
		return typeMapping.get(javaClass);
	}
	
	@Override
	public void config() {
		typeMapping.put(Integer.class, DataType.INT);
	    typeMapping.put(Long.class, DataType.LONG);
	    typeMapping.put(Float.class, DataType.FLOAT);
	    typeMapping.put(Double.class, DataType.DOUBLE);
	    typeMapping.put(Boolean.class, DataType.BOOLEAN);
		typeMapping.put(String.class,DataType.STRING);
	    typeMapping.put(java.math.BigDecimal.class, DataType.BIGDECIMAL);
	    
	    
	    typeMapping.put(java.sql.Date.class, DataType.DATE);
	    typeMapping.put(java.util.Date.class, DataType.DATE);
	    typeMapping.put(java.sql.Timestamp.class, DataType.DATE);

//	    //--------------------------------
//	    typeMapString.put(-7,"BIT");
//		typeMapString.put(-6,"TINYINT");
//		typeMapString.put(5,"SMALLINT");
//		typeMapString.put(4,"INTEGER");
//		typeMapString.put(-5,"BIGINT");
//		typeMapString.put(6,"FLOAT");
//		typeMapString.put(7,"REAL");
//		typeMapString.put(8,"DOUBLE");
//		typeMapString.put(2,"NUMERIC");
//		typeMapString.put(3,"DECIMAL");
//		typeMapString.put(1,"CHAR");
//		typeMapString.put(12,"VARCHAR");
//		typeMapString.put(-1,"LONGVARCHAR");
//		typeMapString.put(91,"DATETIME");
//		typeMapString.put(92,"TIME");
//		typeMapString.put(93,"DATETIME");
//		typeMapString.put(-2,"BINARY");
//		typeMapString.put(-3,"VARBINARY");
//		typeMapString.put(-4,"LONGVARBINARY");
//		typeMapString.put(2003,"ARRAY");
//		typeMapString.put(2004,"BLOB");
//		typeMapString.put(2005,"CLOB");
//		typeMapString.put(2006,"REF");
//		typeMapString.put(16,"BOOLEAN");
//		typeMapString.put(-8,"ROWID");
//		typeMapString.put(-15,"NCHAR");
//		typeMapString.put(-9,"NVARCHAR");
//		typeMapString.put(-16,"LONGNVARCHAR");
//		typeMapString.put(2011,"NCLOB");
//		typeMapString.put(2009,"SQLXML");
	}

}
