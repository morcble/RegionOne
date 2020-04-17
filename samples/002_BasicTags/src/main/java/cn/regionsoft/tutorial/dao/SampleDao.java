package cn.regionsoft.tutorial.dao;

import cn.regionsoft.one.annotation.Dao;

@Dao
public class SampleDao {
	public String queryDb(String para) {
		return "dumydata_"+para;
	}
}
