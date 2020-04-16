package cn.regionsoft.one.bigdata.impl;

import org.apache.hadoop.hbase.util.Bytes;

public class RDConstants {	
	public static final String RD_HBASE_FAMILY_0 = "fm0";
	public static final int RD_HBASE_CLOUMN_0 = 0;
	public static final String EMPTY_STR = "";
	
	public static final String ENABLED = "1";
	public static final String DISABLED = "0";
	
	
	public static final String _STR = "_";
	
//	public static final String RD_SCHEMA_STR = "S";
//	public static final String RD_APPID_STR = "A";
//	public static final String RD_TBLS_STR = "T";
//	public static final String RD_COLS_STR = "C";
//	public static final String RD_DATA_STR = "D";
//	public static final String RD_INDEX_STR = "I";
//	public static final String RD_ENABLE_STR = "E";
//	public static final String RD_DATATYPE_STR = "DT";
//	public static final String RD_LABEL_STR = "L";	
	public static final String RD_SCHEMA_STR = "schema";
    public static final String RD_APPIDS_STR = "appids";
	public static final String RD_TBLS_STR = "tbls";
	public static final String RD_COLS_STR = "cols";
	public static final String RD_DATA_STR = "data";
	public static final String RD_INDEX_STR = "index";
	public static final String RD_ENABLE_STR = "enable";
	public static final String RD_DATATYPE_STR = "datatype";
	public static final String RD_LABEL_STR = "label";

	public static final String RD_SEARCH_EOF = "EOF";
	
	public static final String RD_BEGIN_SUFFIX = "!";
	public static final String RD_END_SUFFIX = "~";
	
	public static final byte[] FAMILY_BYTES = Bytes.toBytes(RD_HBASE_FAMILY_0);
	

}
