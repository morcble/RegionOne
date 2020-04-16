package cn.regionsoft.one.zookeeper;

import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import com.google.common.base.Charsets;

public class CuratorFrameworkWrapper {
	private CuratorFramework curatorFramework;
	public CuratorFrameworkWrapper(CuratorFramework curatorFramework){
		this.curatorFramework = curatorFramework;
	}
	
	public void createPathIfNotExsits(final String path, final boolean ephemeral) {
		int i;
		/*if ((i = org.apache.commons.lang3.StringUtils.lastIndexOf(path, '/')) > 0) {
			createPathIfNotExsits(org.apache.commons.lang3.StringUtils.substring(path, 0, i), ephemeral);
		}*/
		
		if ((i = path.lastIndexOf('/')) > 0) {
			createPathIfNotExsits(path.substring(0, i), ephemeral);
		}
		
		if (checkExist(path)) {
			return;
		}
		if (ephemeral) {
			createEphemeral(path);
		} else {
			createPersistent(path);
		}
	}

	
	public void createrOrUpdate(final String path, final String content) {
		try {
			curatorFramework.setData().forPath(path, content.getBytes(Charsets.UTF_8));
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	
	public void createrOrUpdateInTransaction(final String path, final String content) {
		try {
			curatorFramework.inTransaction().setData().forPath(path, content.getBytes(Charsets.UTF_8));
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public void createPersistent(final String path) {
		try {
			curatorFramework.create().forPath(path);
		} catch (NodeExistsException e) {
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	
	public void createEphemeral(final String path) {
		try { 
			curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(path);
		} catch (NodeExistsException e) {
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public void createEphemeralSEQUENTIAL(final String path,String data) {
		try { 
			byte[] bytes = data.getBytes();
			curatorFramework.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path,bytes);
		} catch (NodeExistsException e) {
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	
	public void delete(final String path, final boolean deletingChildrenIfNeeded) {
		try {
			(deletingChildrenIfNeeded ? curatorFramework.delete().guaranteed().deletingChildrenIfNeeded()
					: curatorFramework.delete().guaranteed()).forPath(path);
		} catch (NoNodeException e) {
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	
	public List<String> getListChildren(final String path) {
		try {
			return curatorFramework.getChildren().forPath(path);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	
	public void getListChildrenAndValues(String path) throws Exception {
		List<String> paths = curatorFramework.getChildren().forPath(path);
		for (String p : paths) {
			String data = new String(curatorFramework.getData().forPath(path + "/" + p), Charsets.UTF_8);
			System.out.println(p + "=" + data);
		}

	}

	
	public boolean checkExist(final String path) {
		try {
			return null != curatorFramework.checkExists().forPath(path);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	
	public String read(final String path) throws Exception {
		return new String(curatorFramework.getData().forPath(path), Charsets.UTF_8);
	}
}
