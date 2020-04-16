package cn.regionsoft.one.core.contextinfo;
 
import cn.regionsoft.one.web.core.BaseDtoWithStringID;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.common.Logger;

public class ManagedBeanDetailPojo extends BaseDtoWithStringID{
	 private static final Logger logger = Logger.getLogger(ManagedBeanDetailPojo.class);
	 private List<MethodInfoPojo> methodInfoPojoLs = new ArrayList<MethodInfoPojo>();
	 
	 public void addMethodInfoPojo(MethodInfoPojo methodInfoPojo){
		 methodInfoPojoLs.add(methodInfoPojo);
	 }
	 private String systemId;
	 
	 private String contextName;
	 
	 private String name;
	 
	 private String packageName;
	 
	 private String beanType;
	 
	 private String svcType;
	 
	 public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getContextName() {
		 return contextName;
	 }

	 public void setContextName(String contextName) {
		 this.contextName = contextName;
	 }
	 
	 public String getName() {
		 return name;
	 }

	 public void setName(String name) {
		 this.name = name;
	 }
	 
	 public String getPackageName() {
		 return packageName;
	 }

	 public void setPackageName(String packageName) {
		 this.packageName = packageName;
	 }
	 
	 public String getBeanType() {
		 return beanType;
	 }

	 public void setBeanType(String beanType) {
		 this.beanType = beanType;
	 }
	 
	 public String getSvcType() {
		 return svcType;
	 }

	 public void setSvcType(String svcType) {
		 this.svcType = svcType;
	 }
	 
}
