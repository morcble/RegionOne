package cn.regionsoft.one.core;

import cn.regionsoft.one.data.persistence.Column;

public class CountResult{
	@Column(name = "count")
	private Long count;

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}
}
