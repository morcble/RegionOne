package cn.regionsoft.one.core.auth;
//package cn.regionsoft.one.admin.security;
//
//import java.util.Map;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import cn.regionsoft.one.core.auth.SecurityFilter;
//import cn.regionsoft.one.core.auth.SecurityFilterResult;
//import cn.regionsoft.one.enums.UserToDoAction;
//
//public class SecurityFilterImpl implements SecurityFilter{
//	@Override
//	public SecurityFilterResult checkAccess(String requestURI, String method, Map<String, String[]> requestMap,Map<String,String> headerInfo,HttpServletRequest request, HttpServletResponse response){
//		String account = null;
//		String token = null;
//		return new SecurityFilterResult(UserToDoAction.VALID_ACCESS,account,token);
//	}
//}
