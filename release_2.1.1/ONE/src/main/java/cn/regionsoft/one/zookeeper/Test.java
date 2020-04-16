package cn.regionsoft.one.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		String connectStr = "127.0.0.1:2181";//192.168.19.130:2181,192.168.19.130:2182,192.168.19.130:2183
		ZooKeeperManager zooKeeperManager = new ZooKeeperManager(connectStr);
		Thread.sleep(1000);
		String observedPath = "/xframe/morcble/services";
		zooKeeperManager.getCuratorFrameworkWrapper().createPathIfNotExsits(observedPath, false);
		zooKeeperManager.getCuratorFrameworkWrapper().createEphemeralSEQUENTIAL(observedPath+"/",connectStr);
		zooKeeperManager.getCuratorFrameworkWrapper().createEphemeralSEQUENTIAL(observedPath+"/",connectStr);
		zooKeeperManager.getCuratorFrameworkWrapper().createEphemeralSEQUENTIAL(observedPath+"/",connectStr);
		
		zooKeeperManager.addPathChildrenListener(observedPath, new PathChildrenCacheListener(){
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				System.out.println("PathChildrenCacheListener"+event.toString());
				
			}
			
		});
		
		
		Thread.sleep(100000000);
	}

}
