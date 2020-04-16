package cn.regionsoft.one.data.persistence;

import java.util.Date;

import cn.regionsoft.one.core.CommonUtil;

public class BasicBaseEntityWithLongID implements H2OEntity<Long>{
	/**
	 * Common Begin
	 */
	@Id
	protected Long id; 

	@Column(name = "createDt")
	protected Date createDt;

	@Column(name = "softDelete", length = 5)
	private Integer softDelete = 0;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	public Date getCreateDt() {
		return createDt;
	}

	public void setCreateDt(Date createDt) {
		this.createDt = createDt;
	}

	public Integer getSoftDelete() {
		return softDelete;
	}

	public void setSoftDelete(Integer softDelete) {
		this.softDelete = softDelete;
	}

	@Override
	public String toString() {
		return CommonUtil.instanceToString(this,false);
	}

	/**
	 * Common End
	 */
}
