package cn.regionsoft.one.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;

import cn.regionsoft.one.common.Logger;

public class ZooKeeperManager extends AbstractZooKeeperManager{
	private static final Logger logger = Logger.getLogger(ZooKeeperManager.class);
	
	public static final String DEFAULT_CHARSET = "UTF-8";
	private final CuratorFramework curatorFramework;
	private CuratorFrameworkWrapper curatorFrameworkWrapper;
	public ZooKeeperManager(String connectStr){
		curatorFramework = CuratorFrameworkFactory.builder()
				.connectString(connectStr)
				.retryPolicy(new RetryNTimes(3, 1000))
				.sessionTimeoutMs(5*1000)
				.connectionTimeoutMs(5*1000).build();
		
		curatorFrameworkWrapper = new CuratorFrameworkWrapper(curatorFramework);
		
		this.setCuratorClient(curatorFramework);
		curatorFramework.getConnectionStateListenable().addListener(
						new ConnectionStateListener() {
							@Override
							public void stateChanged(final CuratorFramework client,final ConnectionState state) {
										ZooKeeperManager.this.setConnectionState(state);
										switch (state) {
											case LOST:
												triggerState(StateListener.State.DISCONNECTED);
												break;
											case CONNECTED:
												triggerState(StateListener.State.CONNECTED);
												break;
											case RECONNECTED:
												triggerState(StateListener.State.RECONNECTED);
												break;
											default:
												break;
										}
									}
						});
		try {
			curatorFramework.start();
			curatorFramework.blockUntilConnected();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	
	public CuratorFrameworkWrapper getCuratorFrameworkWrapper() {
		return curatorFrameworkWrapper;
	}



	public void regService(String registryKey,final String serviceEndPoint) {
		try{
			final String observedPath =  registryKey;
			if(this.getConnectionState() == ConnectionState.CONNECTED){
				curatorFrameworkWrapper.createPathIfNotExsits(observedPath, false);
				curatorFrameworkWrapper.createEphemeralSEQUENTIAL(observedPath+"/",serviceEndPoint);
			}
			
			this.addStateListener(observedPath,new StateListener(){
				@Override
				public void stateChanged(State state) {
					switch (state) {
						case CONNECTED:
						case RECONNECTED:
							curatorFrameworkWrapper.createPathIfNotExsits(observedPath, false);
							curatorFrameworkWrapper.createEphemeralSEQUENTIAL(observedPath+"/",serviceEndPoint);
							break;
						default:
							break;
					
					}
				}
				
			});
		}
		catch(Exception e){
			logger.error(e);
		}
		
	}
	
	
 
	
}
