package cn.regionsoft.one.thridparty.wechat;

import java.io.Serializable;

import cn.regionsoft.one.core.CommonUtil;

public class AccessTokenInfo implements Serializable{
	private static final long serialVersionUID = -514329413513889301L;
	private String access_token = null;
	private Integer expires_in;
	private String openid = null;
	private String refresh_token = null;
	private String errcode = null;
	
	private Long createTime = null;
	
	public AccessTokenInfo() {
		createTime = System.currentTimeMillis();
	}
	
	
	/**
	 * 获取过期时间
	 * @return
	 */
	public Long getExpiredTime() {
		return createTime+expires_in*1000;
	}


	public String getAccess_token() {
		return access_token;
	}


	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}


	public Integer getExpires_in() {
		return expires_in;
	}


	public void setExpires_in(Integer expires_in) {
		this.expires_in = expires_in;
	}


	public String getOpenid() {
		return openid;
	}


	public void setOpenid(String openid) {
		this.openid = openid;
	}


	public String getRefresh_token() {
		return refresh_token;
	}


	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}


	public Long getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}


	public String getErrcode() {
		return errcode;
	}


	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}


	@Override
	public String toString() {
		return CommonUtil.instanceToString(this,false);
	}
}
