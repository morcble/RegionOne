package cn.regionsoft.one.thridparty.wechat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.regionsoft.one.common.HttpUtil;
import cn.regionsoft.one.common.JsonUtil;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;

public class WechatUtil {
	private static final Logger logger = Logger.getLogger(WechatUtil.class);
	//登录的access token
	private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=${appId}&secret=${secret}&code=${code}&grant_type=authorization_code";
	private static final String APP_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=${accessToken}&openid=${openId}";
	
	//获取jsapi的access token
	private static final String JSAPI_ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${appId}&secret=${secret}";
	private static final String JSAPI_TICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=${accessToken}&type=jsapi";
	
	
	public static AccessTokenInfo getAccessToken(String appId,String secret,String code) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("appId", appId);
		params.put("secret", secret);
		params.put("code", code);
		String actualUrl = CommonUtil.wrapText(ACCESS_TOKEN_URL, params);
		
		String tokenJson = HttpUtil.getWithTimeOut(actualUrl, null,5000);
		
		AccessTokenInfo accessTokenInfo = JsonUtil.jsonToBean(tokenJson, AccessTokenInfo.class);
		if(accessTokenInfo.getErrcode()!=null)throw new Exception("request wechat interface error:"+tokenJson);
		return accessTokenInfo;
	}
	
	//{"openid":"oe1Evwc7lA9rVo061RyA-rkI-vO8","nickname":"morcble","sex":1,"language":"zh_CN","city":"Chengdu","province":"Sichuan","country":"CN","headimgurl":"http:\/\/thirdwx.qlogo.cn\/mmopen\/vi_32\/NibGOaMbeXVrcLvkygNWtvQYuibibVf32ges4pmPmAicho6BaZPOlO8kV0LW164f5hjMLIz6qoHTXO1y5HBiasquovg\/132","privilege":[]}	
	private static WechatUserInfo getUserInfo(String accessToken,String openId) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("accessToken", accessToken);
		params.put("openId", openId);
		String actualUrl = CommonUtil.wrapText(APP_USER_INFO_URL, params);
		
		String userInfoJson = HttpUtil.getWithTimeOut(actualUrl, null,5000);
		
		WechatUserInfo userInfo = JsonUtil.jsonToBean(userInfoJson, WechatUserInfo.class);
		if(userInfo.getErrcode()!=null)throw new Exception("request wechat interface error:"+userInfo);
		return userInfo;
	}
	
	public static WechatUserInfo getUserInfo(String appId,String secret,String code)throws Exception {
		AccessTokenInfo accessTokenInfo = getAccessToken(appId, secret, code);
		return getUserInfo(accessTokenInfo.getAccess_token(),accessTokenInfo.getOpenid());
	}
	
	public static String getAPIAccessToken(String appId,String secret) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("appId", appId);
		params.put("secret", secret);
		String actualUrl = CommonUtil.wrapText(JSAPI_ACCESS_TOKEN, params);
		String apiJson = HttpUtil.getWithTimeOut(actualUrl, null,5000);
		Map<String,String> resultMap = JsonUtil.jsonToMap(apiJson);
		String accessToken = resultMap.get("access_token");
		return accessToken;
	}
	
	public static String getJsApiTicket(String appId,String secret) throws Exception {
		String accessToken = getAPIAccessToken(appId, secret);
		Map<String, Object> params = new HashMap<String, Object>();
		logger.debug("accessToken="+accessToken);
		params.put("accessToken", accessToken);
		String actualUrl = CommonUtil.wrapText(JSAPI_TICKET, params);
		logger.debug(actualUrl);
		String jsApiTicketJson = HttpUtil.getWithTimeOut(actualUrl, null,5000);
		logger.debug("jsApiTicketJson="+jsApiTicketJson);
		Map<String,String> ticketsInfoMap = JsonUtil.jsonToMap(jsApiTicketJson);
		return ticketsInfoMap.get("ticket");
	}
	
	public static Map<String,String> getApiSingnature(String jsapiTicket,String url) throws Exception {
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
		String nonceStr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
		String str = "jsapi_ticket="+jsapiTicket+"&noncestr="+nonceStr+"&timestamp="+timestamp+"&url="+url;  
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] byteArray = str.getBytes("UTF-8");
        sha.update(byteArray);  
        byte messageDigest[] = sha.digest();  
        // Create Hex String  
        StringBuffer hexString = new StringBuffer();  
        // 字节数组转换为 十六进制 数  
        for (int i = 0; i < messageDigest.length; i++) {  
            String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);  
            if (shaHex.length() < 2) {  
                hexString.append(0);  
            }  
            hexString.append(shaHex);  
        }  
        Map<String,String> result = new HashMap<String,String>();
        result.put("timestamp", timestamp);
        result.put("nonceStr", nonceStr);
        result.put("signature", hexString.toString());
        return result;  
	}
}