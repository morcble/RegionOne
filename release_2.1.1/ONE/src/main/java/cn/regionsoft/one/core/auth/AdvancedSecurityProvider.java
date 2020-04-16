package cn.regionsoft.one.core.auth;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.enums.UserToDoAction;

public interface AdvancedSecurityProvider {
	public abstract UserToDoAction checkAccess(String account, String reqURI,Map<String,String> headerInfo,Map<String,String> cookies, HttpServletRequest request, HttpServletResponse response);


	public abstract Set<String> getAccessableGroupsByAccount(String account);


	void updateGroupResourceByGroupId(String groupId);


	void updateGroupUserByMapIds(List<Long> mapIds);


	void updateGroup(String[] groupId, String flag);
	
	
	public static boolean isAccessable(Set<String> alowSet,String reqURI) {
		boolean accessable = alowSet.contains(reqURI);
		if(!accessable) {
			String[] splits = null;
			String[] splits1 = null;
			for(String url:alowSet) {
				if(url.indexOf(Constants.DOLLAR)==-1)continue;
		
				splits = url.split(Constants.SLASH);
				splits1 = reqURI.split(Constants.SLASH);
				if(splits.length==splits1.length) {//判断模糊匹配
					for(int i = 0 ; i<splits.length;i++) {
						if(!splits[i].startsWith(Constants.DOLLAR)||!splits[i].endsWith(Constants.OCCUP_END)||!splits[i].startsWith(Constants.OCCUP_START)) {
							if(!splits[i].equals(splits1[i])) {
								break;
							}
						}
						
						if(i==splits.length-1)accessable=true;
					}
				}
			}
		}
		return accessable;
	}

}
