package cn.regionsoft.one.web.core;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.auth.AuthUtil;
import cn.regionsoft.one.web.wrapper.WebReqWrapper;


public class BaseDtoWithStringID implements Serializable{
	private static final long serialVersionUID = 1L;
	private WebReqWrapper webReqWrapper;

	public WebReqWrapper getWebReqWrapper() {
		return webReqWrapper;
	}

	public void setWebReqWrapper(WebReqWrapper webReqWrapper) {
		this.webReqWrapper = webReqWrapper;
	}
	
	
	protected String id;

	protected String createBy;

	protected Date createDt;

	protected String updateBy;

	protected Date updateDt;

	protected Integer version;

	protected Integer softDelete = 0;
	
//	protected Boolean editable = true;
//	protected Boolean deletable = true;
	
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

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Integer getSoftDelete() {
		return softDelete;
	}

	public void setSoftDelete(Integer softDelete) {
		this.softDelete = softDelete;
	}
	
	 public String getUpdateDtAsStr() {
		 SimpleDateFormat sdf = CommonUtil.getSimpleDateFormat(Constants.DATE_FORMAT1);
		 if(updateDt==null) return null;
		 else return sdf.format(updateDt);
	 }

	 public void setUpdateDtAsStr(String updateDtAsStr) {
		 SimpleDateFormat sdf = CommonUtil.getSimpleDateFormat(Constants.DATE_FORMAT1);
		 try {
			 updateDt = sdf.parse(updateDtAsStr);
		 } catch (ParseException e) {
			 updateDt = null;
		 }	
	 }

	 public String getCreateDtAsStr() {
		 SimpleDateFormat sdf = CommonUtil.getSimpleDateFormat(Constants.DATE_FORMAT1);
		 if(createDt==null) return null;
		 else return sdf.format(createDt);
	 }

	 public void setCreateDtAsStr(String createDtAsStr) {
		 try {
			 SimpleDateFormat sdf = CommonUtil.getSimpleDateFormat(Constants.DATE_FORMAT1);
			 createDt = sdf.parse(createDtAsStr);
		 } catch (ParseException e) {
			 createDt = null;
		 }
	 }

	@Override
	public String toString() {
		return CommonUtil.instanceToString(this,false);
	}

//	public Boolean getEditable() {
//		return editable;
//	}
//
//	public void setEditable(Boolean editable) {
//		this.editable = editable;
//	}
//
//	public Boolean getDeletable() {
//		return deletable;
//	}
//
//	public void setDeletable(Boolean deletable) {
//		this.deletable = deletable;
//	}
	
	protected SimpleDateFormat getSimpleDateFormat() {
		return AuthUtil.getDateFormater();
	}
	
	protected SimpleDateFormat getSimpleDateFormat(String formatPattern) {
		return AuthUtil.getDateFormater(formatPattern);
	}
}
