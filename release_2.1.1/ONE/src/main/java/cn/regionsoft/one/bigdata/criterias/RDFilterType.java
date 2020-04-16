package cn.regionsoft.one.bigdata.criterias;

/**
 *查询条件类别
 */
public enum RDFilterType {
	EQUAL,
	GT,
	LT,
	AND,
	OR,
	//字符串比较
	CONTAIN,
	START_WITH,
	STOP_WITH, 
	REGEX;

}
