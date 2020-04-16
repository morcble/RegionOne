package cn.regionsoft.one.core.contextinfo;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.one.core.contextinfo.dto.H2OContextDetail;
import cn.regionsoft.one.core.contextinfo.dto.SystemContextDetail;

public class SystemContextDetailDetector {
	public static SystemContextDetail refreshSystemContextDetail(boolean persist) throws SQLException{
		Map<String, H2OContext> contextsMap = SystemContext.getInstance().getContextsMap();
		SystemContextDetail systemContextDetail = new SystemContextDetail();
		
		for(Entry<String, H2OContext> entry: contextsMap.entrySet()){
			H2OContextDetail h2oContextDetail = new H2OContextDetail(entry.getValue(),persist,entry.getKey());
			systemContextDetail.addH2OContextDetail(h2oContextDetail);
		}
		
		return systemContextDetail;
	}
}
