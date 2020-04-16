package cn.regionsoft.one.zookeeper;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.utils.CloseableUtils;

import cn.regionsoft.one.common.Logger;
import com.google.common.collect.Maps;

public abstract class AbstractZooKeeperManager{
	private static final Logger logger = Logger.getLogger(AbstractZooKeeperManager.class);

	private final ConcurrentHashMap<String,StateListener> stateListenerPool;
	
	private final Lock nodeLock = new ReentrantLock();

	private final Lock pathChildrenLock = new ReentrantLock();

	private final Map<String, NodeCache> nodeCachePool;

	private final Map<String, PathChildrenCache> pathChildrenCachePool;
	/** indicate this resource closed or not */
	private final AtomicBoolean closed = new AtomicBoolean(false);
	
	private ConnectionState connectionState = ConnectionState.LOST;

	public AbstractZooKeeperManager() {
		super();
		stateListenerPool = new ConcurrentHashMap<String,StateListener>();
		nodeCachePool = Maps.newHashMap();
		pathChildrenCachePool = Maps.newHashMap();
		/**
		 * close hook 
		 */
		addShutdownHook();
	}

	private CuratorFramework curatorClient;

	public void setCuratorClient(CuratorFramework curatorClient) {
		this.curatorClient = curatorClient;
	}
	
	public void addStateListener(String path ,final StateListener listener) {
		stateListenerPool.put(path,listener);
	}

	
	public void removeStateListener(final String path) {
		stateListenerPool.remove(path);
	}

	
	public ConcurrentHashMap<String,StateListener> getStateListeners() {
		return stateListenerPool;
	}

	
	public void addNodeListener(final String path, final NodeCacheListener nodeCacheListener) {
		final Lock currentLock = nodeLock;
		currentLock.lock();
		try {
			if (null == (nodeCachePool.get(path))) {
				final NodeCache nodeCache;
				(nodeCache = new NodeCache(curatorClient, path, false)).getListenable().addListener(nodeCacheListener);
				nodeCache.start();
				nodeCachePool.put(path, nodeCache);
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			currentLock.unlock();
		}
	}

	
	public void removeNodeListener(final String path) {
		final Lock currentLock = nodeLock;
		currentLock.lock();
		try {
			final NodeCache nodeCache;
			if (null != (nodeCache = nodeCachePool.remove(path))) {
				nodeCache.close();
			}
		} catch (IOException e) {
			logger.error(e);
		} finally {
			currentLock.unlock();
		}
	}

	
	public void addPathChildrenListener(final String path, final PathChildrenCacheListener pathChildrenCacheListener) {

		final Lock currentLock = pathChildrenLock;
		currentLock.lock();
		try {
			if (null == (pathChildrenCachePool.get(path))) {
				final PathChildrenCache pathChildrenCache;
				(pathChildrenCache = new PathChildrenCache(curatorClient, path, true)).getListenable().addListener(pathChildrenCacheListener);

				pathChildrenCache.start();
				pathChildrenCachePool.put(path, pathChildrenCache);
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			currentLock.unlock();
		}
	}

	
	public void removePathChildrenListener(final String path) {
		final Lock currentLock = pathChildrenLock;
		currentLock.lock();
		try {
			final PathChildrenCache pathChildrenCache;
			if (null != (pathChildrenCache = pathChildrenCachePool.remove(path))) {
				pathChildrenCache.close(); 
			}
		} catch (IOException e) {
			logger.error(e);
		} finally {
			currentLock.unlock();
		}
	}

	protected void triggerState(final StateListener.State state) {
		for (StateListener stateListener : getStateListeners().values()) {
			stateListener.stateChanged(state);
		}
	}

	private void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				close();
			}
		});
	}

	private void close() {
		if (closed.get()) {
			return;
		}
		if (closed.compareAndSet(false, true)) {
			try {
				doClose();
			} catch (Throwable t) {
				logger.error(t);
			}
		}
	}

	
	private void doClose() {
		Iterator<NodeCache> itrator = nodeCachePool.values().iterator();
		while(itrator.hasNext()){
			CloseableUtils.closeQuietly(itrator.next());
		}
		
		Iterator<PathChildrenCache> itrator2 = pathChildrenCachePool.values().iterator();
		while(itrator2.hasNext()){
			CloseableUtils.closeQuietly(itrator2.next());
		}
		
		CloseableUtils.closeQuietly(curatorClient);
	}

	public boolean isConnected() {
		return curatorClient.getZookeeperClient().isConnected();
	}

	public ConnectionState getConnectionState() {
		return connectionState;
	}

	public void setConnectionState(ConnectionState connectionState) {
		this.connectionState = connectionState;
	}
}
