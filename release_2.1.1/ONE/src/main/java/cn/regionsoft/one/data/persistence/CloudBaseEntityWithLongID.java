package cn.regionsoft.one.data.persistence;

import java.util.Date;

import cn.regionsoft.one.core.CommonUtil;

public class CloudBaseEntityWithLongID implements H2OEntity<Long>{
	/**
	 * Common Begin
	 */
	@Id
	protected Long id; 

	@Column(name = "createBy", length = 32)
	protected String createBy;

	@Column(name = "createDt")
	protected Date createDt;

	@Column(name = "updateBy", length = 32)
	protected String updateBy;

	@Column(name = "updateDt")
	protected Date updateDt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

	@Override
	public String toString() {
		return CommonUtil.instanceToString(this,false);
	}

	/**
	 * Common End
	 */
}
