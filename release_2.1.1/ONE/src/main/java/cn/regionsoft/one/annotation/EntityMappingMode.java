package cn.regionsoft.one.annotation;

public enum EntityMappingMode {
	CREATE,//create the tables which not exsits
	DROP_CREATE,//drop table before create table
	NONE;//do nothing for the tables
}
