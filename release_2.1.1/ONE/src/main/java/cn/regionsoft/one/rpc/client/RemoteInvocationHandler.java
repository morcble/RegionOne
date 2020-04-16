package cn.regionsoft.one.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.RequestInfoHolder;
import cn.regionsoft.one.core.auth.dto.RequestInfoDto;
import cn.regionsoft.one.reflect.MethodMeta;
import cn.regionsoft.one.rpc.common.RpcRequest;
import cn.regionsoft.one.rpc.common.RpcResponse;

import io.netty.channel.Channel;

public class RemoteInvocationHandler implements InvocationHandler{
	private static final Logger logger = Logger.getLogger(RemoteInvocationHandler.class);
	
	private static final String SERVICE_NOT_FOUND = "microsvcManager is null";
	private static final String NO_REMOTE_SERVICE_FOUND = "No remote service found in service registration server:{0}  , path : {1}";
	public static String DELIMETER = ":";
	
	private static ConcurrentHashMap<String,Class<?>[]> parameterTypesCache = new ConcurrentHashMap<String,Class<?>[]>();
	
	private Class interfaceClass;
	private String registerPath;
	private MicrosvcManager microsvcManager;
	private RpcClientPool rpcClientPool;
	

	public RemoteInvocationHandler(Class interfaceClass, String registerPath, MicrosvcManager microsvcManager,
			RpcClientPool rpcClientPool) {
		super();
		this.interfaceClass = interfaceClass;
		this.registerPath = registerPath;
		this.microsvcManager = microsvcManager;
		this.rpcClientPool = rpcClientPool;
	}


	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try{
    		RpcRequest request = new RpcRequest();
    		
    		RequestInfoDto requestInfoDto = RequestInfoHolder.getInstance().getRequestInfo();
    		if(requestInfoDto!=null) {
    			 request.setRequestId(requestInfoDto.getRequestId());
    	         request.setLocale(requestInfoDto.getLocale());
    		}
            request.setRegisterPath(registerPath);
            request.setMethodName(method.getName());
            request.setParameterTypes(method.getParameterTypes());
            request.setParameters(args);

            String serverAddress = microsvcManager.discover(registerPath);
            
            if(serverAddress==null){
            	throw new Exception(MessageFormat.format(NO_REMOTE_SERVICE_FOUND, microsvcManager.getZkConectStr(),registerPath));
            }

            RpcClient rpcClient = rpcClientPool.getClient(serverAddress);
            Channel channel = rpcClient.send(request);
            RpcResponse rpcResponse= rpcClient.getResponse(channel);
            return rpcResponse.getResult();
            
    	}
    	catch(Throwable e){
    		logger.error(e);
    	}
    	return null;
	}
}
