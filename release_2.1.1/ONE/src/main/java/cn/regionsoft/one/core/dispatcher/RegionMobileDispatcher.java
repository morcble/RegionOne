package cn.regionsoft.one.core.dispatcher;

import java.util.Map;

import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.controller.RequestNodeWrapper;

public class RegionMobileDispatcher {
    static{
        SystemContext systemContext =  SystemContext.getInstance();
       // systemContext.init(new DefaultContext(), new CustomizedContext1());
    }
	public RegionMobileDispatcher(){
	    //ServerInstance.instance.init();
            System.out.println(1);
		
	}
	//missing thread pool
	public static String doPost(String reqURI,String method,Map<String, String[]> requestMap,Map<String,String> headerInfo,Map<String,String> cookies) throws Exception{
		try {
			ProcessResponseWrapper prw = RequestProcessor.process(reqURI,method,requestMap,headerInfo,cookies);
			String responseMsg = (String) prw.getResponse();
			return responseMsg;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		/*finally{
			try{
				SystemContext.getInstance().releaseResources();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}*/
		return null;	
	}
        
        public static String simulateHttpRequest(String reqURI) throws Exception{/*
            SystemContext systemContext =  SystemContext.getInstance();
            ExampleResource b = (ExampleResource) systemContext.getManagedBean(ExampleResource.class);
    		b.handleTest();
    		ExampleController a = (ExampleController) systemContext.getManagedBean(ExampleController.class);
    		Long time11 = System.currentTimeMillis();
    		String tmpName = XframeUtil.getTargetXframeContext(ExampleEntity.class);
    		XframeContext xframeContext = SystemContext.getInstance().getXframeContext(tmpName);
    		RequestNodeWrapper rp =xframeContext.getMatchedRequestNode("/xxx/aaversion/aaversionlist/3/feng");
           // ExampleServiceImpl exampleService  = b.getExampleService();
*/            return "123";
        }
        
        public static void main(String[] args) throws Exception{
        	simulateHttpRequest("123");
        }
}
