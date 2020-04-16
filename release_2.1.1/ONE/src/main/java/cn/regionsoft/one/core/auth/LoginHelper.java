package cn.regionsoft.one.core.auth;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.properties.ConfigUtil;
import cn.regionsoft.one.rpc.common.ServerConstant;

public class LoginHelper {
	private static final Logger logger = Logger.getLogger(LoginHelper.class);
	
	public static final String CLAIM_SESSION_ID = "sessionId";
	public static Algorithm ALGORITHM = null ;
	public static Long RENEW_GAP  = null;//millisecond
	
	static{
		try {
			String renewGapStr = ConfigUtil.getProperty(ServerConstant.TOKEN_RENEW_GAP);
			if (CommonUtil.isEmpty(renewGapStr)) {
				renewGapStr = "15";
			}
			RENEW_GAP = Long.valueOf(renewGapStr) * 60000;
		} catch (Exception e) {
			logger.error(e);
		}
		try {
			String key = ConfigUtil.getProperty(ServerConstant.JWT_SIGNATURE);
			if (CommonUtil.isEmpty(key)) {
				key = "OPHgw^H_+gj=";
			}
			ALGORITHM = Algorithm.HMAC256(key);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public static void deleteToken(HttpServletResponse response){
		Cookie tokenCookie = new Cookie(ServerConstant.REGION_TOKEN,null);
		tokenCookie.setMaxAge(0);
		tokenCookie.setPath("/");
		response.addCookie(tokenCookie);
	}
	
	public static void deleteLoginFlag(HttpServletResponse response){
		Cookie loginFlag = new Cookie(ServerConstant.REGION_LOGIN,null);
		loginFlag.setMaxAge(0);
		loginFlag.setPath("/");
		response.addCookie(loginFlag);
	}

	public static void addToken(String tokenStr,HttpServletResponse response){
		Cookie tokenCookie = new Cookie(ServerConstant.REGION_TOKEN,tokenStr);
		tokenCookie.setPath("/");
		tokenCookie.setMaxAge(36000);
		tokenCookie.setHttpOnly(true);
		//tokenCookie.setSecure(true);
		response.addCookie(tokenCookie);
	}
	
	public static void addLoginFlag(String userInfo,HttpServletResponse response){
		Cookie loginFlag = new Cookie(ServerConstant.REGION_LOGIN,userInfo);
		loginFlag.setMaxAge(172800);
		loginFlag.setPath("/");
		loginFlag.setHttpOnly(true);
		response.addCookie(loginFlag);
	}

	
	public static String generateToken(Long renewGap,Map<String,String> claims ,Algorithm algorithm){
		Date now = new Date();
		Date expireDt = new Date();
		expireDt.setTime(now.getTime() + renewGap);
		
		Builder builder = JWT.create().withIssuer("regionsoft").withIssuedAt(now).withExpiresAt(expireDt);
		for(Entry<String,String> entry:claims.entrySet()){
			builder.withClaim(entry.getKey(), entry.getValue());
		}
		String token = builder.sign(algorithm);
		return token;
	}

	public static String generateToken(Map<String,String> claims) {
		String token = generateToken(LoginHelper.RENEW_GAP, claims, LoginHelper.ALGORITHM);
		return token;
	}
	
	public static DecodedJWT resolveToken(String token) {
		JWTVerifier verifier1 = JWT.require(LoginHelper.ALGORITHM)
				  .acceptExpiresAt(ServerConstant.SESSION_EXPIRE_GAP/1000L)
				  .build();
		
		try{
			DecodedJWT decodedJWT = verifier1.verify(token);
			return decodedJWT;
		}
		catch(Exception e){
			logger.warn(e);
			return null;
		}
	}
}
