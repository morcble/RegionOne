package cn.regionsoft.one.core;

import java.util.List;
import java.util.Set;
import cn.regionsoft.one.data.persistence.H2OEntity;

public interface H2ODaoI<T extends H2OEntity, I> {

	/*
	 * create instance in data storage
	 * @param instance
	 * @return
	 * @throws Exception 
	 */
	T insert(T instance) throws Exception;

	T find(I id) throws Exception;

	Integer delete(I id) throws Exception;

	/*
	 * update all columns
	 * @param instance
	 * @throws Exception
	 */
	T update(T instance) throws Exception;

	/*
	 * update specified columns in paras
	 * @param instance
	 * @param columns
	 * @throws Exception
	 */
	T update(T instance, String... columns) throws Exception;

	/*
	 * update specified columns in Set
	 * @param instance
	 * @param columns
	 * @throws Exception
	 */
	T update(T instance, Set<String> columns) throws Exception;

	List<T> findBySelective(T instance) throws Exception;
	
	
	Integer softDelete(I id,String operator) throws Exception;


}