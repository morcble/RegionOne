package cn.regionsoft.one.data.persistence;

import java.util.Date;

public interface H2OEntity<T> {
	public T getId();

	public void setId(T id);

	public void setCreateDt(Date createDt);
}
