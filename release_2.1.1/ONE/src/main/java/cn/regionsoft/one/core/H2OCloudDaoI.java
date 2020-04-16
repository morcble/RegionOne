package cn.regionsoft.one.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.regionsoft.one.bigdata.core.persist.RDEntityListResult;
import cn.regionsoft.one.data.persistence.H2OEntity;

public interface H2OCloudDaoI<T extends H2OEntity, I> {

	/**
	 * create instance in data storage
	 * @param instance
	 * @return
	 * @throws Exception 
	 */
	T insert(T instance) throws Exception;

	T find(I id) throws Exception;

	void delete(I id) throws Exception;

	/**
	 * update all columns
	 * @param instance
	 * @throws Exception
	 */
	T update(T instance) throws Exception;

	/**
	 * update specified columns in paras
	 * @param instance
	 * @param columns
	 * @throws Exception
	 */
	T update(T instance, String... columns) throws Exception;


	T getOneBySelective(T instance) throws Exception;
	
	
	void softDelete(I id,String operator) throws Exception;


}