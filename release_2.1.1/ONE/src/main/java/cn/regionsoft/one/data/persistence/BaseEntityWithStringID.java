package cn.regionsoft.one.data.persistence;

import java.util.Date;

import cn.regionsoft.one.core.CommonUtil;

public class BaseEntityWithStringID implements H2OEntity<String>{
	/**
	 * Common Begin
	 */
	@Id(length = 32)
	protected String id; 

	@Column(name = "createBy", length = 32)
	protected String createBy;

	@Column(name = "createDt")
	protected Date createDt;

	@Column(name = "updateBy", length = 32)
	protected String updateBy;

	@Column(name = "updateDt")
	protected Date updateDt;

	@Version
	protected Integer version;

	@Column(name = "softDelete", length = 5)
	private Integer softDelete = 0;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getCreateDt() {
		return createDt;
	}

	public void setCreateDt(Date createDt) {
		this.createDt = createDt;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public Date getUpdateDt() {
		return updateDt;
	}

	public void setUpdateDt(Date updateDt) {
		this.updateDt = updateDt;
	}

	public Integer getSoftDelete() {
		return softDelete;
	}

	public void setSoftDelete(Integer softDelete) {
		this.softDelete = softDelete;
	}
	
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return CommonUtil.instanceToString(this,false);
	}

	/**
	 * Common End
	 */
}
