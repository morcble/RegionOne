package cn.regionsoft.one.bigdata.enums;

/**
 *查询条件类别
 */
public enum ConditionType {
	EQUAL, 
	GT,
	LT,
	AND,
	OR,
	//字符串比较
	REGEX,//正则
	CONTAIN,
	START_WITH,
	END_WITH;

}
