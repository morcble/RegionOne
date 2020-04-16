package cn.regionsoft.one.rpc.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.properties.ConfigUtil;
import cn.regionsoft.one.rpc.common.ServerConstant;
import cn.regionsoft.one.rpc.server.RPCServer;
//import cn.regionsoft.one.rpc.server.RPCThread;
import cn.regionsoft.one.zookeeper.ZooKeeperManager;
import io.netty.util.internal.ThreadLocalRandom;

/**
 * 微服务管理中心
  用于发现服务节点,获取服务节点,监听服务变动
 * @author fenglj
 */
public class MicrosvcManager {
	public static Logger logger = Logger.getLogger(MicrosvcManager.class);

    private CountDownLatch latch = new CountDownLatch(1);
    
    private ConcurrentHashMap<String,List<String>> registedServicePaths = new ConcurrentHashMap<String,List<String>>();//regestkey  -- datalists

    private String zkConectStr;
    
    private RpcProxy rpcProxy = null;
    
	private Map<String, Object> rpcHandlerMap = new HashMap<String, Object>();
	
	private ZooKeeperManager zooKeeperManager;
	
	private int consumerCount = 0;
	private int providerCount = 0;

    public RpcProxy getRpcProxy() {
		return rpcProxy;
	}

	public void setRpcProxy(RpcProxy rpcProxy) {
		this.rpcProxy = rpcProxy;
	}

	public MicrosvcManager(String zkConectStr) {
        this.zkConectStr = zkConectStr;
    }
    
	public void plusConsumer() {
		consumerCount++;
	}
	
	public void plusProvider() {
		providerCount++;
	}
	
    public int getConsumerCount() {
		return consumerCount;
	}

	public int getProviderCount() {
		return providerCount;
	}

	public String discover(String servicePath) {
    	try{
    		//判断节点服务是否正在变动
    		return getServiceNode(servicePath);
    	}
    	catch(Exception e){
    		throw new RuntimeException(e);
    	}       
    }
    
    private String getServiceNode(String servicePath) {
    	String data = null;
		List<String> dataList = registedServicePaths.get(servicePath);
		int size = dataList.size();
		if (size > 0) {
			if (size == 1) {
				data = dataList.get(0);
				//logger.debug("using only data:", data);
			} else {
				data = dataList.get(ThreadLocalRandom.current().nextInt(size));
				//logger.debug("using random data:", data);
			}
		}
		return data;
    }

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(zkConectStr, ServerConstant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        }  catch (IOException e) {
        	logger.error(e);
        }
        catch (InterruptedException e) {
        	logger.error(e);
        }
        return zk;
    }
    
    public void startWatchServices() {
		ZooKeeper zk = connectServer();
        if (zk != null) {
        	watchNodes(zk);
        }
	}
    
    public Map<String, Object> getRpcHandlerMap() {
		return rpcHandlerMap;
	}

	private void watchNodes(final ZooKeeper zk){
    	Enumeration<String> bindServices = registedServicePaths.keys();
    	while(bindServices.hasMoreElements()){
    		watchNode(zk,bindServices.nextElement());
    	}
    }
    
    private void watchNode(final ZooKeeper zk,final String servicePath) {
        try {
            List<String> nodeList = zk.getChildren(servicePath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                    	try{
                    		synchronized (servicePath.intern()) {
                    			watchNode(zk,servicePath);
                    		}
                    		//zk.removeAllWatches(path, WatcherType.Any, true);
                    	}
                    	catch(Exception e){
                    		throw new RuntimeException(e);
                    	}
                    }
                }
            });
            List<String> dataList = new ArrayList<String>();
            for (String node : nodeList) {
                byte[] bytes = zk.getData(servicePath + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            logger.debug("refresh node data:", dataList);
            registedServicePaths.put(servicePath, dataList);
        }  catch (KeeperException e) {
            logger.error("", e);
        }
        catch (InterruptedException e) {
        	logger.error("", e);
        }
    }
    
    public void registerConsumer(String path){
    	if(registedServicePaths.containsKey(path))return;
    	else registedServicePaths.put(path, new ArrayList<String>());
    }

	public String getZkConectStr() {
		return zkConectStr;
	}
	
	public ZooKeeperManager getZooKeeperManager() {
		return zooKeeperManager;
	}

	public void startMicroSvcServer() {
		String rsMicrosvcEndpoint = ConfigUtil.getProperty(ServerConstant.RS_MICROSVC_ENDPOINT);
		String zkConectStr = ConfigUtil.getProperty(ServerConstant.RS_MICROSVC_ZOOKEEPERS);
		zooKeeperManager = new ZooKeeperManager(zkConectStr);
		//zooKeeperManager.regService(ServerConstant.RS_MICROSVC_REG_CONTEXT,rsMicrosvcEndpoint);
		RPCServer.getInstance().startSvc(rsMicrosvcEndpoint, this);
		
		
		//RPCThread rpcThread = new RPCThread(rsMicrosvcEndpoint,this);
    	//rpcThread.start();
	}
	
}