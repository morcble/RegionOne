package cn.regionsoft.one.core.contextinfo;
 
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.web.core.BaseDtoWithStringID;

public class MethodInfoPojo extends BaseDtoWithStringID{
	private static final Logger logger = Logger.getLogger(MethodInfoPojo.class);
	 private Long detailId;
	 
	 private String name;
	 
	 private String url;
	 
	 private String inputType;
	 
	 private String returnType;
	 
	 public Long getDetailId() {
		 return detailId;
	 }

	 public void setDetailId(Long detailId) {
		 this.detailId = detailId;
	 }
	 
	 public String getName() {
		 return name;
	 }

	 public void setName(String name) {
		 this.name = name;
	 }
	 
	 public String getUrl() {
		 return url;
	 }

	 public void setUrl(String url) {
		 this.url = url;
	 }
	 
	 public String getInputType() {
		 return inputType;
	 }

	 public void setInputType(String inputType) {
		 this.inputType = inputType;
	 }
	 
	 public String getReturnType() {
		 return returnType;
	 }

	 public void setReturnType(String returnType) {
		 this.returnType = returnType;
	 }
	 
}
