package cn.regionsoft.one.data.persistence.criteria;

import java.util.ArrayList;
import java.util.List;

public class Query{
	private List<Condition> conditions = new ArrayList<Condition>();
	
	public <T> Query addCondition(Condition condition) {
		conditions.add(condition);
		return this;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

}
