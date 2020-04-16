package cn.regionsoft.one.bigdata.criterias;

import cn.regionsoft.one.bigdata.enums.ConditionType;

public class RDCondition {
	private ConditionType contitionType = null;
	private Para para = null;
	private RDCondition[] childConditions = null;
	
	private RDCondition() {}
	
	public static RDCondition equal(String property, Object val) {
		RDCondition rdCondition = new RDCondition();
		rdCondition.contitionType = ConditionType.EQUAL;
		rdCondition.para = new Para(property, val);
		return rdCondition;
	}
	
	public static RDCondition contain(String property, Object val) {
		RDCondition rdCondition = new RDCondition();
		rdCondition.contitionType = ConditionType.CONTAIN;
		rdCondition.para = new Para(property, val);
		return rdCondition;
	}
//	public static RDCondition regx(String property, Object val) {
//		RDCondition rdCondition = new RDCondition();
//		rdCondition.contitionType = ConditionType.REGEX;
//		rdCondition.para = new Para(property, val);
//		return rdCondition;
//	}
	public static RDCondition startWith(String property, Object val) {
		RDCondition rdCondition = new RDCondition();
		rdCondition.contitionType = ConditionType.START_WITH;
		rdCondition.para = new Para(property, val);
		return rdCondition;
	}

//	public static RDCondition gt(String property, Object val) {
//		RDCondition rdCondition = new RDCondition();
//		rdCondition.contitionType = ConditionType.GT;
//		rdCondition.para = new Para(property, val);
//		return rdCondition;
//	}
//	
//	public static RDCondition lt(String property, Object val) {
//		RDCondition rdCondition = new RDCondition();
//		rdCondition.contitionType = ConditionType.LT;
//		rdCondition.para = new Para(property, val);
//		return rdCondition;
//	}

	public static RDCondition and(RDCondition... childConditions) {
		RDCondition rdCondition = new RDCondition();
		rdCondition.contitionType = ConditionType.AND;
		rdCondition.childConditions = childConditions;
		return rdCondition;
	}
	
	@Deprecated
	public static RDCondition or(RDCondition... childConditions) {
		RDCondition rdCondition = new RDCondition();
		rdCondition.contitionType = ConditionType.OR;
		rdCondition.childConditions = childConditions;
		return rdCondition;
	}

	public Para getPara() {
		return para;
	}

	public ConditionType getContitionType() {
		return contitionType;
	}

	public RDCondition[] getChildConditions() {
		return childConditions;
	}
	
	public void setChildConditions(RDCondition[] childConditions) {
		this.childConditions = childConditions;
	}

	public static class Para{
		private String key;
		private Object value;
		public Para(String key, Object value) {
			super();
			this.key = key;
			this.value = value;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
	}
}
