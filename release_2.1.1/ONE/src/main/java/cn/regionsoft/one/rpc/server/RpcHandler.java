package cn.regionsoft.one.rpc.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
/*import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;*/

import cn.regionsoft.one.assist.method.MethodProxyWithValueReturn;
import cn.regionsoft.one.assist.method.MethodProxyAssist;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.RequestInfoHolder;
import cn.regionsoft.one.core.auth.dto.RequestInfoDto;
import cn.regionsoft.one.rpc.common.ParaUtil;
import cn.regionsoft.one.rpc.common.RpcRequest;
import cn.regionsoft.one.rpc.common.RpcResponse;

/**
 * 
 * @author fenglj
 *
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = Logger.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }
    
    
    private HashMap<String,FastClass> fastClassCache = new HashMap<String,FastClass>();
    @Override
    public void channelRead0(final ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());

        RequestInfoDto requestInfo = new RequestInfoDto();
        requestInfo.setRequestId(request.getRequestId());
        requestInfo.setLocale(request.getLocale());
        RequestInfoHolder.getInstance().setRequestInfo(requestInfo);
        try {
//        	String className = request.getClassName();
//            Object serviceBean = handlerMap.get(className);
//            String methodName = request.getMethodName();
//            String parameterTypeStr = request.getParameterTypes();
//            
//            Object[] parameters = null;
//            Class<?>[] parameterTypes = ParaUtil.paseStringToParaTypes(parameterTypeStr);
//            if(parameterTypes!=null&&parameterTypes.length>0) {
//            	parameters = new Object[parameterTypes.length];
//            	byte[] parametersBytes = request.getParameters();
//            	parameters = ParaUtil.getObjectArrayFormBytes(parametersBytes, parameterTypes);
//            }
 

            
        	 String registerPath = request.getRegisterPath();
        	 Object serviceBean = handlerMap.get(registerPath);
        	 String methodName = request.getMethodName();
        	 Class<?>[] parameterTypes = request.getParameterTypes();
        	 Object[] parameters = request.getParameters();
            
        	/* MethodProxyWithValueReturn proxy = (MethodProxyWithValueReturn) MethodProxyAssist.genMethodProxy(serviceBean,methodName,parameterTypes,true);
             Object result = proxy.excute(serviceBean, parameters);
             response.setResult(result);*/
        	 
        	 Class serviceClass = serviceBean.getClass();
        	 FastClass serviceFastClass = fastClassCache.get(serviceClass.getName());
        	 if(serviceFastClass==null) {
        		 synchronized(serviceClass) {
        			 if(serviceFastClass==null) {
        				 serviceFastClass = FastClass.create(serviceClass);
        				 fastClassCache.put(serviceClass.getName(), serviceFastClass);
        			 }
        		 }
        	 }

        	 FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        	 Object result = serviceFastMethod.invoke(serviceBean, parameters);
        	 response.setResult(result);
            
//             Class serviceClass = serviceBean.getClass();
//             Method method = serviceClass.getMethod(methodName, parameterTypes);
//             method.setAccessible(true);
//             Object result =  method.invoke(serviceBean, parameters);
//             response.setResult(result);
             
             ctx.writeAndFlush(response);
        }
        catch (Throwable t) {
        	logger.error(t);
        	response.setResult(CommonUtil.getStackTrace(t));
        }
        //.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.error(cause);
        ctx.close();
    }
}
