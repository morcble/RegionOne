package cn.regionsoft.one.web.wrapper;

import java.io.Serializable;
import java.util.List;

import cn.regionsoft.one.common.Constants;

public class PaginationTO implements Serializable{
	public static final int NOT_DELETED = 0;
	public static final int DELETED = 1;
	
	private Integer currentPageNo = 1;
	private int totalPageAmount = 0;
	private int totalAmount = 0;
	private Integer pageSize = Constants.PAGE_SIZE;
	private String orderBy = null;
	private String deleteIds = null;
	private String deleteId = null;
	private Object extData = null;
	private List<?> list;
	
	private String loginAccount = null;
	
	private String firstRecordId = null;//记录当前分页第一条记录的ID
	private String lastRecordId = null;//记录当前分页最后一条记录的ID
	
	//是否是初始化加载
	private Boolean initLoad = false;
	private Boolean minusPage = false;//true 向前翻页 , false向后翻页,默认为向后翻页


	public Boolean getMinusPage() {
		return minusPage;
	}
	public void setMinusPage(Boolean minusPage) {
		this.minusPage = minusPage;
	}
	public Boolean getInitLoad() {
		return initLoad;
	}
	public void setInitLoad(Boolean initLoad) {
		this.initLoad = initLoad;
	}
	public String getLoginAccount() {
		return loginAccount;
	}
	public void setLoginAccount(String loginAccount) {
		this.loginAccount = loginAccount;
	}
	public Object getExtData() {
		return extData;
	}
	public void setExtData(Object extData) {
		this.extData = extData;
	}
	//for cloud db only
	private String nextStartRowKey = null;
	
	private Object attachedObj = null;

	public Object getAttachedObj() {
		return attachedObj;
	}
	public void setAttachedObj(Object attachedObj) {
		this.attachedObj = attachedObj;
	}
	public Integer getCurrentPageNo() {
		return currentPageNo;
	}
	public void setCurrentPageNo(Integer currentPageNo) {
		this.currentPageNo = currentPageNo;
	}
	public int getTotalPageAmount() {
		return totalPageAmount;
	}
	public void setTotalPageAmount(int totalPageAmount) {
		this.totalPageAmount = totalPageAmount;
	}
	public int getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(int totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	
	public List<?> getList() {
		return list;
	}
	public void setList(List<?> list) {
		this.list = list;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public String getDeleteIds() {
		return deleteIds;
	}
	public void setDeleteIds(String deleteIds) {
		this.deleteIds = deleteIds;
	}
	public String getDeleteId() {
		return deleteId;
	}
	public void setDeleteId(String deleteId) {
		this.deleteId = deleteId;
	}
	public String getNextStartRowKey() {
		return nextStartRowKey;
	}
	public void setNextStartRowKey(String nextStartRowKey) {
		this.nextStartRowKey = nextStartRowKey;
	}
	public String getLastRecordId() {
		return lastRecordId;
	}
	public void setLastRecordId(String lastRecordId) {
		this.lastRecordId = lastRecordId;
	}
	public String getFirstRecordId() {
		return firstRecordId;
	}
	public void setFirstRecordId(String firstRecordId) {
		this.firstRecordId = firstRecordId;
	}
}
