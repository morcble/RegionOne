package cn.regionsoft.one.core.auth;

import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.regionsoft.one.core.auth.dto.BasicSecurityResultDto;
import cn.regionsoft.one.core.auth.dto.LoginDto;

public interface BasicSecurityProvider {
	Set<String> getPublicRes() ;

	LoginDto login(String account, String password, HttpServletRequest request, HttpServletResponse response);

	LogoutResponseType logout(String loginToken, HttpServletRequest request, HttpServletResponse response);

	BasicSecurityResultDto validateAccess(String loginToken, String reqURI, HttpServletRequest request,
			HttpServletResponse response,Map<String,String> cookies);

}
