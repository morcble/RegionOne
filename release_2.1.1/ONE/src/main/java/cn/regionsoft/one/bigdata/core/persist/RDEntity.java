package cn.regionsoft.one.bigdata.core.persist;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.data.persistence.H2OEntity;

/**
 * 存储的数据对应的实体对象
 * @author fenglj
 *
 */
public class RDEntity implements H2OEntity<String>{
	private Map<String,Object> properties = new LinkedHashMap<String,Object>();
	public void put(String propName,Object value) {
		properties.put(propName, value);
	}
	
	
	public Map<String, Object> getProperties() {
		return properties;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<Entry<String,Object>> iterator = properties.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry entry = iterator.next();
			sb.append(entry.getKey()+"="+entry.getValue()+",\r\n");
		}
		return sb.toString();
	}

	public static final String ID = "id";
	public static final String CREATE_BY = "createBy";
	public static final String CREATE_DT = "createDt";
	public static final String UPDATE_BY = "updateBy";
	public static final String UPDATE_DT = "updateDt";
	
//	private String id;
//	private String createBy;
//	private Date createDt;
//	private String updateBy;
//	private Date updateDt;
	
//	public RDEntity fold() {
//		properties.put(ID, this.id);
//		properties.put(CREATE_BY, this.createBy);
//		properties.put(CREATE_DT, this.createDt);
//		properties.put(UPDATE_BY, this.updateBy);
//		properties.put(UPDATE_DT, this.updateDt);
//		return this;
//	}
//	
//	public RDEntity unFold() {
//		this.id = (String) properties.remove(ID);
//		this.createBy = (String) properties.remove(CREATE_BY);
//		this.createDt = (Date) properties.remove(CREATE_DT);
//		this.updateBy = (String) properties.remove(UPDATE_BY);
//		this.updateDt = (Date) properties.remove(UPDATE_DT);
//		return this;
//	}
	
	public static RDEntity mapToRDEntity(Map<String,Object> map) {
		if(map==null)return null;
		RDEntity rdEntity = new RDEntity();
		rdEntity.properties = map;
		return rdEntity;
	}
	
	public static Map<String,Object> rdEntityToMap(RDEntity rdEntity) {
		if(rdEntity==null)return null;
		return rdEntity.properties;
	}
	
	public String getId() {
		return (String) properties.get(ID);
	}

	public void setId(String id) {
		properties.put(ID, id);
	}

	public String getCreateBy() {
		return (String) properties.get(CREATE_BY);
	}

	public void setCreateBy(String createBy) {
		properties.put(CREATE_BY, createBy);
	}


	public Date getCreateDt() {
		return (Date) properties.get(CREATE_DT);
	}


	public void setCreateDt(Date createDt) {
		properties.put(CREATE_DT,createDt);
	}

	public String getUpdateBy() {
		return (String) properties.get(UPDATE_BY);
	}


	public void setUpdateBy(String updateBy) {
		properties.put(UPDATE_BY,updateBy);
	}

	public Date getUpdateDt() {
		return (Date) properties.get(UPDATE_DT);
	}

	public void setUpdateDt(Date updateDt) {
		properties.put(UPDATE_DT,updateDt);
	}
	
	/**
	 * 把entity 转换 传到前端
	 */
	public void transferForFrontEnd(){
		Iterator<Entry<String,Object>> iterator = properties.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String,Object> entry = iterator.next();
			if(entry.getValue() instanceof Long) {
				properties.put(entry.getKey(), CommonUtil.longToStr((Long) entry.getValue()));
			}
		}
	}
}
