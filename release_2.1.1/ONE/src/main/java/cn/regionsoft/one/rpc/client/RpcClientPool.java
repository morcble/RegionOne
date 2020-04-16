package cn.regionsoft.one.rpc.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import cn.regionsoft.one.common.Logger;

public class RpcClientPool {
	private static final Logger logger = Logger.getLogger(RpcClientPool.class);
	private Map<String,RpcClient> clientMap = new ConcurrentHashMap<String,RpcClient>();
	
	private Integer poolSizePerServer;
	private Semaphore semaphore = new Semaphore(1);
	
	public static String DELIMETER = ":";
	

	/**
	 * 
	 * @param poolSizePerServer
	 */
	public RpcClientPool(Integer poolSizePerServer){
		if(poolSizePerServer==null) poolSizePerServer = 1;
		this.poolSizePerServer = poolSizePerServer;
	}
	


	public RpcClient getClient(String serverAddress){
		try{
			RpcClient client = clientMap.get(serverAddress);
			
			if(client==null){
				try{
					semaphore.acquire();
					client = clientMap.get(serverAddress);
					
					if(client==null){
						String[] array = serverAddress.split(DELIMETER);
	                    String host = array[0];
	                    int port = Integer.parseInt(array[1]);
						
	                    client = new RpcClient(host,port);
						clientMap.put(serverAddress, client);
					}
				}
				finally{
					semaphore.release();
				}
				
			}
			return client;
		}
		catch(Exception e){
			logger.error(e);
			return null;
		}
	}

	public Integer getPoolSizePerServer() {
		return poolSizePerServer;
	}

	
}
