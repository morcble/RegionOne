package cn.regionsoft.one.data.persistence.criteria;

/**
 * 查询条件
 * @author fenglj
 *
 */
public class Condition {
	private String fieldName;
	private Operator operator;
	private Object val;
	public Condition( String fieldName, Operator operator, Object val) {
		super();
		this.fieldName = fieldName;
		this.operator = operator;
		this.val = val;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public Operator getOperator() {
		return operator;
	}
	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	public Object getVal() {
		return val;
	}
	public void setVal(Object val) {
		this.val = val;
	}
}
